package wangdaye.com.geometricweather.manage.adapter;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.turingtechnologies.materialscrollbar.ICustomAdapter;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;
import wangdaye.com.geometricweather.databinding.ItemLocationBinding;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.utils.manager.ThemeManager;

/**
 * Location adapter.
 * */

public class LocationAdapter extends ListAdapter<LocationModel, LocationHolder>
        implements ICustomAdapter {

    private final Context context;
    private final OnLocationItemClickListener clickListener;
    private @Nullable final OnLocationItemDragListener dragListener;

    private @NonNull final ThemeManager themeManager;
    private @NonNull final ResourceProvider resourceProvider;
    private @NonNull final WeatherSource defaultSource;
    private @NonNull final TemperatureUnit temperatureUnit;

    public LocationAdapter(Context context,
                           List<Location> locationList,
                           @Nullable String selectedId,
                           @NonNull OnLocationItemClickListener clickListener,
                           @Nullable OnLocationItemDragListener dragListener) {
        super(new DiffUtil.ItemCallback<LocationModel>() {
            @Override
            public boolean areItemsTheSame(@NonNull LocationModel oldItem, @NonNull LocationModel newItem) {
                return oldItem.areItemsTheSame(newItem);
            }

            @Override
            public boolean areContentsTheSame(@NonNull LocationModel oldItem, @NonNull LocationModel newItem) {
                return oldItem.areContentsTheSame(newItem);
            }
        });
        this.context = context;
        this.clickListener = clickListener;
        this.dragListener = dragListener;

        this.themeManager = ThemeManager.getInstance(context);
        this.resourceProvider = ResourcesProviderFactory.getNewInstance();
        this.defaultSource = SettingsOptionManager.getInstance(context).getWeatherSource();
        this.temperatureUnit = SettingsOptionManager.getInstance(context).getTemperatureUnit();

        update(locationList, selectedId, null);
    }

    @NonNull
    @Override
    public LocationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LocationHolder(
                ItemLocationBinding.inflate(LayoutInflater.from(parent.getContext())),
                clickListener,
                dragListener
        );
    }

    @Override
    public void onBindViewHolder(@NonNull LocationHolder holder, int position) {
        holder.onBindView(context, getItem(position), resourceProvider);
    }

    public void update(@Nullable String selectedId) {
        List<LocationModel> modelList = new ArrayList<>(getItemCount());
        for (LocationModel model : getCurrentList()) {
            modelList.add(
                    new LocationModel(
                            context,
                            model.location,
                            temperatureUnit,
                            defaultSource,
                            themeManager.isLightTheme(),
                            model.location.getFormattedId().equals(selectedId),
                            false
                    )
            );
        }
        submitList(modelList);
    }

    public void update(@NonNull List<Location> newList,
                       @Nullable String selectedId,
                       @Nullable String forceUpdateId) {
        List<LocationModel> modelList = new ArrayList<>(newList.size());
        for (Location l : newList) {
            modelList.add(
                    new LocationModel(
                            context,
                            l,
                            temperatureUnit,
                            defaultSource,
                            themeManager.isLightTheme(),
                            l.getFormattedId().equals(selectedId),
                            l.getFormattedId().equals(forceUpdateId)
                    )
            );
        }
        submitList(modelList);
    }

    @ColorInt
    public int getItemSourceColor(int position) {
        if (0 <= position && position < getItemCount()) {
            return getItem(position).weatherSource.getSourceColor();
        } else {
            return Color.TRANSPARENT;
        }
    }

    // interface.

    public interface OnLocationItemClickListener {
        void onClick(View view, String formattedId);
    }

    public interface OnLocationItemDragListener {
        void onDrag(LocationHolder holder);
    }

    // I custom adapter.

    @Override
    public String getCustomStringForElement(int element) {
        if (getItemCount() == 0) {
            return "";
        }
        return getItem(element).weatherSource.getSourceUrl();
    }
}