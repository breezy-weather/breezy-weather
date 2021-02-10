package wangdaye.com.geometricweather.basic;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public abstract class GeoDialog extends DialogFragment {

    private boolean mForeground = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((GeoActivity) requireActivity()).addDialog(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mForeground = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        mForeground = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((GeoActivity) requireActivity()).removeDialog(this);
    }

    public abstract View getSnackbarContainer();

    public boolean isForeground() {
        return mForeground;
    }
}
