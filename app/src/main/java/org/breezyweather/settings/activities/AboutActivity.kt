/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

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
import org.breezyweather.settings.SettingsManager
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
    val url: String,
    @StringRes val contribution: Int? = null
)

private class TranslatorItem(
    val lang: Array<String> = emptyArray(),
    val name: String,
    val url: String
)

class AboutActivity : GeoActivity() {

    private val contactLinks = arrayOf(
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
    private val aboutAppLinks = arrayOf(
        AboutAppLinkItem(
            iconId = R.drawable.ic_shield_lock,
            titleId = R.string.about_privacy_policy
        ) {
            IntentHelper.startPrivacyPolicyActivity(this@AboutActivity)
        },
        AboutAppLinkItem(
            iconId = R.drawable.ic_contract,
            titleId = R.string.about_dependencies
        ) {
            IntentHelper.startDependenciesActivity(this@AboutActivity)
        },
    )

    private val contributors: Array<ContributorItem> = arrayOf(
        ContributorItem("Julien Papasian", "https://github.com/papjul"),
        ContributorItem("WangDaYeeeeee", "https://github.com/WangDaYeeeeee", R.string.about_contribution_WangDaYeeeeee),
        ContributorItem("Cod3d.", "https://github.com/Cod3dDOT", R.string.about_contribution_weather_source),
        ContributorItem("Romain Théry", "https://github.com/rthery"),
        ContributorItem("majjejjam", "https://github.com/majjejjam"),
        ContributorItem("Mark Bestavros", "https://github.com/mbestavros")
    )
    // Please keep them ordered by the main language translated so that we can easily sort translators by % contributed
    // Here, we want to sort by language code, which is a different order than in Language.kt
    private val translators = arrayOf(
        TranslatorItem(arrayOf("ar"), "sodqe muhammad", "mailto:sodqe.younes@gmail.com"),
        TranslatorItem(arrayOf("ar"), "Rex_sa", "https://github.com/rex07"),
        TranslatorItem(arrayOf("bg"), "elgratea", "https://hosted.weblate.org/user/flantito/"),
        TranslatorItem(arrayOf("bg"), "StoyanDimitrov", "https://github.com/StoyanDimitrov"),
        TranslatorItem(arrayOf("ca"), "Álvaro Martínez Majado", "https://github.com/alvaromartinezmajado"),
        TranslatorItem(arrayOf("ckb", "ar"), "anyone00", "https://hosted.weblate.org/user/anyone00/"),
        TranslatorItem(arrayOf("cs"), "Jiří Král", "mailto:jirkakral978@gmail.com"),
        TranslatorItem(arrayOf("cs"), "ikanakova", "https://github.com/ikanakova"),
        TranslatorItem(arrayOf("de"), "Ken Berns", "mailto:ken.berns@yahoo.de"),
        TranslatorItem(arrayOf("de"), "Jörg Meinhardt", "mailto:jorime@web.de"),
        TranslatorItem(arrayOf("de"), "Thorsten Eckerlein", "mailto:thorsten.eckerlein@gmx.de"),
        TranslatorItem(arrayOf("de"), "Pascal Dietrich", "https://github.com/Cameo007"),
        TranslatorItem(arrayOf("de"), "min7-i", "https://github.com/min7-i"),
        TranslatorItem(arrayOf("de"), "Ettore Atalan", "https://github.com/Atalanttore"),
        TranslatorItem(arrayOf("de"), "FineFindus", "https://github.com/FineFindus"),
        TranslatorItem(arrayOf("de"), "elea11", "https://github.com/elea11"),
        TranslatorItem(arrayOf("de"), "Ulion", "https://hosted.weblate.org/user/ulion/"),
        TranslatorItem(arrayOf("el"), "Μιχάλης Καζώνης", "mailto:istrios@gmail.com"),
        TranslatorItem(arrayOf("el"), "Kostas Giapis", "https://github.com/tsiflimagas"),
        TranslatorItem(arrayOf("el"), "giwrgosmant", "https://github.com/giwrgosmant"),
        TranslatorItem(arrayOf("es"), "dylan", "https://github.com/d-l-n"),
        TranslatorItem(arrayOf("es"), "Miguel Torrijos", "mailto:migueltg352340@gmail.com"),
        TranslatorItem(arrayOf("es"), "Julio Martínez Ródenas", "https://github.com/juliomartinezrodenas"),
        TranslatorItem(arrayOf("es"), "Hin Weisner", "https://hosted.weblate.org/user/Hinweis/"),
        TranslatorItem(arrayOf("fr", "en"), "Julien Papasian", "https://github.com/papjul"),
        TranslatorItem(arrayOf("fr"), "Benjamin Tourrel", "mailto:polo_naref@hotmail.fr"),
        TranslatorItem(arrayOf("fr"), "Nam", "https://github.com/ldmpub"),
        TranslatorItem(arrayOf("fi"), "huuhaa", "https://github.com/huuhaa"),
        TranslatorItem(arrayOf("hu"), "Olivér Paróczai", "mailto:oliver.paroczai@gmail.com"),
        TranslatorItem(arrayOf("hu"), "Paróczai Olivér", "https://github.com/OliverParoczai"),
        TranslatorItem(arrayOf("in"), "MDP43140", "https://github.com/MDP43140"),
        TranslatorItem(arrayOf("in"), "Reza", "https://github.com/rezaalmanda"),
        TranslatorItem(arrayOf("it"), "Andrea Carulli", "mailto:rctandrew100@gmail.com"),
        TranslatorItem(arrayOf("it"), "Giovanni Donisi", "https://github.com/gdonisi"),
        TranslatorItem(arrayOf("it"), "Henry The Mole", "https://hosted.weblate.org/user/htmole/"),
        TranslatorItem(arrayOf("it"), "Lorenzo J. Lucchini", "https://github.com/LuccoJ"),
        TranslatorItem(arrayOf("it"), "Gabriele Monaco", "https://github.com/glemco"),
        TranslatorItem(arrayOf("ja"), "rikupin1105", "https://github.com/rikupin1105"),
        TranslatorItem(arrayOf("ja"), "Meiru", "https://hosted.weblate.org/user/Tenbin/"),
        TranslatorItem(arrayOf("ja"), "若林 さち", "https://hosted.weblate.org/user/05e82918ec434690/"),
        TranslatorItem(arrayOf("ja"), "しいたけ", "https://github.com/Shiitakeeeee"),
        TranslatorItem(arrayOf("ko"), "이서경", "mailto:ng0972@naver.com"),
        TranslatorItem(arrayOf("ko"), "Yurical", "https://github.com/yurical"),
        TranslatorItem(arrayOf("lt"), "Deividas Paukštė", "https://hosted.weblate.org/user/RustyOperator/"),
        TranslatorItem(arrayOf("lt"), "D221", "https://github.com/D221"),
        TranslatorItem(arrayOf("lv"), "Eduards Lusts", "https://hosted.weblate.org/user/eduardslu/"),
        TranslatorItem(arrayOf("nl"), "Jurre Tas", "mailto:jurretas@gmail.com"),
        TranslatorItem(arrayOf("nl"), "trend", "https://github.com/trend-1"),
        TranslatorItem(arrayOf("pl"), "Kamil", "mailto:invisiblehype@gmail.com"),
        TranslatorItem(arrayOf("pl"), "nid", "https://github.com/nidmb"),
        TranslatorItem(arrayOf("pl"), "Eryk Michalak", "https://github.com/gnu-ewm"),
        TranslatorItem(arrayOf("pl"), "HackZy01", "https://github.com/HackZy01"),
        TranslatorItem(arrayOf("pl"), "GGORG", "https://github.com/GGORG0"),
        TranslatorItem(arrayOf("pt"), "Silvério Santos", "https://github.com/SantosSi"),
        TranslatorItem(arrayOf("pt"), "TiagoAryan", "https://github.com/TiagoAryan"),
        TranslatorItem(arrayOf("pt_rBR"), "Fabio Raitz", "mailto:fabioraitz@outlook.com"),
        TranslatorItem(arrayOf("pt_rBR"), "Washington Luiz Candido dos Santos Neto", "https://hosted.weblate.org/user/Netocon/"),
        TranslatorItem(arrayOf("pt_rBR"), "mf", "https://hosted.weblate.org/user/marfS2/"),
        TranslatorItem(arrayOf("pt_rBR"), "jucasagr", "https://github.com/jucasagr"),
        TranslatorItem(arrayOf("ro"), "Igor Sorocean", "https://github.com/ygorigor"),
        TranslatorItem(arrayOf("ro"), "alexandru l", "mailto:sandu.lulu@gmail.com"),
        TranslatorItem(arrayOf("ro"), "sas", "https://hosted.weblate.org/user/sas33/"),
        TranslatorItem(arrayOf("ro"), "Alexandru51", "https://github.com/Alexandru51"),
        TranslatorItem(arrayOf("ru"), "Roman Adadurov", "mailto:orelars53@gmail.com"),
        TranslatorItem(arrayOf("ru"), "Denio", "mailto:deniosens@yandex.ru"),
        TranslatorItem(arrayOf("ru"), "Егор Ермаков", "https://hosted.weblate.org/user/creepen/"),
        TranslatorItem(arrayOf("ru"), "kilimov25", "https://github.com/kilimov25"),
        TranslatorItem(arrayOf("sk"), "Kuko", "https://hosted.weblate.org/user/kuko7/"),
        TranslatorItem(arrayOf("sl_rSI"), "Gregor", "mailto:glakner@gmail.com"),
        TranslatorItem(arrayOf("sr"), "Milan Andrejić", "mailto:amikia@hotmail.com"),
        TranslatorItem(arrayOf("tr"), "Mehmet Saygin Yilmaz", "mailto:memcos@gmail.com"),
        TranslatorItem(arrayOf("tr"), "Ali D.", "mailto:siyaha@gmail.com"),
        TranslatorItem(arrayOf("tr"), "metezd", "https://hosted.weblate.org/user/metezd/"),
        TranslatorItem(arrayOf("tr"), "Furkan Karcıoğlu", "https://github.com/frknkrc44"),
        TranslatorItem(arrayOf("uk"), "Cod3d.", "https://github.com/Cod3dDOT"),
        TranslatorItem(arrayOf("uk"), "Skrripy", "https://hosted.weblate.org/user/Skrripy/"),
        TranslatorItem(arrayOf("vi"), "minb", "https://hosted.weblate.org/user/minbe/"),
        TranslatorItem(arrayOf("vi"), "Fairy", "https://hosted.weblate.org/user/Fairy/"),
        TranslatorItem(arrayOf("vi"), "ngocanhtve", "https://github.com/ngocanhtve"),
        TranslatorItem(arrayOf("zh_rCN", "zh_rHK", "zh_rTW", "en"), "WangDaYeeeeee", "https://github.com/WangDaYeeeeee"),
        TranslatorItem(arrayOf("zh_rCN"), "Coelacanthus", "https://github.com/CoelacanthusHex"),
        TranslatorItem(arrayOf("zh_rCN"), "御坂13766号", "https://github.com/misaka-13766"),
        TranslatorItem(arrayOf("zh_rCN"), "losky2987", "https://github.com/losky2987"),
        TranslatorItem(arrayOf("zh_rCN"), "thdcloud", "https://github.com/thdcloud"),
        TranslatorItem(arrayOf("zh_rCN", "zh_rHK", "zh_rTW"), "thaumiel9", "https://github.com/thaumiel9"),
        TranslatorItem(arrayOf("zh_rCN"), "tomac4t", "https://github.com/tomac4t"),
        TranslatorItem(arrayOf("zh_rHK", "zh_rTW"), "abc0922001", "https://github.com/abc0922001"),
        TranslatorItem(arrayOf("zh_rCN"), "Eric", "https://hosted.weblate.org/user/hamburger2048/"),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BreezyWeatherTheme(lightTheme = !isSystemInDarkTheme()) {
                ContentView()
            }
        }
    }

