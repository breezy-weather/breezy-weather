package wangdaye.com.geometricweather.daily.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.unit.DurationUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.PrecipitationUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.ProbabilityUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.basic.model.weather.Daily;
import wangdaye.com.geometricweather.basic.model.weather.HalfDay;
import wangdaye.com.geometricweather.basic.model.weather.Precipitation;
import wangdaye.com.geometricweather.basic.model.weather.PrecipitationDuration;
import wangdaye.com.geometricweather.basic.model.weather.PrecipitationProbability;
import wangdaye.com.geometricweather.basic.model.weather.Temperature;
import wangdaye.com.geometricweather.daily.adapter.holder.AirQualityHolder;
import wangdaye.com.geometricweather.daily.adapter.holder.AstroHolder;
import wangdaye.com.geometricweather.daily.adapter.holder.LargeTitleHolder;
import wangdaye.com.geometricweather.daily.adapter.holder.LineHolder;
import wangdaye.com.geometricweather.daily.adapter.holder.MarginHolder;
import wangdaye.com.geometricweather.daily.adapter.holder.OverviewHolder;
import wangdaye.com.geometricweather.daily.adapter.holder.PollenHolder;
import wangdaye.com.geometricweather.daily.adapter.holder.TitleHolder;
import wangdaye.com.geometricweather.daily.adapter.holder.UVHolder;
import wangdaye.com.geometricweather.daily.adapter.holder.ValueHolder;
import wangdaye.com.geometricweather.daily.adapter.holder.WindHolder;
import wangdaye.com.geometricweather.daily.adapter.model.DailyAirQuality;
import wangdaye.com.geometricweather.daily.adapter.model.DailyAstro;
import wangdaye.com.geometricweather.daily.adapter.model.DailyPollen;
import wangdaye.com.geometricweather.daily.adapter.model.DailyUV;
import wangdaye.com.geometricweather.daily.adapter.model.DailyWind;
import wangdaye.com.geometricweather.daily.adapter.model.LargeTitle;
import wangdaye.com.geometricweather.daily.adapter.model.Line;
import wangdaye.com.geometricweather.daily.adapter.model.Margin;
import wangdaye.com.geometricweather.daily.adapter.model.Overview;
import wangdaye.com.geometricweather.daily.adapter.model.Title;
import wangdaye.com.geometricweather.daily.adapter.model.Value;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;

public class DailyWeatherAdapter extends RecyclerView.Adapter<DailyWeatherAdapter.ViewHolder> {

    private List<ViewModel> modelList;
    private int spanCount;

    public interface ViewModel {
        int getCode();
    }

