package wangdaye.com.geometricweather.common.ui.adapters.location;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.turingtechnologies.materialscrollbar.ICustomAdapter;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.unit.TemperatureUnit;
import wangdaye.com.geometricweather.common.ui.adapters.SyncListAdapter;
import wangdaye.com.geometricweather.theme.ThemeManager;
import wangdaye.com.geometricweather.databinding.ItemLocationBinding;
import wangdaye.com.geometricweather.theme.resource.providers.ResourceProvider;
import wangdaye.com.geometricweather.theme.resource.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsManager;

/**
 * Location adapter.
 * */

public class LocationAdapter extends SyncListAdapter<LocationModel, LocationHolder>
        implements ICustomAdapter {

    private final Context mContext;
    private final OnLocationItemClickListener mClickListener;
    private @Nullable final OnLocationItemDragListener mDragListener;

    private @NonNull final ThemeManager mThemeManager;
    private @NonNull final ResourceProvider mResourceProvider;
    private @NonNull final TemperatureUnit mTemperatureUnit;

    public interface OnLocationItemClickListener {
        void onClick(View view, String formattedId);
    }

    public interface OnLocationItemDragListener {
        void onDrag(LocationHolder holder);
    }

    public LocationAdapter(Context context,
                           List<Location> locationList,
                           @Nullable String selectedId,
                           @NonNull OnLocationItemClickListener clickListener,
                           @Nullable OnLocationItemDragListener dragListener,
                           @NonNull ThemeManager themeManager) {
        super(new ArrayList<>(), new DiffUtil.ItemCallback<LocationModel>() {
            @Override
            public boolean areItemsTheSame(@NonNull LocationModel oldItem, @NonNull LocationModel newItem) {
                return oldItem.areItemsTheSame(newItem);
            }

            @Override
            public boolean areContentsTheSame(@NonNull LocationModel oldItem, @NonNull LocationModel newItem) {
                return oldItem.areContentsTheSame(newItem);
            }
        });
        mContext = context;
        mClickListener = clickListener;
        mDragListener = dragListener;

        mThemeManager = themeManager;
        mResourceProvider = ResourcesProviderFactory.getNewInstance();
        mTemperatureUnit = SettingsManager.getInstance(context).getTemperatureUnit();

        update(locationList, selectedId, null);
    }

    @NonNull
    @Override
    public LocationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LocationHolder(
                ItemLocationBinding.inflate(LayoutInflater.from(parent.getContext())),
                mThemeManager,
                mClickListener,
                mDragListener
        );
    }

    @Override
    public void onBindViewHolder(@NonNull LocationHolder holder, int position) {
        holder.onBindView(mContext, getItem(position), mResourceProvider);
    }

    public void update(@Nullable String selectedId) {
        List<LocationModel> modelList = new ArrayList<>(getItemCount());
        for (LocationModel model : getCurrentList()) {
            modelList.add(
                    new LocationModel(
                            mContext,
                            model.location,
                            mTemperatureUnit,
                            mThemeManager.isLightTheme(mContext),
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
                            mContext,
                            l,
                            mTemperatureUnit,
                            mThemeManager.isLightTheme(mContext),
                            l.getFormattedId().equals(selectedId),
                            l.getFormattedId().equals(forceUpdateId)
                    )
            );
        }
        submitList(modelList);
    }

    public void update(int from, int to) {
        submitMove(from, to);
    }

    @Override
    public void submitList(@NonNull List<LocationModel> newList) {
        super.submitList(newList);
    }

    @ColorInt
    public int getItemSourceColor(int position) {
        if (0 <= position && position < getItemCount()) {
            return getItem(position).weatherSource.getSourceColor();
        } else {
            return Color.TRANSPARENT;
        }
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