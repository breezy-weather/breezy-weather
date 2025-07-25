/*
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

package org.breezyweather.data

import androidx.annotation.StringRes
import org.breezyweather.R

class ContributorItem(
    val name: String,
    val github: String? = null,
    val weblate: String? = null,
    val mail: String? = null,
    val url: String? = null,
    @StringRes val contribution: Int? = null,
) {
    val link = when {
        !github.isNullOrEmpty() -> "https://github.com/$github"
        !weblate.isNullOrEmpty() -> "https://hosted.weblate.org/user/$weblate/"
        !mail.isNullOrEmpty() -> "mailto:$mail"
        !url.isNullOrEmpty() -> url
        else -> ""
    }
}

class TranslatorItem(
    val lang: Array<String> = emptyArray(),
    val name: String,
    val github: String? = null,
    val weblate: String? = null,
    val mail: String? = null,
    val url: String? = null,
)

val appContributors: Array<ContributorItem> = arrayOf(
    ContributorItem("Julien Papasian", github = "papjul"),
    ContributorItem(
        "WangDaYeeeeee",
        github = "WangDaYeeeeee",
        contribution = R.string.about_contribution_WangDaYeeeeee
    ),
    /**
     * Many contributions
     */
    ContributorItem("min7-i", github = "min7-i"),
    /**
     * Many contributions
     */
    ContributorItem("chunshek", github = "chunshek"),
    /**
     * - Fix daily shift in widgets
     * - Improvements to the Android location source
     * - Many other contributions that I can no longer find
     */
    ContributorItem("Cod3d.", github = "Cod3dDOT"),
    /**
     * - Disable animation settings when disabled at system level
     * - Fix location dialog opening twice
     * - Fix location dialog being duplicated on certain screens
     * - Fix colors in composables
     * - Exclude live wallpaper preview from recent apps list
     */
    ContributorItem("ecawthorne", github = "ecawthorne"),
    /**
     * - Add cache support to OkHttp
     * - Added method to compute hourly/current UV based on daily UV
     */
    ContributorItem("Romain Théry", github = "rthery"),
    /**
     * Clean up / Documentation
     */
    ContributorItem("Mark Bestavros", github = "mbestavros"),
    /**
     * Dark mode fixes
     */
    ContributorItem("Suyash Gupta", github = "suyashgupta25"),
    /**
     * Fix Chinese calendar days
     */
    ContributorItem("Coelacanthus", github = "CoelacanthusHex"),
    /**
     * Show full multiline forecast in expanded notification
     */
    ContributorItem("danielzhang130", github = "danielzhang130"),
    /**
     * Added spacing between quantity and unit according to ISO.
     */
    ContributorItem("majjejjam", github = "majjejjam"),
    /**
     * MET Office source
     */
    ContributorItem("bunburya", github = "bunburya"),
    /**
     * Fix lag in refresh
     */
    ContributorItem("jayyuz", github = "jayyuz"),
    /**
     * Fix hourly forecast hours in China source
     */
    ContributorItem("JiunnTarn", github = "JiunnTarn"),
    /**
     * Remove trailing spaces in search
     */
    ContributorItem("mags0ft", github = "mags0ft"),
    /**
     * Make the app seen as a weather app for the system
     */
    ContributorItem("Devy Ballard", github = "devycarol"),
    /**
     * Fix dark mode of the Edit location dialog
     */
    ContributorItem("Mushfiq1060", github = "Mushfiq1060"),
    /**
     * Fix hourly tabs disappearing
     */
    ContributorItem("ccyybn", github = "ccyybn"),
    /**
     * Don’t open location list in landscape by default when there is only 1 location
     */
    ContributorItem("Doğaç Tanrıverdi", github = "DogacTanriverdi"),
    /**
     * Memory leak
     */
    ContributorItem("Nero Nguyen", github = "neronguyenvn"),
    /**
     * UX of custom subtitle documentation
     */
    ContributorItem("Dipesh Pal", github = "codewithdipesh"),
    /**
     * Logo
     */
    ContributorItem(
        "Anthony Dégrange",
        url = "https://anthony-degrange-design.fr/",
        contribution = R.string.about_contribution_designer
    )
)

