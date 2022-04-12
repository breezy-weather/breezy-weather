package wangdaye.com.geometricweather.settings.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import wangdaye.com.geometricweather.BuildConfig
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.GeoActivity
import wangdaye.com.geometricweather.common.ui.widgets.insets.FitStatusBarTopAppBar
import wangdaye.com.geometricweather.common.utils.helpers.IntentHelper
import wangdaye.com.geometricweather.settings.utils.DonateHelper
import wangdaye.com.geometricweather.theme.compose.DayNightTheme
import wangdaye.com.geometricweather.theme.compose.GeometricWeatherTheme
import wangdaye.com.geometricweather.theme.compose.rememberThemeRipple

private class AboutAppLinkItem(
    @DrawableRes val iconId: Int,
    @StringRes val titleId: Int,
    val onClick: () -> Unit,
)

private class TranslatorItem(
    val name: String,
    val url: String,
    val flag: String,
)

class AboutActivity : GeoActivity() {

    private val aboutAppLinks = arrayOf(
        AboutAppLinkItem(
            iconId = R.drawable.ic_github,
            titleId = R.string.gitHub,
        ) {
            IntentHelper.startWebViewActivity(
                this@AboutActivity,
                "https://github.com/WangDaYeeeeee/GeometricWeather"
            )
        },
        AboutAppLinkItem(
            iconId = R.drawable.ic_email,
            titleId = R.string.email,
        ) {
            IntentHelper.startWebViewActivity(
                this@AboutActivity,
                "mailto:wangdayeeeeee@gmail.com"
            )
        },
    )
    private val donateLinks = arrayOf(
        AboutAppLinkItem(
            iconId = R.drawable.ic_alipay,
            titleId = R.string.alipay,
        ) {
            DonateHelper.donateByAlipay(this)
        },
        AboutAppLinkItem(
            iconId = R.drawable.ic_wechat_pay,
            titleId = R.string.wechat,
        ) {
            DonateHelper.donateByWechat(this)
        },
    )
    private val translators = arrayOf(
        TranslatorItem(
            name = "Mehmet Saygin Yilmaz",
            url = "mailto:memcos@gmail.com",
            flag = "🇹🇷",
        ),
        TranslatorItem(
            name = "Ali D.",
            url = "mailto:siyaha@gmail.com",
            flag = "🇹🇷",
        ),
        TranslatorItem(
            name = "benjamin Tourrel",
            url = "mailto:polo_naref@hotmail.fr",
            flag = "🇫🇷",
        ),
        TranslatorItem(
            name = "Roman Adadurov",
            url = "mailto:orelars53@gmail.com",
            flag = "🇷🇺",
        ),
        TranslatorItem(
            name = "Denio",
            url = "mailto:deniosens@yandex.ru",
            flag = "🇷🇺",
        ),
        TranslatorItem(
            name = "Ken Berns",
            url = "mailto:ken.berns@yahoo.de",
            flag = "🇩🇪",
        ),
        TranslatorItem(
            name = "Milan Andrejić",
            url = "mailto:amikia@hotmail.com",
            flag = "🇷🇸",
        ),
        TranslatorItem(
            name = "Miguel Torrijos",
            url = "mailto:migueltg352340@gmail.com",
            flag = "🇪🇸",
        ),
        TranslatorItem(
            name = "juliomartinezrodenas",
            url = "https://github.com/juliomartinezrodenas",
            flag = "🇪🇸",
        ),
        TranslatorItem(
            name = "Andrea Carulli",
            url = "mailto:rctandrew100@gmail.com",
            flag = "🇮🇹",
        ),
        TranslatorItem(
            name = "Jurre Tas",
            url = "mailto:jurretas@gmail.com",
            flag = "🇳🇱",
        ),
        TranslatorItem(
            name = "Jörg Meinhardt",
            url = "mailto:jorime@web.de",
            flag = "🇩🇪",
        ),
        TranslatorItem(
            name = "Olivér Paróczai",
            url = "mailto:oliver.paroczai@gmail.com",
            flag = "🇭🇺",
        ),
        TranslatorItem(
            name = "Fabio Raitz",
            url = "mailto:fabioraitz@outlook.com",
            flag = "🇧🇷",
        ),
        TranslatorItem(
            name = "Gregor",
            url = "mailto:glakner@gmail.com",
            flag = "🇸🇮",
        ),
        TranslatorItem(
            name = "Paróczai Olivér",
            url = "https://github.com/OliverParoczai",
            flag = "🇭🇺",
        ),
        TranslatorItem(
            name = "sodqe muhammad",
            url = "mailto:sodqe.younes@gmail.com",
            flag = "🇦🇪",
        ),
        TranslatorItem(
           name = "Thorsten Eckerlein",
           url = "mailto:thorsten.eckerlein@gmx.de",
           flag = "🇩🇪",
        ),
        TranslatorItem(
            name = "Jiří Král",
            url = "mailto:jirkakral978@gmail.com",
           flag = "🇨🇿",
        ),
        TranslatorItem(
            name = "Kamil",
            url = "mailto:invisiblehype@gmail.com",
            flag = "🇵🇱",
        ),
        TranslatorItem(
            name = "Μιχάλης Καζώνης",
            url = "mailto:istrios@gmail.com",
            flag = "🇬🇷",
        ),
        TranslatorItem(
            name = "이서경",
            url = "mailto:ng0972@naver.com",
            flag = "🇰🇷",
        ),
        TranslatorItem(
            name = "rikupin1105",
            url = "https://github.com/rikupin1105",
            flag = "🇯🇵",
        ),
        TranslatorItem(
            name = "Julien Papasian",
            url = "https://github.com/papjul",
            flag = "🇫🇷",
        ),
        TranslatorItem(
            name = "alexandru l",
            url = "mailto:sandu.lulu@gmail.com",
            flag = "🇷🇴",
        ),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GeometricWeatherTheme(lightTheme = !isSystemInDarkTheme()) {
                ContentView()
            }
        }
    }

    @Composable
    private fun ContentView() {
        Scaffold(
            topBar = {
                FitStatusBarTopAppBar(
                    title = stringResource(R.string.action_about),
                    onBackPressed = { finish() },
                )
            },
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                items(1) { Header() }

                items(1) {
                    Divider(
                        color = MaterialTheme.colorScheme.outline,
                        thickness = 1.dp
                    )
                    SectionTitle(stringResource(R.string.about_app))
                }
                items(aboutAppLinks) { item ->
                    AboutAppLink(
                        iconId = item.iconId,
                        title = stringResource(item.titleId),
                        onClick = item.onClick,
                    )
                }

                items(1) {
                    Divider(
                        color = MaterialTheme.colorScheme.outline,
                        thickness = 1.dp
                    )
                    SectionTitle(stringResource(R.string.donate))
                }
                items(donateLinks) { item ->
                    AboutAppLink(
                        iconId = item.iconId,
                        title = stringResource(item.titleId),
                        onClick = item.onClick,
                    )
                }

                items(1) {
                    Divider(
                        color = MaterialTheme.colorScheme.outline,
                        thickness = 1.dp
                    )
                    SectionTitle(stringResource(R.string.translator))
                }
                items(translators) { item ->
                    Translator(name = item.name, url = item.url, flag = item.flag)
                }

                items(1) {
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    }

    @Composable
    private fun Header() {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_round),
                contentDescription = null,
                modifier = Modifier.size(72.dp),
            )
            Spacer(
                modifier = Modifier
                    .height(dimensionResource(R.dimen.little_margin))
                    .fillMaxWidth()
            )
            Text(
                text = stringResource(R.string.geometric_weather),
                color = DayNightTheme.colors.titleColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = BuildConfig.VERSION_NAME,
                color = DayNightTheme.colors.captionColor,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }

    @Composable
    private fun SectionTitle(title: String) {
        Text(
            text = title,
            modifier = Modifier.padding(dimensionResource(R.dimen.normal_margin)),
            color = DayNightTheme.colors.captionColor,
            style = MaterialTheme.typography.labelMedium,
        )
    }

    @Composable
    private fun AboutAppLink(
        @DrawableRes iconId: Int,
        title: String,
        onClick: () -> Unit,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberThemeRipple(),
                    onClick = onClick,
                )
                .padding(dimensionResource(R.dimen.normal_margin)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(iconId),
                contentDescription = null,
                tint = DayNightTheme.colors.titleColor,
            )
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.normal_margin)))
            Text(
                text = title,
                color = DayNightTheme.colors.titleColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }

    @Composable
    private fun Translator(name: String, url: String, flag: String) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberThemeRipple(),
                    onClick = { IntentHelper.startWebViewActivity(this, url) },
                )
                .padding(dimensionResource(R.dimen.normal_margin)),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    color = DayNightTheme.colors.titleColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.little_margin)))
                Text(
                    text = flag,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }

    @Preview
    @Composable
    private fun DefaultPreview() {
        GeometricWeatherTheme(lightTheme = isSystemInDarkTheme()) {
            ContentView()
        }
    }
}