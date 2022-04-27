package wangdaye.com.geometricweather.search.ui.adapter.location;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.turingtechnologies.materialscrollbar.ICustomAdapter;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.ui.adapters.SyncListAdapter;
import wangdaye.com.geometricweather.databinding.ItemLocationCardBinding;
import wangdaye.com.geometricweather.theme.ThemeManager;

/**
 * Location adapter.
 * */

public class LocationAdapter extends SyncListAdapter<LocationModel, LocationHolder>
        implements ICustomAdapter {

    private final Context mContext;
    private final OnLocationItemClickListener mClickListener;

    @ColorInt
    private final int primaryColor;
    @ColorInt
    private final int surfaceColor;

    public interface OnLocationItemClickListener {
        void onClick(View view, String formattedId);
    }

    public LocationAdapter(Context context,
                           List<Location> locationList,
                           @NonNull OnLocationItemClickListener clickListener) {
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
        primaryColor = ThemeManager.getInstance(context).getThemeColor(context, R.attr.colorPrimary);
        surfaceColor = ThemeManager.getInstance(context).getThemeColor(context, R.attr.colorSurface);

        update(locationList);
    }

    @NonNull
    @Override
    public LocationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LocationHolder(
                ItemLocationCardBinding.inflate(LayoutInflater.from(parent.getContext())),
                mClickListener
        );
    }

    @Override
    public void onBindViewHolder(@NonNull LocationHolder holder, int position) {
        holder.onBindView(mContext, getItem(position), primaryColor, surfaceColor);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationHolder holder, int position,
                                 @NonNull List<Object> payloads) {
        holder.onBindView(mContext, getItem(position), primaryColor, surfaceColor);
    }

    public void update(@NonNull List<Location> newList) {
        List<LocationModel> modelList = new ArrayList<>(newList.size());
        for (Location l : newList) {
            modelList.add(
                    new LocationModel(mContext, l)
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