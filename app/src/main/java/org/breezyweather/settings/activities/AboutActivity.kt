/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
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
import androidx.compose.runtime.mutableStateOf
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
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.ui.composables.AlertDialogLink
import org.breezyweather.common.ui.widgets.Material3CardListItem
import org.breezyweather.common.ui.widgets.Material3Scaffold
import org.breezyweather.common.ui.widgets.generateCollapsedScrollBehavior
import org.breezyweather.common.ui.widgets.getCardListItemMarginDp
import org.breezyweather.common.ui.widgets.insets.FitStatusBarTopAppBar
import org.breezyweather.common.ui.widgets.insets.bottomInsetItem
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.theme.compose.BreezyWeatherTheme
import org.breezyweather.theme.compose.DayNightTheme
import org.breezyweather.theme.compose.rememberThemeRipple

private class AboutAppLinkItem(
    @DrawableRes val iconId: Int,
    @StringRes val titleId: Int,
    val onClick: () -> Unit,
)

private class ContributorItem(
    val name: String,
    val github: String? = null,
    val weblate: String? = null,
    val mail: String? = null,
    val url: String? = null,
    @StringRes val contribution: Int? = null
)

private class TranslatorItem(
    val lang: Array<String> = emptyArray(),
    val name: String,
    val github: String? = null,
    val weblate: String? = null,
    val mail: String? = null,
    val url: String? = null
)

