package org.breezyweather.settings.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.basic.GeoActivity
import org.breezyweather.common.ui.widgets.Material3CardListItem
import org.breezyweather.common.ui.widgets.Material3Scaffold
import org.breezyweather.common.ui.widgets.generateCollapsedScrollBehavior
import org.breezyweather.common.ui.widgets.getCardListItemMarginDp
import org.breezyweather.common.ui.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.common.ui.widgets.insets.bottomInsetItem
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.theme.compose.DayNightTheme
import org.breezyweather.theme.compose.BreezyWeatherTheme
import org.breezyweather.theme.compose.rememberThemeRipple

private class AboutAppLinkItem(
    @DrawableRes val iconId: Int,
    @StringRes val titleId: Int,
    val onClick: () -> Unit,
)

private class ContributorItem(
    val name: String,
    val contribution: String? = null,
    val url: String,
    val flag: String,
)

class AboutActivity : GeoActivity() {

    private val aboutAppLinks = arrayOf(
        AboutAppLinkItem(
            iconId = R.drawable.ic_github,
            titleId = R.string.about_source_code,
        ) {
            IntentHelper.startWebViewActivity(
                this@AboutActivity,
                "https://github.com/breezy-weather/breezy-weather"
            )
        },
        AboutAppLinkItem(
            iconId = R.drawable.ic_forum,
            titleId = R.string.about_matrix,
        ) {
            IntentHelper.startWebViewActivity(
                this@AboutActivity,
                "https://matrix.to/#/#breezy-weather:matrix.org"
            )
        },
    )
    private lateinit var contributors: Array<ContributorItem>
    private val translators = arrayOf(
        ContributorItem(
            name = "Mehmet Saygin Yilmaz",
            url = "mailto:memcos@gmail.com",
            flag = "🇹🇷",
        ),
        ContributorItem(
            name = "Ali D.",
            url = "mailto:siyaha@gmail.com",
            flag = "🇹🇷",
        ),
        ContributorItem(
            name = "Benjamin Tourrel",
            url = "mailto:polo_naref@hotmail.fr",
            flag = "🇫🇷",
        ),
        ContributorItem(
            name = "Roman Adadurov",
            url = "mailto:orelars53@gmail.com",
            flag = "🇷🇺",
        ),
        ContributorItem(
            name = "Denio",
            url = "mailto:deniosens@yandex.ru",
            flag = "🇷🇺",
        ),
        ContributorItem(
            name = "Ken Berns",
            url = "mailto:ken.berns@yahoo.de",
            flag = "🇩🇪",
        ),
        ContributorItem(
            name = "Milan Andrejić",
            url = "mailto:amikia@hotmail.com",
            flag = "🇷🇸",
        ),
        ContributorItem(
            name = "Miguel Torrijos",
            url = "mailto:migueltg352340@gmail.com",
            flag = "🇪🇸",
        ),
        ContributorItem(
            name = "Julio Martínez Ródenas",
            url = "https://github.com/juliomartinezrodenas",
            flag = "🇪🇸",
        ),
        ContributorItem(
            name = "Andrea Carulli",
            url = "mailto:rctandrew100@gmail.com",
            flag = "🇮🇹",
        ),
        ContributorItem(
            name = "Jurre Tas",
            url = "mailto:jurretas@gmail.com",
            flag = "🇳🇱",
        ),
        ContributorItem(
            name = "Jörg Meinhardt",
            url = "mailto:jorime@web.de",
            flag = "🇩🇪",
        ),
        ContributorItem(
            name = "Olivér Paróczai",
            url = "mailto:oliver.paroczai@gmail.com",
            flag = "🇭🇺",
        ),
        ContributorItem(
            name = "Fabio Raitz",
            url = "mailto:fabioraitz@outlook.com",
            flag = "🇧🇷",
        ),
        ContributorItem(
            name = "Gregor",
            url = "mailto:glakner@gmail.com",
            flag = "🇸🇮",
        ),
        ContributorItem(
            name = "Paróczai Olivér",
            url = "https://github.com/OliverParoczai",
            flag = "🇭🇺",
        ),
        ContributorItem(
            name = "sodqe muhammad",
            url = "mailto:sodqe.younes@gmail.com",
            flag = "🇦🇪",
        ),
        ContributorItem(
           name = "Thorsten Eckerlein",
           url = "mailto:thorsten.eckerlein@gmx.de",
           flag = "🇩🇪",
        ),
        ContributorItem(
            name = "Jiří Král",
            url = "mailto:jirkakral978@gmail.com",
           flag = "🇨🇿",
        ),
        ContributorItem(
            name = "Kamil",
            url = "mailto:invisiblehype@gmail.com",
            flag = "🇵🇱",
        ),
        ContributorItem(
            name = "Μιχάλης Καζώνης",
            url = "mailto:istrios@gmail.com",
            flag = "🇬🇷",
        ),
        ContributorItem(
            name = "이서경",
            url = "mailto:ng0972@naver.com",
            flag = "🇰🇷",
        ),
        ContributorItem(
            name = "rikupin1105",
            url = "https://github.com/rikupin1105",
            flag = "🇯🇵",
        ),
        ContributorItem(
            name = "Julien Papasian",
            url = "https://github.com/papjul",
            flag = "🇫🇷",
        ),
        ContributorItem(
            name = "alexandru l",
            url = "mailto:sandu.lulu@gmail.com",
            flag = "🇷🇴",
        ),
        ContributorItem(
            name = "Pascal Dietrich",
            url = "https://github.com/Cameo007",
            flag = "🇩🇪",
        ),
        ContributorItem(
            name = "Kostas Giapis",
            url = "https://github.com/tsiflimagas",
            flag = "🇬🇷",
        ),
        ContributorItem(
            name = "Giovanni Donisi",
            url = "https://github.com/gdonisi",
            flag = "🇮🇹",
        ),
        ContributorItem(
            name = "nid",
            url = "https://github.com/nidmb",
            flag = "🇵🇱",
        ),
        ContributorItem(
            name = "Álvaro Martínez Majado",
            url = "https://github.com/alvaromartinezmajado",
            flag = "\uD83C\uDFF4\uDB40\uDC65\uDB40\uDC73\uDB40\uDC63\uDB40\uDC74\uDB40\uDC7F",
        ),
        ContributorItem(
            name = "MDP43140",
            url = "https://github.com/MDP43140",
            flag = "\uD83C\uDDEE\uD83C\uDDE9",
        ),
        ContributorItem(
            name = "Coelacanthus",
            url = "https://github.com/CoelacanthusHex",
            flag = "🇨🇳",
        ),
        ContributorItem(
            name = "min7-i",
            url = "https://github.com/min7-i",
            flag = "🇩🇪",
        ),
        ContributorItem(
            name = "Washington Luiz Candido dos Santos Neto",
            url = "https://hosted.weblate.org/user/Netocon/",
            flag = "🇧🇷",
        ),
        ContributorItem(
            name = "Henry The Mole",
            url = "https://hosted.weblate.org/user/htmole/",
            flag = "🇮🇹",
        ),
        ContributorItem(
            name = "sas",
            url = "https://hosted.weblate.org/user/sas33/",
            flag = "🇷🇴",
        ),
        ContributorItem(
            name = "Егор Ермаков",
            url = "https://hosted.weblate.org/user/creepen/",
            flag = "🇷🇺",
        ),
        ContributorItem(
            name = "minb",
            url = "https://hosted.weblate.org/user/minbe/",
            flag = "\uD83C\uDDFB\uD83C\uDDF3",
        ),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        contributors = arrayOf(
            ContributorItem(
                name = "Julien Papasian",
                url = "https://github.com/papjul",
                flag = "🇫🇷",
            ),
            ContributorItem(
                name = "WangDaYeeeeee",
                contribution = this.getString(R.string.about_contribution_WangDaYeeeeee),
                url = "https://github.com/WangDaYeeeeee",
                flag = "🇨🇳",
            ),
            ContributorItem(
                name = "dylan",
                url = "https://github.com/d-l-n",
                flag = "🇦🇷",
            ),
            ContributorItem(
                name = "Nam",
                url = "https://github.com/ldmpub",
                flag = "",
            ),
            ContributorItem(
                name = "Igor Sorocean",
                url = "https://github.com/ygorigor",
                flag = "🇲🇩",
            ),
            ContributorItem(
                name = "EmberHeartshine",
                url = "https://github.com/EmberHeartshine",
                flag = "",
            ),
            ContributorItem(
                name = "majjejjam",
                url = "https://github.com/majjejjam",
                flag = "",
            ),
            ContributorItem(
                name = "Poussinou",
                url = "https://github.com/Poussinou",
                flag = "",
            ),
            ContributorItem(
                name = "Dominik",
                url = "https://github.com/Domi04151309",
                flag = "🇩🇪",
            ),
            ContributorItem(
                name = "Mark Bestavros",
                url = "https://github.com/mbestavros",
                flag = "🇺🇸",
            ),
            ContributorItem(
                name = "giwrgosmant",
                url = "https://github.com/giwrgosmant",
                flag = "🇬🇷",
            ),
            ContributorItem(
                name = "Romain Théry",
                url = "https://github.com/rthery",
                flag = "🇫🇷",
            ),
            ContributorItem(
                name = "Kyler",
                url = "https://github.com/HiFiiDev",
                flag = "🇺🇸",
            )
        )

        setContent {
            BreezyWeatherTheme(lightTheme = !isSystemInDarkTheme()) {
                ContentView()
            }
        }
    }