    public static abstract class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public abstract void onBindView(ViewModel model, int position);
    }

    public GridLayoutManager.SpanSizeLookup spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
        @Override
        public int getSpanSize(int position) {
            int viewType = getItemViewType(position);
            if (Value.isCode(viewType)) {
                return 1;
            } else {
                return spanCount;
            }
        }
    };

    public DailyWeatherAdapter(Context context, Daily daily, int spanCount) {
        this.modelList = new ArrayList<>();
        this.spanCount = spanCount;

        modelList.add(new LargeTitle(context.getString(R.string.daytime)));
        modelList.add(new Overview(daily.day(), true));
        modelList.add(new DailyWind(daily.day().getWind()));
        modelList.addAll(getHalfDayOptionalModelList(context, daily.day()));

        modelList.add(new Line());
        modelList.add(new LargeTitle(context.getString(R.string.nighttime)));
        modelList.add(new Overview(daily.night(), false));
        modelList.add(new DailyWind(daily.night().getWind()));
        modelList.addAll(getHalfDayOptionalModelList(context, daily.night()));

        modelList.add(new Line());
        modelList.add(new LargeTitle(context.getString(R.string.life_details)));
        modelList.add(new DailyAstro(daily.sun(), daily.moon(), daily.getMoonPhase()));
        if (daily.getAirQuality().isValid()) {
            modelList.add(new Title(R.drawable.weather_haze_mini_xml, context.getString(R.string.air_quality)));
            modelList.add(new DailyAirQuality(daily.getAirQuality()));
        }
        if (daily.getPollen().isValid()) {
            modelList.add(new Title(R.drawable.ic_flower, context.getString(R.string.allergen)));
            modelList.add(new DailyPollen(daily.getPollen()));
        }
        if (daily.getUV().isValid()) {
            modelList.add(new Title(R.drawable.ic_uv, context.getString(R.string.uv_index)));
            modelList.add(new DailyUV(daily.getUV()));
        }
        modelList.add(new Line());
        modelList.add(new Value(
                context.getString(R.string.hours_of_sun),
                DurationUnit.H.getDurationText(daily.getHoursOfSun())
        ));
        modelList.add(new Margin());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (LargeTitle.isCode(viewType)) {
            return new LargeTitleHolder(parent);
        } else if (Overview.isCode(viewType)) {
            return new OverviewHolder(parent);
        } else if (Line.isCode(viewType)) {
            return new LineHolder(parent);
        } else if (Margin.isCode(viewType)) {
            return new MarginHolder(parent);
        } else if (Value.isCode(viewType)) {
            return new ValueHolder(parent);
        } else if (Title.isCode(viewType)) {
            return new TitleHolder(parent);
        } else if (DailyAirQuality.isCode(viewType)) {
            return new AirQualityHolder(parent);
        } else if (DailyAstro.isCode(viewType)) {
            return new AstroHolder(parent);
        } else if (DailyPollen.isCode(viewType)) {
            return new PollenHolder(parent);
        } else if (DailyUV.isCode(viewType)) {
            return new UVHolder(parent);
        } else if (DailyWind.isCode(viewType)) {
            return new WindHolder(parent);
        }
        throw new RuntimeException("Invalid viewType.");
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBindView(modelList.get(position), position);
    }

    @Override
    public int getItemViewType(int position) {
        return modelList.get(position).getCode();
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    private List<ViewModel> getHalfDayOptionalModelList(Context context, HalfDay halfDay) {
        List<ViewModel> list = new ArrayList<>();
        // temperature.
        Temperature temperature = halfDay.getTemperature();
        TemperatureUnit temperatureUnit = SettingsOptionManager.getInstance(context).getTemperatureUnit();
        if (temperature.isValid()) {
            TemperatureUnit unit = SettingsOptionManager.getInstance(context).getTemperatureUnit();
            int resId;
            if (unit == TemperatureUnit.C) {
                resId = R.drawable.ic_temperature_celsius;
            } else if (unit == TemperatureUnit.F) {
                resId = R.drawable.ic_temperature_fahrenheit;
            } else {
                resId = R.drawable.ic_temperature_kelvin;
            }
            list.add(new Title(resId, context.getString(R.string.temperature)));
            if (temperature.getRealFeelTemperature() != null) {
                list.add(new Value(
                        context.getString(R.string.real_feel_temperature),
                        temperatureUnit.getTemperatureText(temperature.getRealFeelTemperature())
                ));
            }
            if (temperature.getRealFeelShaderTemperature() != null) {
                list.add(new Value(
                        context.getString(R.string.real_feel_shader_temperature),
                        temperatureUnit.getTemperatureText(temperature.getRealFeelShaderTemperature())
                ));
            }
            if (temperature.getApparentTemperature() != null) {
                list.add(new Value(
                        context.getString(R.string.apparent_temperature),
                        temperatureUnit.getTemperatureText(temperature.getApparentTemperature())
                ));
            }
            if (temperature.getWindChillTemperature() != null) {
                list.add(new Value(
                        context.getString(R.string.wind_chill_temperature),
                        temperatureUnit.getTemperatureText(temperature.getWindChillTemperature())
                ));
            }
            if (temperature.getWetBulbTemperature() != null) {
                list.add(new Value(
                        context.getString(R.string.wet_bulb_temperature),
                        temperatureUnit.getTemperatureText(temperature.getWetBulbTemperature())
                ));
            }
            if (temperature.getDegreeDayTemperature() != null) {
                list.add(new Value(
                        context.getString(R.string.degree_day_temperature),
                        temperatureUnit.getTemperatureText(temperature.getDegreeDayTemperature())
                ));
            }
            list.add(new Margin());
        }

        // precipitation.
        Precipitation precipitation = halfDay.getPrecipitation();
        PrecipitationUnit precipitationUnit = SettingsOptionManager.getInstance(context).getPrecipitationUnit();
        if (precipitation.getTotal() != null && precipitation.getTotal() > 0) {
            list.add(new Title(R.drawable.ic_water, context.getString(R.string.precipitation)));
            list.add(new Value(
                    context.getString(R.string.total),
                    precipitationUnit.getPrecipitationText(precipitation.getTotal())
            ));
            if (precipitation.getRain() != null && precipitation.getRain() > 0) {
                list.add(new Value(
                        context.getString(R.string.rain),
                        precipitationUnit.getPrecipitationText(precipitation.getRain())
                ));
            }
            if (precipitation.getSnow() != null && precipitation.getSnow() > 0) {
                list.add(new Value(
                        context.getString(R.string.snow),
                        precipitationUnit.getPrecipitationText(precipitation.getSnow())
                ));
            }
            if (precipitation.getIce() != null && precipitation.getIce() > 0) {
                list.add(new Value(
                        context.getString(R.string.ice),
                        precipitationUnit.getPrecipitationText(precipitation.getIce())
                ));
            }
            if (precipitation.getThunderstorm() != null && precipitation.getThunderstorm() > 0) {
                list.add(new Value(
                        context.getString(R.string.thunderstorm),
                        precipitationUnit.getPrecipitationText(precipitation.getThunderstorm())
                ));
            }
            list.add(new Margin());
        }

        // precipitation probability.
        PrecipitationProbability probability = halfDay.getPrecipitationProbability();
        if (probability.getTotal() != null && probability.getTotal() > 0) {
            list.add(new Title(R.drawable.ic_water_percent, context.getString(R.string.precipitation_probability)));
            list.add(new Value(
                    context.getString(R.string.total),
                    ProbabilityUnit.PERCENT.getProbabilityText(probability.getTotal())
            ));
            if (probability.getRain() != null && probability.getRain() > 0) {
                list.add(new Value(
                        context.getString(R.string.rain),
                        ProbabilityUnit.PERCENT.getProbabilityText(probability.getRain())
                ));
            }
            if (probability.getSnow() != null && probability.getSnow() > 0) {
                list.add(new Value(
                        context.getString(R.string.snow),
                        ProbabilityUnit.PERCENT.getProbabilityText(probability.getSnow())
                ));
            }
            if (probability.getIce() != null && probability.getIce() > 0) {
                list.add(new Value(
                        context.getString(R.string.ice),
                        ProbabilityUnit.PERCENT.getProbabilityText(probability.getIce())
                ));
            }
            if (probability.getThunderstorm() != null && probability.getThunderstorm() > 0) {
                list.add(new Value(
                        context.getString(R.string.thunderstorm),
                        ProbabilityUnit.PERCENT.getProbabilityText(probability.getThunderstorm())
                ));
            }
            list.add(new Margin());
        }

        // precipitation duration.
        PrecipitationDuration duration = halfDay.getPrecipitationDuration();
        if (duration.getTotal() != null && duration.getTotal() > 0) {
            list.add(new Title(R.drawable.ic_time, context.getString(R.string.precipitation_duration)));
            list.add(new Value(
                    context.getString(R.string.total),
                    DurationUnit.H.getDurationText(duration.getTotal())
            ));
            if (duration.getRain() != null && duration.getRain() > 0) {
                list.add(new Value(
                        context.getString(R.string.rain),
                        DurationUnit.H.getDurationText(duration.getRain())
                ));
            }
            if (duration.getSnow() != null && duration.getSnow() > 0) {
                list.add(new Value(
                        context.getString(R.string.snow),
                        DurationUnit.H.getDurationText(duration.getSnow())
                ));
            }
            if (duration.getIce() != null && duration.getIce() > 0) {
                list.add(new Value(
                        context.getString(R.string.ice),
                        DurationUnit.H.getDurationText(duration.getIce())
                ));
            }
            if (duration.getThunderstorm() != null && duration.getThunderstorm() > 0) {
                list.add(new Value(
                        context.getString(R.string.thunderstorm),
                        DurationUnit.H.getDurationText(duration.getThunderstorm())
                ));
            }
            list.add(new Margin());
        }
        return list;
    }
}
