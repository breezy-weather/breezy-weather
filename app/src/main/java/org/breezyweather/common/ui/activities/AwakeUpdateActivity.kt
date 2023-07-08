package org.breezyweather.common.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.breezyweather.R
import org.breezyweather.background.polling.PollingManager

class AwakeUpdateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(applicationContext, R.string.action_refresh, Toast.LENGTH_SHORT).show()
        PollingManager.resetAllBackgroundTask(this, true)
        finish()
    }
}
