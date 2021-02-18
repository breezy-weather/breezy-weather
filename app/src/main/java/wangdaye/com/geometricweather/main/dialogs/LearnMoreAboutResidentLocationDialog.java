package wangdaye.com.geometricweather.main.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoDialog;

/**
 * Learn more about resident location dialog.
 * */
public class LearnMoreAboutResidentLocationDialog extends GeoDialog {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return LayoutInflater.from(getActivity()).inflate(
                R.layout.dialog_resident_location, container, false);
    }

    @Override
    public ViewGroup getSnackbarContainer() {
        return requireDialog().findViewById(R.id.dialog_resident_location_container);
    }
}
