package org.breezyweather.sources.openmeteo

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.basic.models.options._basic.BaseEnum
import org.breezyweather.common.basic.models.options._basic.Utils

/**
 * List from:
 * https://open-meteo.com/en/docs/
 *
 * Up-to-date as of 2024-03-13
 */
enum class OpenMeteoWeatherModel(
    override val id: String,
    val incompatibleSources: Set<String>
): BaseEnum {
    BEST_MATCH("best_match", setOf("ecmwf_ifs04", "ecmwf_ifs025", "ecmwf_aifs025", "cma_grapes_global", "bom_access_global", "metno_nordic", "gfs_seamless", "gfs_global", "gfs_hrrr", "jma_seamless", "jma_msm", "jma_gsm", "icon_seamless", "icon_global", "icon_eu", "icon_d2", "gem_seamless", "gem_global", "gem_regional", "gem_hrdps_continental", "meteofrance_seamless", "meteofrance_arpege_world", "meteofrance_arpege_europe", "meteofrance_arome_france", "meteofrance_arome_france_hd", "arpae_cosmo_seamless", "arpae_cosmo_2i", "arpae_cosmo_2i_ruc", "arpae_cosmo_5m")),

    ECMWF_IFS04("ecmwf_ifs04", setOf("best_match")),
    ECMWF_IFS025("ecmwf_ifs025", setOf("best_match")),
    ECMWF_AIFS025("ecmwf_aifs025", setOf("best_match")),
    CMA_GRAPES_GLOBAL("cma_grapes_global", setOf("best_match")),
    BOM_ACCESS_GLOBAL("bom_access_global", setOf("best_match")),
    MET_NO_NORDIC("metno_nordic", setOf("best_match")),

    GFS_SEAMLESS("gfs_seamless", setOf("best_match", "gfs_global", "gfs_hrrr")),
    GFS_GLOBAL("gfs_global", setOf("best_match", "gfs_seamless")),
    GFS_HRRR("gfs_hrrr", setOf("best_match", "gfs_seamless")),

    JMA_SEAMLESS("jma_seamless", setOf("best_match", "jma_msm", "jma_gsm")),
    JMA_MSM("jma_msm", setOf("best_match", "jma_seamless")),
    JMA_GSM("jma_gsm", setOf("best_match", "jma_seamless")),

    ICON_SEAMLESS("icon_seamless", setOf("best_match", "icon_global", "icon_eu", "icon_d2")),
    ICON_GLOBAL("icon_global", setOf("best_match", "icon_seamless")),
    ICON_EU("icon_eu", setOf("best_match", "icon_seamless")),
    ICON_D2("icon_d2", setOf("best_match", "icon_seamless")),

    GEM_SEAMLESS("gem_seamless", setOf("best_match", "gem_global", "gem_regional", "gem_hrdps_continental")),
    GEM_GLOBAL("gem_global", setOf("best_match", "gem_seamless")),
    GEM_REGIONAL("gem_regional", setOf("best_match", "gem_seamless")),
    GEM_HRDPS_CONTINENTAL("gem_hrdps_continental", setOf("best_match", "gem_seamless")),

    METEO_FRANCE_SEAMLESS("meteofrance_seamless", setOf("best_match", "meteofrance_arpege_world", "meteofrance_arpege_europe", "meteofrance_arome_france", "meteofrance_arome_france_hd")),
    METEO_FRANCE_ARPEGE_WORLD("meteofrance_arpege_world", setOf("best_match", "meteofrance_seamless")),
    METEO_FRANCE_ARPEGE_EUROPE("meteofrance_arpege_europe", setOf("best_match", "meteofrance_seamless")),
    METEO_FRANCE_AROME_FRANCE("meteofrance_arome_france", setOf("best_match", "meteofrance_seamless")),
    METEO_FRANCE_AROME_FRANCE_HD("meteofrance_arome_france_hd", setOf("best_match", "meteofrance_seamless")),

    ARPAE_COSMO_SEAMLESS("arpae_cosmo_seamless", setOf("best_match", "arpae_cosmo_2i", "arpae_cosmo_2i_ruc", "arpae_cosmo_5m")),
    ARPAE_COSMO_2I("arpae_cosmo_2i", setOf("best_match", "arpae_cosmo_seamless")),
    ARPAE_COSMO_2I_RUC("arpae_cosmo_2i_ruc", setOf("best_match", "arpae_cosmo_seamless")),
    ARPAE_COSMO_5M("arpae_cosmo_5m", setOf("best_match", "arpae_cosmo_seamless"));

    companion object {

        fun getInstance(
            value: String
        ) = when (value) {
            "best_match" -> BEST_MATCH

            "ecmwf_ifs04" -> ECMWF_IFS04
            "ecmwf_ifs025" -> ECMWF_IFS025
            "ecmwf_aifs025" -> ECMWF_AIFS025
            "cma_grapes_global" -> CMA_GRAPES_GLOBAL
            "bom_access_global" -> BOM_ACCESS_GLOBAL
            "metno_nordic" -> MET_NO_NORDIC

            "gfs_seamless" -> GFS_SEAMLESS
            "gfs_global" -> GFS_GLOBAL
            "gfs_hrrr" -> GFS_HRRR

            "jma_seamless" -> JMA_SEAMLESS
            "jma_msm" -> JMA_MSM
            "jma_gsm" -> JMA_GSM

            "icon_seamless" -> ICON_SEAMLESS
            "icon_global" -> ICON_GLOBAL
            "icon_eu" -> ICON_EU
            "icon_d2" -> ICON_D2

            "gem_seamless" -> GEM_SEAMLESS
            "gem_global" -> GEM_GLOBAL
            "gem_regional" -> GEM_REGIONAL
            "gem_hrdps_continental" -> GEM_HRDPS_CONTINENTAL

            "meteofrance_seamless" -> METEO_FRANCE_SEAMLESS
            "meteofrance_arpege_world" -> METEO_FRANCE_ARPEGE_WORLD
            "meteofrance_arpege_europe" -> METEO_FRANCE_ARPEGE_EUROPE
            "meteofrance_arome_france" -> METEO_FRANCE_AROME_FRANCE
            "meteofrance_arome_france_hd" -> METEO_FRANCE_AROME_FRANCE_HD

            "arpae_cosmo_seamless" -> ARPAE_COSMO_SEAMLESS
            "arpae_cosmo_2i" -> ARPAE_COSMO_2I
            "arpae_cosmo_2i_ruc" -> ARPAE_COSMO_2I_RUC
            "arpae_cosmo_5m" -> ARPAE_COSMO_5M

            else -> null
        }
    }

    override val valueArrayId = R.array.open_meteo_weather_models_values
    override val nameArrayId = R.array.open_meteo_weather_models

    override fun getName(context: Context) =
        Utils.getName(context, this)
            .replace("Best match", context.getString(R.string.settings_weather_source_open_meteo_weather_models_best_match))
            .replace("Seamless", context.getString(R.string.settings_weather_source_open_meteo_weather_models_seamless))

    fun getDescription(context: Context): String? = if (id == "best_match") {
        context.getString(R.string.settings_weather_source_open_meteo_weather_models_best_match_description)
    } else if (id.endsWith("_seamless")) {
        context.getString(R.string.settings_weather_source_open_meteo_weather_models_seamless_description)
    } else null
}