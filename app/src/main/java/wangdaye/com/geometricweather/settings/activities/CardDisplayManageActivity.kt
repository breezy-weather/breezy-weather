package wangdaye.com.geometricweather.settings.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import wangdaye.com.geometricweather.common.basic.GeoActivity

class CardDisplayManageActivity : GeoActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ContentView()
        }
    }

    @Composable
    private fun ContentView() {

    }

    @Preview
    @Composable
    private fun DefaultPreview() {
        ContentView()
    }
}