// Please keep them ordered by the main language translated so that we can easily sort translators by % contributed
// Here, we want to sort by language code, which is a different order than in Language.kt
// If you significantly contributed more than other translators, and you would like to appear
// first in the list, please open a GitHub issue
val appTranslators = arrayOf(
    TranslatorItem(arrayOf("ar"), "sodqe muhammad", mail = "sodqe.younes@gmail.com"),
    TranslatorItem(arrayOf("ar"), "Rex_sa", github = "rex07"),
    TranslatorItem(arrayOf("ar"), "TomatoScriptCPP", github = "TomatoScriptCPP"),
    TranslatorItem(arrayOf("ar"), "jonnysemon", weblate = "jonnysemon"),
    TranslatorItem(arrayOf("be"), "Yauhen", weblate = "Bugomol"),
    TranslatorItem(arrayOf("be"), "Drugi Sapog", weblate = "DinDrugi"),
    TranslatorItem(arrayOf("be"), "Ding User", weblate = "DeNGus"),
    TranslatorItem(arrayOf("bg"), "elgratea", weblate = "flantito"),
    TranslatorItem(arrayOf("bg"), "StoyanDimitrov", github = "StoyanDimitrov"),
    TranslatorItem(arrayOf("bg"), "srmihnev", github = "srmihnev"),
    TranslatorItem(arrayOf("bn"), "Manab Ray", github = "manabray"),
    TranslatorItem(arrayOf("bn"), "The Contributor", weblate = "another_user"),
    TranslatorItem(arrayOf("bn"), "Fahim Ahmed", github = "fahim-ahmed05"),
    TranslatorItem(arrayOf("bn"), "Dipyaman Roy", github = "dipyamanroy"),
    TranslatorItem(arrayOf("bn"), "ferus3", weblate = "ferus3"),
    TranslatorItem(arrayOf("bs"), "Erudaro", github = "Erudaro"),
    TranslatorItem(arrayOf("bs"), "SecularSteve", github = "SecularSteve"),
    TranslatorItem(arrayOf("ca"), "Álvaro Martínez Majado", github = "alvaromartinezmajado"),
    TranslatorItem(arrayOf("ca"), "Arnau Mora", github = "ArnyminerZ"),
    TranslatorItem(arrayOf("ca"), "Sabrina Khan", weblate = "khansabrina594"),
    TranslatorItem(arrayOf("ca"), "Pere Orga", github = "pereorga"),
    TranslatorItem(arrayOf("ca"), "Jaime Muñoz Martín", github = "kayron8"),
    TranslatorItem(arrayOf("ca"), "John Doe", weblate = "healthyburrito"),
    TranslatorItem(arrayOf("ca"), "gReventos", github = "gReventos"),
    TranslatorItem(arrayOf("ca"), "BennyBeat", github = "BennyBeat"),
    TranslatorItem(arrayOf("ckb", "ar"), "anyone00", weblate = "anyone00"),
    TranslatorItem(arrayOf("cs"), "Jiří Král", mail = "jirkakral978@gmail.com"),
    TranslatorItem(arrayOf("cs"), "ikanakova", github = "ikanakova"),
    TranslatorItem(arrayOf("cs"), "esszed", github = "esszed"),
    TranslatorItem(arrayOf("cs"), "Vojta", github = "vojta-dev"),
    TranslatorItem(arrayOf("cs"), "Jiří Král", github = "FrameXX"),
    TranslatorItem(arrayOf("cs"), "Fjuro", github = "Fjuro"),
    TranslatorItem(arrayOf("da"), "Rasmus", weblate = "Grooty"),
    TranslatorItem(arrayOf("da"), "Peter", github = "peetabix"),
    TranslatorItem(arrayOf("da"), "Grooty12", weblate = "Grooty12"),
    TranslatorItem(arrayOf("da"), "Benjamin Nielsen", weblate = "devjam1n"),
    TranslatorItem(arrayOf("da"), "Michael Millet", weblate = "mrMillet"),
    TranslatorItem(arrayOf("de"), "Ken Berns", mail = "ken.berns@yahoo.de"),
    TranslatorItem(arrayOf("de"), "Jörg Meinhardt", mail = "jorime@web.de"),
    TranslatorItem(arrayOf("de"), "Thorsten Eckerlein", mail = "thorsten.eckerlein@gmx.de"),
    TranslatorItem(arrayOf("de"), "Pascal Dietrich", github = "Cameo007"),
    TranslatorItem(arrayOf("de"), "min7-i", github = "min7-i"),
    TranslatorItem(arrayOf("de"), "Ettore Atalan", github = "Atalanttore"),
    TranslatorItem(arrayOf("de"), "FineFindus", github = "FineFindus"),
    TranslatorItem(arrayOf("de"), "elea11", github = "elea11"),
    TranslatorItem(arrayOf("de"), "Ulion", weblate = "ulion"),
    TranslatorItem(arrayOf("de"), "ColorfulRhino", weblate = "ColorfulRhino"),
    TranslatorItem(arrayOf("de"), "Lacey Anaya", weblate = "lanAYA"),
    TranslatorItem(arrayOf("de"), "Kachelkaiser", github = "Kachelkaiser"),
    TranslatorItem(arrayOf("de"), "Lenny Angst", github = "Lezurex"),
    TranslatorItem(arrayOf("el"), "Μιχάλης Καζώνης", mail = "istrios@gmail.com"),
    TranslatorItem(arrayOf("el"), "Kostas Giapis", github = "tsiflimagas"),
    TranslatorItem(arrayOf("el"), "giwrgosmant", github = "giwrgosmant"),
    TranslatorItem(arrayOf("el"), "Steven Shehata", weblate = "Stidon"),
    TranslatorItem(arrayOf("el"), "Lefteris T.", github = "trlef19"),
    TranslatorItem(arrayOf("eo"), "phlostically", weblate = "phlostically"),
    TranslatorItem(arrayOf("eo"), "Oasis Tri", weblate = "Oasis3"),
    TranslatorItem(arrayOf("eo", "cs"), "Valentin Lluba", weblate = "circulate"),
    TranslatorItem(arrayOf("es"), "dylan", github = "d-l-n"),
    TranslatorItem(arrayOf("es"), "Miguel Torrijos", mail = "migueltg352340@gmail.com"),
    TranslatorItem(arrayOf("es"), "Julio Martínez Ródenas", github = "juliomartinezrodenas"),
    TranslatorItem(arrayOf("es"), "Hin Weisner", weblate = "Hinweis"),
    TranslatorItem(arrayOf("es"), "gallegonovato", weblate = "gallegonovato"),
    TranslatorItem(arrayOf("es"), "Jose", github = "AzagraMac"),
    TranslatorItem(arrayOf("es"), "Yayi23", github = "Yayi23"),
    TranslatorItem(arrayOf("es"), "Eraorahan", weblate = "eraorahan"),
    TranslatorItem(arrayOf("es"), "Jose l. Azagra", github = "azagramac"),
    TranslatorItem(arrayOf("es"), "Traductor", github = "cyphra"),
    TranslatorItem(arrayOf("et"), "kovabait12", github = "kovabait12"),
    TranslatorItem(arrayOf("et"), "Priit Jõerüüt", weblate = "jrthwlate"),
    TranslatorItem(arrayOf("et"), "Gert Lutter", weblate = "ruut.103"),
    TranslatorItem(arrayOf("et"), "Theodor Põlluste", github = "theodor373"),
    TranslatorItem(arrayOf("et"), "rimasx", github = "rimasx"),
    TranslatorItem(arrayOf("eu"), "Dabid", github = "desertorea"),
    TranslatorItem(arrayOf("eu"), "beriain", github = "beriain"),
    TranslatorItem(arrayOf("eu"), "xabiliza", github = "xabiliza"),
    TranslatorItem(arrayOf("eu"), "Isolus", weblate = "isolus"),
    TranslatorItem(arrayOf("fa"), "Aspen", weblate = "olden"),
    TranslatorItem(arrayOf("fa"), "Armin Bashizade", github = "arminbashizade"),
    TranslatorItem(arrayOf("fa"), "Alireza Rashidi", github = "alr86"),
    TranslatorItem(arrayOf("fa"), "Monirzadeh", github = "Monirzadeh"),
    TranslatorItem(arrayOf("fr", "en", "eo"), "Julien Papasian", github = "papjul"),
    TranslatorItem(arrayOf("fr"), "Benjamin Tourrel", mail = "polo_naref@hotmail.fr"),
    TranslatorItem(arrayOf("fr"), "Nam", github = "ldmpub"),
    TranslatorItem(arrayOf("fi"), "huuhaa", github = "huuhaa"),
    TranslatorItem(arrayOf("fi"), "nimxaa", github = "nimxaa"),
    TranslatorItem(arrayOf("fi"), "MillionsToOne", github = "MillionsToOne"),
    TranslatorItem(arrayOf("fi"), "Jane Doe", weblate = "Decaf3683"),
    TranslatorItem(arrayOf("fi"), "Ricky-Tigg", github = "Ricky-Tigg"),
    TranslatorItem(arrayOf("fi"), "Juli", weblate = "Julimiro"),
    TranslatorItem(arrayOf("ga"), "Aindriú Mac Giolla Eoin", github = "aindriu80"),
    TranslatorItem(arrayOf("gl"), "Adrian Hermida Baloira", github = "adrianhermida"),
    TranslatorItem(arrayOf("gl"), "xcomesana", github = "xcomesana"),
    TranslatorItem(arrayOf("gl"), "Roi", weblate = "roicou"),
    TranslatorItem(arrayOf("he", "iw"), "nick", github = "nvurgaft"),
    TranslatorItem(arrayOf("he", "iw"), "Doge", weblate = "Doge"),
    TranslatorItem(arrayOf("hi", "mr"), "Sapate Vaibhav", github = "sapatevaibhav"),
    TranslatorItem(arrayOf("hi"), "Chandra Mohan Jha", github = "ChAJ07"),
    TranslatorItem(arrayOf("hi"), "Deepesh Singh Chauhan", github = "master2619"),
    TranslatorItem(arrayOf("hi"), "ShareASmile", weblate = "ShareASmile"),
    TranslatorItem(arrayOf("hi"), "Akshat", weblate = "Akshat-Projects"),
    TranslatorItem(arrayOf("hr"), "Mateo Spajić", github = "Spajki001"),
    TranslatorItem(arrayOf("hr"), "Milo Ivir", github = "milotype"),
    TranslatorItem(arrayOf("hr"), "ggdorman", github = "ggdorman"),
    TranslatorItem(arrayOf("hu"), "Viktor Blaskó", github = "blaskoviktor"),
    TranslatorItem(arrayOf("hu"), "Olivér Paróczai", github = "OliverParoczai"),
    TranslatorItem(arrayOf("hu"), "summoner001", github = "summoner001"),
    TranslatorItem(arrayOf("hu"), "NBencee", github = "NBencee"),
    TranslatorItem(arrayOf("ia"), "softinterlingua", github = "softinterlingua"),
    TranslatorItem(arrayOf("in"), "MDP43140", github = "MDP43140"),
    TranslatorItem(arrayOf("in"), "Reza", github = "rezaalmanda"),
    TranslatorItem(arrayOf("in"), "Christian Elbrianno", github = "crse"),
    TranslatorItem(arrayOf("in"), "Linerly", github = "Linerly"),
    TranslatorItem(arrayOf("in"), "Adrien N", weblate = "adriennathaniel1999"),
    TranslatorItem(arrayOf("it"), "Andrea Carulli", mail = "rctandrew100@gmail.com"),
    TranslatorItem(arrayOf("it"), "Giovanni Donisi", github = "gdonisi"),
    TranslatorItem(arrayOf("it"), "Henry The Mole", weblate = "htmole"),
    TranslatorItem(arrayOf("it"), "Lorenzo J. Lucchini", github = "LuccoJ"),
    TranslatorItem(arrayOf("it"), "Gabriele Monaco", github = "glemco"),
    TranslatorItem(arrayOf("it"), "Manuel Tassi", github = "Mannivu"),
    TranslatorItem(arrayOf("it"), "Ulisse Perusin", github = "ulipo"),
    TranslatorItem(arrayOf("it"), "Lorenzo Romano", weblate = "lloranmorenzio"),
    TranslatorItem(arrayOf("it"), "Innominatapersona", github = "Innominatapersona"),
    TranslatorItem(arrayOf("it"), "bryce-lynch", weblate = "bryce-lynch"),
    TranslatorItem(arrayOf("it"), "Giorgio", github = "dimeglio98"),
    TranslatorItem(arrayOf("it"), "mapi68", github = "mapi68"),
    TranslatorItem(arrayOf("ja"), "rikupin1105", github = "rikupin1105"),
    TranslatorItem(arrayOf("ja"), "Suguru Hirahara", weblate = "shirahara"),
    TranslatorItem(arrayOf("ja"), "Meiru", weblate = "Tenbin"),
    TranslatorItem(arrayOf("ja"), "若林 さち", weblate = "05e82918ec434690"),
    TranslatorItem(arrayOf("ja"), "しいたけ", github = "Shiitakeeeee"),
    TranslatorItem(arrayOf("kab"), "ButterflyOfFire", weblate = "boffire"),
    TranslatorItem(arrayOf("kab"), "Ziri Sut", github = "ZiriSut"),
    TranslatorItem(arrayOf("ko"), "이서경", mail = "ng0972@naver.com"),
    TranslatorItem(arrayOf("ko"), "Yurical", github = "yurical"),
    TranslatorItem(arrayOf("ko"), "ID J", weblate = "tabby4442"),
    TranslatorItem(arrayOf("ko"), "Alex", github = "whatthesamuel"),
    TranslatorItem(arrayOf("ko"), "agw76638", github = "agw76638"),
    TranslatorItem(arrayOf("ko"), "tabby", weblate = "tabby"),
    TranslatorItem(arrayOf("lt"), "Deividas Paukštė", weblate = "RustyOperator"),
    TranslatorItem(arrayOf("lt"), "D221", github = "D221"),
    TranslatorItem(arrayOf("lt"), "splice11", github = "splice11"),
    TranslatorItem(arrayOf("lt"), "Oliveinparis", github = "Oliveinparis"),
    TranslatorItem(arrayOf("lv"), "Niks Rodžers", weblate = "niks.rodzers.auzins"),
    TranslatorItem(arrayOf("lv"), "Eduards Lusts", weblate = "eduardslu"),
    TranslatorItem(arrayOf("lv"), "Edgars Andersons", weblate = "Edgarsons"),
    TranslatorItem(arrayOf("lv"), "09pulse", weblate = "09pulse"),
    TranslatorItem(arrayOf("lv"), "Coool", github = "Coool"),
    TranslatorItem(arrayOf("mk"), "ikocevski7", github = "ikocevski7"),
    TranslatorItem(arrayOf("mk"), "Rijolo", weblate = "rijolo4790"),
    TranslatorItem(arrayOf("nb_rNO"), "Even Bull-Tornøe", github = "bt0rne"),
    TranslatorItem(arrayOf("nb_rNO"), "Visnes", github = "Visnes"),
    TranslatorItem(arrayOf("nb_rNO"), "Simen", weblate = "sien"),
    TranslatorItem(arrayOf("nl"), "BabyBenefactor", github = "BabyBenefactor"),
    TranslatorItem(arrayOf("nl"), "Jurre Tas", mail = "jurretas@gmail.com"),
    TranslatorItem(arrayOf("nl"), "trend", github = "trend-1"),
    TranslatorItem(arrayOf("nl"), "programpro2005", github = "programpro2005"),
    TranslatorItem(arrayOf("nl"), "OliNau", github = "OliNau"),
    TranslatorItem(arrayOf("nl"), "CouldBeMathijs", github = "JustPassingBy06"),
    TranslatorItem(arrayOf("nl"), "that translator", weblate = "Translate"),
    TranslatorItem(arrayOf("nl"), "Stef Smeets", github = "stefsmeets"),
    TranslatorItem(arrayOf("nl"), "Roan-V", github = "Roan-V"),
    TranslatorItem(arrayOf("nl"), "kyrawertho", github = "kyrawertho"),
    TranslatorItem(arrayOf("nl"), "Brecht", github = "brecht6"),
    TranslatorItem(arrayOf("oc"), "Quentin PAGÈS", weblate = "Quenti"),
    TranslatorItem(arrayOf("pl"), "Kamil", mail = "invisiblehype@gmail.com"),
    TranslatorItem(arrayOf("pl"), "nid", github = "nidmb"),
    TranslatorItem(arrayOf("pl"), "Eryk Michalak", github = "gnu-ewm"),
    TranslatorItem(arrayOf("pl"), "HackZy01", github = "HackZy01"),
    TranslatorItem(arrayOf("pl"), "GGORG", github = "GGORG0"),
    TranslatorItem(arrayOf("pl"), "maksskorka", github = "maksskorka"),
    TranslatorItem(arrayOf("pl"), "bitzy", weblate = "bitzy"),
    TranslatorItem(arrayOf("pl"), "Daniel Misiarek", weblate = "daniel8f54446d1f224098"),
    TranslatorItem(arrayOf("pl"), "r5jyhte", weblate = "trewtdj"),
    TranslatorItem(arrayOf("pl"), "diskacz", github = "diskacz"),
    TranslatorItem(arrayOf("pt"), "Silvério Santos", github = "SantosSi"),
    TranslatorItem(arrayOf("pt"), "TiagoAryan", github = "TiagoAryan"),
    TranslatorItem(arrayOf("pt"), "Pedro", github = "pdafv"),
    TranslatorItem(arrayOf("pt", "de", "es"), "Murcielago", weblate = "MRCLG"),
    TranslatorItem(arrayOf("pt", "pt_rBR"), "Kirakaze", github = "Kirazake"),
    TranslatorItem(arrayOf("pt_rBR"), "Fabio Raitz", mail = "fabioraitz@outlook.com"),
    TranslatorItem(arrayOf("pt_rBR"), "Washington Luiz Candido dos Santos Neto", weblate = "Netocon"),
    TranslatorItem(arrayOf("pt_rBR"), "mf", weblate = "marfS2"),
    TranslatorItem(arrayOf("pt_rBR"), "jucasagr", github = "jucasagr"),
    TranslatorItem(arrayOf("pt_rBR"), "Lucas Fernandes Vitor", weblate = "luc4sfv"),
    TranslatorItem(arrayOf("pt_rBR"), "tetify", github = "tetify"),
    TranslatorItem(arrayOf("pt_rBR"), "DanGLES3", github = "DanGLES3"),
    TranslatorItem(arrayOf("pt_rBR"), "OlliesGudh", github = "OlliesGudh"),
    TranslatorItem(arrayOf("pt_rBR"), "burns", github = "alvaroburns"),
    TranslatorItem(arrayOf("ro"), "Igor Sorocean", github = "ygorigor"),
    TranslatorItem(arrayOf("ro"), "alexandru l", mail = "sandu.lulu@gmail.com"),
    TranslatorItem(arrayOf("ro"), "sas", weblate = "sas33"),
    TranslatorItem(arrayOf("ro"), "Alexandru51", github = "Alexandru51"),
    TranslatorItem(arrayOf("ro"), "Glassto", github = "Glassto"),
    TranslatorItem(arrayOf("ro"), "Renko", github = "Renko"),
    TranslatorItem(arrayOf("ro"), "David", weblate = "David7e16baa08f0b4658"),
    TranslatorItem(arrayOf("ru"), "Roman Adadurov", mail = "orelars53@gmail.com"),
    TranslatorItem(arrayOf("ru"), "Denio", mail = "deniosens@yandex.ru"),
    TranslatorItem(arrayOf("ru"), "Егор Ермаков", weblate = "creepen"),
    TranslatorItem(arrayOf("ru"), "TenchMaviatorius2759", github = "TenchMaviatorius2759"),
    TranslatorItem(arrayOf("ru"), "mak7im01", github = "mak7im01"),
    TranslatorItem(arrayOf("ru"), "Tim", weblate = "dlee"),
    TranslatorItem(arrayOf("sk"), "Kuko", weblate = "kuko7"),
    TranslatorItem(arrayOf("sk", "cs"), "Viliam Geffert", github = "vgeffer"),
    TranslatorItem(arrayOf("sk"), "aasami", weblate = "aasami"),
    TranslatorItem(arrayOf("sl_rSI"), "Gregor", mail = "glakner@gmail.com"),
    TranslatorItem(arrayOf("sl_rSI"), "Kristijan Tkalec", github = "lapor-kris"),
    TranslatorItem(arrayOf("sl_rSI"), "Marko", weblate = "horvat.marko1993"),
    TranslatorItem(arrayOf("sl_rSI"), "BorKajin", github = "BorKajin"),
    TranslatorItem(arrayOf("sr"), "NEXI", github = "nexiRS"),
    TranslatorItem(arrayOf("sr"), "Milan Andrejić", mail = "amikia@hotmail.com"),
    TranslatorItem(arrayOf("sv"), "P.O", weblate = "mxvWhxCebxjnmLQxcIr"),
    TranslatorItem(arrayOf("sv"), "Peter Ericson", github = "noscirep"),
    TranslatorItem(arrayOf("sv"), "Luna Jernberg", github = "bittin"),
    TranslatorItem(arrayOf("sv"), "Victor Zamanian", github = "victorz"),
    TranslatorItem(arrayOf("sv"), "Innocentius0", github = "Innocentius0"),
    TranslatorItem(arrayOf("ta"), "தமிழ் நேரம்", github = "TamilNeram"),
    TranslatorItem(arrayOf("ta"), "Naveen", weblate = "naveen"),
    TranslatorItem(arrayOf("ta"), "Yogeshwar Bala", github = "Blend3rman"),
    TranslatorItem(arrayOf("th"), "Wari", github = "wwwwwwari"),
    TranslatorItem(arrayOf("th"), "ACHN SYPS", github = "achn-syps"),
    TranslatorItem(arrayOf("tr"), "Mehmet Saygin Yilmaz", mail = "memcos@gmail.com"),
    TranslatorItem(arrayOf("tr"), "Ali D.", mail = "siyaha@gmail.com"),
    TranslatorItem(arrayOf("tr"), "metezd", weblate = "metezd"),
    TranslatorItem(arrayOf("tr"), "Furkan Karcıoğlu", github = "frknkrc44"),
    TranslatorItem(arrayOf("tr"), "abfreeman", weblate = "abfreeman"),
    TranslatorItem(arrayOf("tr"), "Oğuz Ersen", github = "oersen"),
    TranslatorItem(arrayOf("tr"), "Önder Nuray", github = "ondern"),
    TranslatorItem(arrayOf("tr"), "AbdullahManaz", github = "AbdullahManaz"),
    TranslatorItem(arrayOf("tr"), "ODK", weblate = "odk0160"),
    TranslatorItem(arrayOf("tr"), "polarwood", weblate = "polarwood"),
    TranslatorItem(arrayOf("tr"), "Salih Efe Ergür", github = "salihefee"),
    TranslatorItem(arrayOf("uk"), "Cod3d.", github = "Cod3dDOT"),
    TranslatorItem(arrayOf("uk"), "Skrripy", weblate = "Skrripy"),
    TranslatorItem(arrayOf("uk"), "Fqwe1", weblate = "Fqwe1"),
    TranslatorItem(arrayOf("uk"), "Сергій", github = "Serega124"),
    TranslatorItem(arrayOf("uk"), "Maksim2005UA", github = "Maksim2005UA"),
    TranslatorItem(arrayOf("uk"), "Do you know my name?", weblate = "Anonymous2676"),
    TranslatorItem(arrayOf("uk", "be", "ru"), "vertekplus", github = "vertekplus"),
    TranslatorItem(arrayOf("vi"), "minb", weblate = "minbe"),
    TranslatorItem(arrayOf("vi"), "Fairy", weblate = "Fairy"),
    TranslatorItem(arrayOf("vi"), "ngocanhtve", github = "ngocanhtve"),
    TranslatorItem(arrayOf("vi"), "minh3339", github = "minh3339"),
    TranslatorItem(arrayOf("vi"), "Hoang-Ender", github = "Hoang-Ender"),
    TranslatorItem(arrayOf("zh_rCN", "zh_rHK", "zh_rTW", "en"), "WangDaYeeeeee", github = "WangDaYeeeeee"),
    TranslatorItem(arrayOf("zh_rCN"), "Coelacanthus", github = "CoelacanthusHex"),
    TranslatorItem(arrayOf("zh_rCN"), "御坂13766号", github = "misaka-13766"),
    TranslatorItem(arrayOf("zh_rCN"), "losky2987", github = "losky2987"),
    TranslatorItem(arrayOf("zh_rCN"), "thdcloud", github = "thdcloud"),
    TranslatorItem(arrayOf("zh_rCN", "zh_rTW"), "thaumiel9", github = "thaumiel9"),
    TranslatorItem(arrayOf("zh_rCN"), "tomac4t", github = "tomac4t"),
    TranslatorItem(arrayOf("zh_rHK", "zh_rTW"), "abc0922001", github = "abc0922001"),
    TranslatorItem(arrayOf("zh_rCN"), "大王叫我来巡山", weblate = "hamburger2048"),
    TranslatorItem(arrayOf("zh_rCN"), "hugoalh", github = "hugoalh"),
    TranslatorItem(arrayOf("zh_rCN"), "cloudfish", github = "cloudfish"),
    TranslatorItem(arrayOf("zh_rCN"), "WorldNulptr", github = "WorldNulptr"),
    TranslatorItem(arrayOf("zh_rHK", "zh_rTW"), "chunshek", github = "chunshek"),
    TranslatorItem(arrayOf("ja", "zh_rCN", "zh_rHK", "zh_rTW"), "天ツ風", github = "Yibuki"),
    TranslatorItem(
        arrayOf("zh_rHK", "zh_rTW", "be", "bg", "bs", "de", "el", "en", "eu", "it", "ja", "mk", "pl", "ru", "uk", "vi"),
        "kilimov25",
        github = "kilimov25"
    )
)
