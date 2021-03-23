package wangdaye.com.geometricweather.common.ui.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.polling.PollingManager;

public class AwakeUpdateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(getApplicationContext(), R.string.refresh, Toast.LENGTH_SHORT).show();
        PollingManager.resetAllBackgroundTask(this, true);
        finish();
    }
}