    @Composable
    private fun ContentView() {
        val scrollBehavior = generateCollapsedScrollBehavior()

        Material3Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                FitStatusBarTopAppBar(
                    title = stringResource(R.string.action_about),
                    onBackPressed = { finish() },
                    scrollBehavior = scrollBehavior,
                )
            },
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                contentPadding = it,
            ) {
                item {
                    Header()
                    SectionTitle(stringResource(R.string.about_app))
                }
                items(aboutAppLinks) { item ->
                    AboutAppLink(
                        iconId = item.iconId,
                        title = stringResource(item.titleId),
                        onClick = item.onClick,
                    )
                }

                item { SectionTitle(stringResource(R.string.about_contributors)) }
                items(contributors) { item ->
                    Translator(name = item.name, contribution = item.contribution, url = item.url, flag = item.flag)
                }

                item { SectionTitle(stringResource(R.string.about_translators)) }
                items(translators) { item ->
                    Translator(name = item.name, url = item.url, flag = item.flag)
                }

                bottomInsetItem(
                    extraHeight = getCardListItemMarginDp(this@AboutActivity).dp
                )
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
                text = stringResource(R.string.breezy_weather),
                color = DayNightTheme.colors.titleColor,
                style = MaterialTheme.typography.headlineSmall,
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
        Material3CardListItem {
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
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }

    @Composable
    private fun Translator(name: String, contribution: String? = null, url: String, flag: String) {
        Material3CardListItem {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberThemeRipple(),
                        onClick = {
                            IntentHelper.startWebViewActivity(this@AboutActivity, url)
                        },
                    )
                    .padding(dimensionResource(R.dimen.normal_margin)),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        color = DayNightTheme.colors.titleColor,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.little_margin)))
                    Text(
                        text = flag,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                if (contribution != null) {
                    Text(
                        text = contribution,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }

    @Preview
    @Composable
    private fun DefaultPreview() {
        BreezyWeatherTheme(lightTheme = isSystemInDarkTheme()) {
            ContentView()
        }
    }
}