    @Composable
    private fun ContentView() {
        val scrollBehavior = generateCollapsedScrollBehavior()

        val locale = SettingsManager.getInstance(this).language.locale
        val language = locale.language
        val languageWithCountry = locale.language + (if(!locale.country.isNullOrEmpty()) "_r" + locale.country else "")
        var filteredTranslators = translators.filter { it.lang.contains(language) || it.lang.contains(languageWithCountry) }
        if (filteredTranslators.isEmpty()) {
            // No translators found? Language doesn’t exist, so defaulting to English
            filteredTranslators = translators.filter { it.lang.contains("en") }
        }

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
                    SectionTitle(stringResource(R.string.about_contact))
                }
                items(contactLinks) { item ->
                    AboutAppLink(
                        iconId = item.iconId,
                        title = stringResource(item.titleId),
                        onClick = item.onClick,
                    )
                }

                item {
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
                    ContributorView(name = item.name, contribution = item.contribution, url = item.url)
                }

                item { SectionTitle(stringResource(R.string.about_translators)) }
                items(filteredTranslators) { item ->
                    ContributorView(name = item.name, url = item.url)
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
                verticalAlignment = Alignment.CenterVertically
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
    private fun ContributorView(name: String, @StringRes contribution: Int? = null, url: String, flag: String? = null) {
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
                    if (flag != null) {
                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.little_margin)))
                        Text(
                            text = flag,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
                if (contribution != null) {
                    Text(
                        text = stringResource(contribution),
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