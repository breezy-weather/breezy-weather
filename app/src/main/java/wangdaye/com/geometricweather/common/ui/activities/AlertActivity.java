package wangdaye.com.geometricweather.common.ui.activities;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.GeoActivity;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.weather.Alert;
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.ui.decotarions.ListDecoration;
import wangdaye.com.geometricweather.common.utils.helpters.AsyncHelper;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.common.ui.adapters.AlertAdapter;

/**
 * Alert activity.
 * */

public class AlertActivity extends GeoActivity {

    public static final String KEY_FORMATTED_ID = "formatted_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        final String formattedId = getIntent().getStringExtra(KEY_FORMATTED_ID);
        AsyncHelper.runOnIO((AsyncHelper.Task<List<Alert>>) emitter -> {

            Location location = null;
            if (TextUtils.isEmpty(formattedId)) {
                location = DatabaseHelper.getInstance(this).readLocation(formattedId);
            }
            if (location == null) {
                location = DatabaseHelper.getInstance(this).readLocationList().get(0);
            }

            Weather weather = DatabaseHelper.getInstance(this).readWeather(location);
            if (weather != null) {
                emitter.send(weather.getAlertList(), true);
            } else {
                emitter.send(new ArrayList<>(), true);
            }
        }, (alerts, done) -> {
            RecyclerView recyclerView = findViewById(R.id.activity_alert_recyclerView);
            recyclerView.setAdapter(new AlertAdapter(alerts));
        });

        Toolbar toolbar = findViewById(R.id.activity_alert_toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.activity_alert_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.addItemDecoration(new ListDecoration(this));
    }
}