class AboutActivity : GeoActivity() {

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
        ContributorItem("Julien Papasian", github = "papjul"),
        ContributorItem("WangDaYeeeeee", github = "WangDaYeeeeee", contribution = R.string.about_contribution_WangDaYeeeeee),
        ContributorItem("Cod3d.", github = "Cod3dDOT"),
        ContributorItem("Romain Théry", github = "rthery"),
        ContributorItem("danielzhang130", github = "danielzhang130"),
        ContributorItem("majjejjam", github = "majjejjam"),
        ContributorItem("Mark Bestavros", github = "mbestavros"),
        ContributorItem("min7-i", github = "min7-i"),
        ContributorItem("Anthony Dégrange", url = "https://anthony-degrange-design.fr/", contribution = R.string.about_contribution_designer)
    )
    // Please keep them ordered by the main language translated so that we can easily sort translators by % contributed
    // Here, we want to sort by language code, which is a different order than in Language.kt
    // If you significantly contributed more than other translators, and you would like to appear
    // first in the list, please open a GitHub issue
    private val translators = arrayOf(
        TranslatorItem(arrayOf("ar"), "sodqe muhammad", mail = "sodqe.younes@gmail.com"),
        TranslatorItem(arrayOf("ar"), "Rex_sa", github = "rex07"),
        TranslatorItem(arrayOf("ar"), "TomatoScriptCPP", github = "TomatoScriptCPP"),
        TranslatorItem(arrayOf("bg"), "elgratea", weblate = "flantito"),
        TranslatorItem(arrayOf("bg"), "StoyanDimitrov", github = "StoyanDimitrov"),
        TranslatorItem(arrayOf("bs"), "Erudaro", github = "Erudaro"),
        TranslatorItem(arrayOf("bs"), "SecularSteve", github = "SecularSteve"),
        TranslatorItem(arrayOf("ca"), "Álvaro Martínez Majado", github = "alvaromartinezmajado"),
        TranslatorItem(arrayOf("ca"), "Arnau Mora", github = "ArnyminerZ"),
        TranslatorItem(arrayOf("ca"), "Sabrina Khan", weblate = "khansabrina594"),
        TranslatorItem(arrayOf("ca"), "Pere Orga", github = "pereorga"),
        TranslatorItem(arrayOf("ckb", "ar"), "anyone00", weblate = "anyone00"),
        TranslatorItem(arrayOf("cs"), "Jiří Král", mail = "jirkakral978@gmail.com"),
        TranslatorItem(arrayOf("cs"), "ikanakova", github = "ikanakova"),
        TranslatorItem(arrayOf("cs"), "esszed", github = "esszed"),
        TranslatorItem(arrayOf("cs"), "Vojta", github = "vojta-dev"),
        TranslatorItem(arrayOf("cs"), "Jiří Král", github = "FrameXX"),
        TranslatorItem(arrayOf("cs"), "Fjuro", github = "Fjuro"),
        TranslatorItem(arrayOf("da"), "Peter", github = "peetabix"),
        TranslatorItem(arrayOf("da"), "Grooty12", weblate = "Grooty12"),
        TranslatorItem(arrayOf("de"), "Ken Berns", mail = "ken.berns@yahoo.de"),
        TranslatorItem(arrayOf("de"), "Jörg Meinhardt", mail = "jorime@web.de"),
        TranslatorItem(arrayOf("de"), "Thorsten Eckerlein", mail = "thorsten.eckerlein@gmx.de"),
        TranslatorItem(arrayOf("de"), "Pascal Dietrich", github = "Cameo007"),
        TranslatorItem(arrayOf("de"), "min7-i", github = "min7-i"),
        TranslatorItem(arrayOf("de"), "Ettore Atalan", github = "Atalanttore"),
        TranslatorItem(arrayOf("de"), "FineFindus", github = "FineFindus"),
        TranslatorItem(arrayOf("de"), "elea11", github = "elea11"),
        TranslatorItem(arrayOf("de"), "Ulion", weblate = "ulion"),
        TranslatorItem(arrayOf("el"), "Μιχάλης Καζώνης", mail = "istrios@gmail.com"),
        TranslatorItem(arrayOf("el"), "Kostas Giapis", github = "tsiflimagas"),
        TranslatorItem(arrayOf("el"), "giwrgosmant", github = "giwrgosmant"),
        TranslatorItem(arrayOf("el"), "Steven Shehata", weblate = "Stidon"),
        TranslatorItem(arrayOf("el"), "Lefteris T.", github = "trlef19"),
        TranslatorItem(arrayOf("es"), "dylan", github = "d-l-n"),
        TranslatorItem(arrayOf("es"), "Miguel Torrijos", mail = "migueltg352340@gmail.com"),
        TranslatorItem(arrayOf("es"), "Julio Martínez Ródenas", github = "juliomartinezrodenas"),
        TranslatorItem(arrayOf("es"), "Hin Weisner", weblate = "Hinweis"),
        TranslatorItem(arrayOf("es"), "gallegonovato", weblate = "gallegonovato"),
        TranslatorItem(arrayOf("es"), "Jose", github = "AzagraMac"),
        TranslatorItem(arrayOf("es"), "Yayi23", github = "Yayi23"),
        TranslatorItem(arrayOf("eu"), "Dabid", github = "desertorea"),
        TranslatorItem(arrayOf("fa"), "Aspen", weblate = "olden"),
        TranslatorItem(arrayOf("fa"), "Armin Bashizade", github = "arminbashizade"),
        TranslatorItem(arrayOf("fa"), "Alireza Rashidi", github = "alr86"),
        TranslatorItem(arrayOf("fr", "en", "eo"), "Julien Papasian", github = "papjul"),
        TranslatorItem(arrayOf("fr"), "Benjamin Tourrel", mail = "polo_naref@hotmail.fr"),
        TranslatorItem(arrayOf("fr"), "Nam", github = "ldmpub"),
        TranslatorItem(arrayOf("fi"), "huuhaa", github = "huuhaa"),
        TranslatorItem(arrayOf("fi"), "nimxaa", github = "nimxaa"),
        TranslatorItem(arrayOf("fi"), "MillionsToOne", github = "MillionsToOne"),
        TranslatorItem(arrayOf("hi", "mr"), "sapatevaibhav", github = "sapatevaibhav"),
        TranslatorItem(arrayOf("hi"), "Chandra Mohan Jha", github = "ChAJ07"),
        TranslatorItem(arrayOf("hr"), "Mateo Spajić", github = "Spajki001"),
        TranslatorItem(arrayOf("hr"), "Milo Ivir", github = "milotype"),
        TranslatorItem(arrayOf("hu"), "Viktor Blaskó", github = "blaskoviktor"),
        TranslatorItem(arrayOf("hu"), "Olivér Paróczai", github = "OliverParoczai"),
        TranslatorItem(arrayOf("hu"), "summoner001", github = "summoner001"),
        TranslatorItem(arrayOf("ia"), "softinterlingua", github = "softinterlingua"),
        TranslatorItem(arrayOf("in"), "MDP43140", github = "MDP43140"),
        TranslatorItem(arrayOf("in"), "Reza", github = "rezaalmanda"),
        TranslatorItem(arrayOf("in"), "Christian Elbrianno", github = "crse"),
        TranslatorItem(arrayOf("in"), "Linerly", github = "Linerly"),
        TranslatorItem(arrayOf("it"), "Andrea Carulli", mail = "rctandrew100@gmail.com"),
        TranslatorItem(arrayOf("it"), "Giovanni Donisi", github = "gdonisi"),
        TranslatorItem(arrayOf("it"), "Henry The Mole", weblate = "htmole"),
        TranslatorItem(arrayOf("it"), "Lorenzo J. Lucchini", github = "LuccoJ"),
        TranslatorItem(arrayOf("it"), "Gabriele Monaco", github = "glemco"),
        TranslatorItem(arrayOf("it"), "Manuel Tassi", github = "Mannivu"),
        TranslatorItem(arrayOf("ja"), "rikupin1105", github = "rikupin1105"),
        TranslatorItem(arrayOf("ja"), "Meiru", weblate = "Tenbin"),
        TranslatorItem(arrayOf("ja"), "若林 さち", weblate = "05e82918ec434690"),
        TranslatorItem(arrayOf("ja"), "しいたけ", github = "Shiitakeeeee"),
        TranslatorItem(arrayOf("kab"), "ButterflyOfFire", weblate = "boffire"),
        TranslatorItem(arrayOf("ko"), "이서경", mail = "ng0972@naver.com"),
        TranslatorItem(arrayOf("ko"), "Yurical", github = "yurical"),
        TranslatorItem(arrayOf("lt"), "Deividas Paukštė", weblate = "RustyOperator"),
        TranslatorItem(arrayOf("lt"), "D221", github = "D221"),
        TranslatorItem(arrayOf("lt"), "splice11", github = "splice11"),
        TranslatorItem(arrayOf("lv"), "Niks Rodžers", weblate = "niks.rodzers.auzins"),
        TranslatorItem(arrayOf("lv"), "Eduards Lusts", weblate = "eduardslu"),
        TranslatorItem(arrayOf("mk"), "ikocevski7", github = "ikocevski7"),
        TranslatorItem(arrayOf("nb_rNO"), "Visnes", github = "Visnes"),
        TranslatorItem(arrayOf("nb_rNO"), "Even Bull-Tornøe", github = "bt0rne"),
        TranslatorItem(arrayOf("nl"), "BabyBenefactor", github = "BabyBenefactor"),
        TranslatorItem(arrayOf("nl"), "Jurre Tas", mail = "jurretas@gmail.com"),
        TranslatorItem(arrayOf("nl"), "trend", github = "trend-1"),
        TranslatorItem(arrayOf("pl"), "Kamil", mail = "invisiblehype@gmail.com"),
        TranslatorItem(arrayOf("pl"), "nid", github = "nidmb"),
        TranslatorItem(arrayOf("pl"), "Eryk Michalak", github = "gnu-ewm"),
        TranslatorItem(arrayOf("pl"), "HackZy01", github = "HackZy01"),
        TranslatorItem(arrayOf("pl"), "GGORG", github = "GGORG0"),
        TranslatorItem(arrayOf("pl"), "maksskorka", github = "maksskorka"),
        TranslatorItem(arrayOf("pl"), "bitzy", weblate = "bitzy"),
        TranslatorItem(arrayOf("pl"), "Daniel Misiarek", weblate = "daniel8f54446d1f224098"),
        TranslatorItem(arrayOf("pl"), "r5jyhte", weblate = "trewtdj"),
        TranslatorItem(arrayOf("pt"), "Silvério Santos", github = "SantosSi"),
        TranslatorItem(arrayOf("pt"), "TiagoAryan", github = "TiagoAryan"),
        TranslatorItem(arrayOf("pt", "pt_rBR"), "Kirakaze", github = "Kirazake"),
        TranslatorItem(arrayOf("pt_rBR"), "Fabio Raitz", mail = "fabioraitz@outlook.com"),
        TranslatorItem(arrayOf("pt_rBR"), "Washington Luiz Candido dos Santos Neto", weblate = "Netocon"),
        TranslatorItem(arrayOf("pt_rBR"), "mf", weblate = "marfS2"),
        TranslatorItem(arrayOf("pt_rBR"), "jucasagr", github = "jucasagr"),
        TranslatorItem(arrayOf("pt_rBR"), "Lucas Fernandes Vitor", weblate = "luc4sfv"),
        TranslatorItem(arrayOf("ro"), "Igor Sorocean", github = "ygorigor"),
        TranslatorItem(arrayOf("ro"), "alexandru l", mail = "sandu.lulu@gmail.com"),
        TranslatorItem(arrayOf("ro"), "sas", weblate = "sas33"),
        TranslatorItem(arrayOf("ro"), "Alexandru51", github = "Alexandru51"),
        TranslatorItem(arrayOf("ro"), "Glassto", github = "Glassto"),
        TranslatorItem(arrayOf("ru"), "Roman Adadurov", mail = "orelars53@gmail.com"),
        TranslatorItem(arrayOf("ru"), "Denio", mail = "deniosens@yandex.ru"),
        TranslatorItem(arrayOf("ru"), "Егор Ермаков", weblate = "creepen"),
        TranslatorItem(arrayOf("sk"), "Kuko", weblate = "kuko7"),
        TranslatorItem(arrayOf("sl_rSI"), "Gregor", mail = "glakner@gmail.com"),
        TranslatorItem(arrayOf("sr"), "NEXI", github = "nexiRS"),
        TranslatorItem(arrayOf("sr"), "Milan Andrejić", mail = "amikia@hotmail.com"),
        TranslatorItem(arrayOf("sv"), "P.O", weblate = "mxvWhxCebxjnmLQxcIr"),
        TranslatorItem(arrayOf("sv"), "Peter Ericson", github = "noscirep"),
        TranslatorItem(arrayOf("sv"), "Luna Jernberg", github = "bittin"),
        TranslatorItem(arrayOf("sv"), "Victor Zamanian", github = "victorz"),
        TranslatorItem(arrayOf("tr"), "Mehmet Saygin Yilmaz", mail = "memcos@gmail.com"),
        TranslatorItem(arrayOf("tr"), "Ali D.", mail = "siyaha@gmail.com"),
        TranslatorItem(arrayOf("tr"), "metezd", weblate = "metezd"),
        TranslatorItem(arrayOf("tr"), "Furkan Karcıoğlu", github = "frknkrc44"),
        TranslatorItem(arrayOf("tr"), "abfreeman", weblate = "abfreeman"),
        TranslatorItem(arrayOf("tr"), "Oğuz Ersen", github = "oersen"),
        TranslatorItem(arrayOf("uk"), "Cod3d.", github = "Cod3dDOT"),
        TranslatorItem(arrayOf("uk"), "Skrripy", weblate = "Skrripy"),
        TranslatorItem(arrayOf("uk"), "Fqwe1", weblate = "Fqwe1"),
        TranslatorItem(arrayOf("uk"), "Сергій", github = "Serega124"),
        TranslatorItem(arrayOf("vi"), "minb", weblate = "minbe"),
        TranslatorItem(arrayOf("vi"), "Fairy", weblate = "Fairy"),
        TranslatorItem(arrayOf("vi"), "ngocanhtve", github = "ngocanhtve"),
        TranslatorItem(arrayOf("zh_rCN", "zh_rHK", "zh_rTW", "en"), "WangDaYeeeeee", github = "WangDaYeeeeee"),
        TranslatorItem(arrayOf("zh_rCN"), "Coelacanthus", github = "CoelacanthusHex"),
        TranslatorItem(arrayOf("zh_rCN"), "御坂13766号", github = "misaka-13766"),
        TranslatorItem(arrayOf("zh_rCN"), "losky2987", github = "losky2987"),
        TranslatorItem(arrayOf("zh_rCN"), "thdcloud", github = "thdcloud"),
        TranslatorItem(arrayOf("zh_rCN", "zh_rHK", "zh_rTW"), "thaumiel9", github = "thaumiel9"),
        TranslatorItem(arrayOf("zh_rCN"), "tomac4t", github = "tomac4t"),
        TranslatorItem(arrayOf("zh_rHK", "zh_rTW"), "abc0922001", github = "abc0922001"),
        TranslatorItem(arrayOf("zh_rCN"), "大王叫我来巡山", weblate = "hamburger2048"),
        TranslatorItem(arrayOf("ja", "zh_rCN", "zh_rHK", "zh_rTW"), "天ツ風", github = "Yibuki"),
        TranslatorItem(arrayOf("zh_rHK", "be", "bg", "bs", "de", "el", "en", "eu", "it", "ja", "mk", "pl", "ru", "uk", "vi"), "kilimov25", github = "kilimov25"),
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

        val linkToOpen = remember { mutableStateOf("") }
        val dialogLinkOpenState = remember { mutableStateOf(false) }

        val locale = this.currentLocale
        val language = locale.language
        val languageWithCountry = locale.language + (if(!locale.country.isNullOrEmpty()) "_r" + locale.country else "")
        var filteredTranslators = translators.filter {
            it.lang.contains(language) || it.lang.contains(languageWithCountry)
        }
        if (filteredTranslators.isEmpty()) {
            // No translators found? Language doesn’t exist, so defaulting to English
            filteredTranslators = translators.filter { it.lang.contains("en") }
        }

        val contactLinks = arrayOf(
            AboutAppLinkItem(
                iconId = R.drawable.ic_code,
                titleId = R.string.about_source_code,
            ) {
                linkToOpen.value = "https://github.com/breezy-weather/breezy-weather"
                dialogLinkOpenState.value = true
            },
            AboutAppLinkItem(
                iconId = R.drawable.ic_forum,
                titleId = R.string.about_matrix,
            ) {
                linkToOpen.value = "https://matrix.to/#/#breezy-weather:matrix.org"
                dialogLinkOpenState.value = true
            },
        )

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
                    ContributorView(name = item.name, contribution = item.contribution) {
                        linkToOpen.value = when {
                            !item.github.isNullOrEmpty() -> "https://github.com/${item.github}"
                            !item.weblate.isNullOrEmpty() -> "https://hosted.weblate.org/user/${item.weblate}/"
                            !item.mail.isNullOrEmpty() -> "mailto:${item.mail}"
                            !item.url.isNullOrEmpty() -> item.url
                            else -> ""
                        }
                        if (linkToOpen.value.isNotEmpty()) {
                            dialogLinkOpenState.value = true
                        }
                    }
                }

                item { SectionTitle(stringResource(R.string.about_translators)) }
                items(filteredTranslators) { item ->
                    ContributorView(name = item.name) {
                        linkToOpen.value = when {
                            !item.github.isNullOrEmpty() -> "https://github.com/${item.github}"
                            !item.weblate.isNullOrEmpty() -> "https://hosted.weblate.org/user/${item.weblate}/"
                            !item.mail.isNullOrEmpty() -> "mailto:${item.mail}"
                            !item.url.isNullOrEmpty() -> item.url
                            else -> ""
                        }
                        if (linkToOpen.value.isNotEmpty()) {
                            dialogLinkOpenState.value = true
                        }
                    }
                }

                bottomInsetItem(
                    extraHeight = getCardListItemMarginDp(this@AboutActivity).dp
                )
            }

            if (dialogLinkOpenState.value) {
                AlertDialogLink(
                    onClose = { dialogLinkOpenState.value = false },
                    linkToOpen = linkToOpen.value
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
    private fun ContributorView(
        name: String,
        @StringRes contribution: Int? = null,
        onClick: () -> Unit
    ) {
        Material3CardListItem {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberThemeRipple(),
                        onClick = {
                            onClick()
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
