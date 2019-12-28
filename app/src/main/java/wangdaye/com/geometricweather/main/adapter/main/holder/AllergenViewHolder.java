package wangdaye.com.geometricweather.main.adapter.main.holder;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.option.unit.PollenUnit;
import wangdaye.com.geometricweather.basic.model.weather.Pollen;
import wangdaye.com.geometricweather.main.MainThemePicker;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

public class AllergenViewHolder extends AbstractMainCardViewHolder {

    private CardView card;
    private TextView title;
    private TextView subtitle;
    
    private AppCompatImageView grassIcon;
    private TextView grassTitle;
    private TextView grassValue;

    private AppCompatImageView ragweedIcon;
    private TextView ragweedTitle;
    private TextView ragweedValue;

    private AppCompatImageView treeIcon;
    private TextView treeTitle;
    private TextView treeValue;

    private AppCompatImageView moldIcon;
    private TextView moldTitle;
    private TextView moldValue;
    
    private PollenUnit unit;

    public AllergenViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.container_main_pollen, parent, false));

        this.card = itemView.findViewById(R.id.container_main_pollen);
        this.title = itemView.findViewById(R.id.container_main_pollen_title);
        this.subtitle = itemView.findViewById(R.id.container_main_pollen_subtitle);

        this.grassIcon = itemView.findViewById(R.id.container_main_pollen_grassIcon);
        this.grassTitle = itemView.findViewById(R.id.container_main_pollen_grassTitle);
        this.grassValue = itemView.findViewById(R.id.container_main_pollen_grassValue);
        this.ragweedIcon = itemView.findViewById(R.id.container_main_pollen_ragweedIcon);
        this.ragweedTitle = itemView.findViewById(R.id.container_main_pollen_ragweedTitle);
        this.ragweedValue = itemView.findViewById(R.id.container_main_pollen_ragweedValue);
        this.treeIcon = itemView.findViewById(R.id.container_main_pollen_treeIcon);
        this.treeTitle = itemView.findViewById(R.id.container_main_pollen_treeTitle);
        this.treeValue = itemView.findViewById(R.id.container_main_pollen_treeValue);
        this.moldIcon = itemView.findViewById(R.id.container_main_pollen_moldIcon);
        this.moldTitle = itemView.findViewById(R.id.container_main_pollen_moldTitle);
        this.moldValue = itemView.findViewById(R.id.container_main_pollen_moldValue);
        
        this.unit = PollenUnit.PPCM;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindView(GeoActivity activity, @NonNull Location location,
                           @NonNull ResourceProvider provider, @NonNull MainThemePicker picker,
                           boolean listAnimationEnabled, boolean itemAnimationEnabled, boolean firstCard) {
        super.onBindView(activity, location, provider, picker,
                listAnimationEnabled, itemAnimationEnabled, firstCard);

        assert location.getWeather() != null;

        card.setCardBackgroundColor(picker.getRootColor(context));
        title.setTextColor(picker.getWeatherThemeColors()[0]);
        subtitle.setTextColor(picker.getTextSubtitleColor(context));

        Pollen pollen = location.getWeather().getDailyForecast().get(0).getPollen();

        grassIcon.setSupportImageTintList(ColorStateList.valueOf(
                Pollen.getPollenColor(itemView.getContext(), pollen.getGrassLevel())
        ));
        grassTitle.setText(context.getString(R.string.grass));
        grassValue.setText(unit.getPollenText(pollen.getGrassIndex()) + " - " + pollen.getGrassDescription());

        ragweedIcon.setSupportImageTintList(ColorStateList.valueOf(
                Pollen.getPollenColor(itemView.getContext(), pollen.getRagweedLevel())
        ));
        ragweedTitle.setText(context.getString(R.string.ragweed));
        ragweedValue.setText(unit.getPollenText(pollen.getRagweedIndex()) + " - " + pollen.getRagweedDescription());

        treeIcon.setSupportImageTintList(ColorStateList.valueOf(
                Pollen.getPollenColor(itemView.getContext(), pollen.getTreeLevel())
        ));
        treeTitle.setText(context.getString(R.string.tree));
        treeValue.setText(unit.getPollenText(pollen.getTreeIndex()) + " - " + pollen.getTreeDescription());

        moldIcon.setSupportImageTintList(ColorStateList.valueOf(
                Pollen.getPollenColor(itemView.getContext(), pollen.getMoldLevel())
        ));
        moldTitle.setText(context.getString(R.string.mold));
        moldValue.setText(unit.getPollenText(pollen.getMoldIndex()) + " - " + pollen.getMoldDescription());

        itemView.setOnClickListener(v -> IntentHelper.startAllergenActivity((GeoActivity) context, location));
    }
}