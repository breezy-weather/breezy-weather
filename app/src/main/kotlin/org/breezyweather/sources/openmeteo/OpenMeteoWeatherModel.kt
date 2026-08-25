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

package org.breezyweather.sources.openmeteo

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.options.BaseEnum
import org.breezyweather.common.utils.UnitUtils

/**
 * List from:
 * https://open-meteo.com/en/docs/
 *
 * Up-to-date as of 2025-10-10
 */
enum class OpenMeteoWeatherModel(
    override val id: String,
) : BaseEnum {
    BEST_MATCH("best_match"),

    ECMWF_IFS("ecmwf_ifs"),
    ECMWF_IFS025("ecmwf_ifs025"),
    ECMWF_AIFS025_SINGLE("ecmwf_aifs025_single"),
    CMA_GRAPES_GLOBAL("cma_grapes_global"),
    BOM_ACCESS_GLOBAL("bom_access_global"),

    NCEP_GFS_SEAMLESS("ncep_gfs_seamless"),
    NCEP_GFS_GLOBAL("ncep_gfs_global"),
    NCEP_HRRR_US_CONUS("ncep_hrrr_conus"),
    NCEP_NBM_US_CONUS("ncep_nbm_conus"),
    NCEP_NAM_US_CONUS("ncep_nam_conus"),
    NCEP_AIGFS("ncep_aigfs025"),
    NCEP_HGEFS_ENSEMBLE_MEAN("ncep_hgefs025_ensemble_mean"),

    JMA_SEAMLESS("jma_seamless"),
    JMA_MSM("jma_msm"),
    JMA_GSM("jma_gsm"),

    KMA_SEAMLESS("kma_seamless"),
    KMA_MSM("kma_ldps"),
    KMA_GSM("kma_gdps"),

    DWD_ICON_SEAMLESS("dwd_icon_seamless"),
    DWD_ICON_GLOBAL("dwd_icon_global"),
    DWD_ICON_EU("dwd_icon_eu"),
    DWD_ICON_D2("dwd_icon_d2"),

    CMC_GEM_SEAMLESS("cmc_gem_seamless"),
    CMC_GEM_GLOBAL("cmc_gem_gdps"),
    CMC_GEM_REGIONAL("cmc_gem_rdps"),
    CMC_GEM_HRDPS_CONTINENTAL("cmc_gem_hrdps"),
    CMC_GEM_HRDPS_WEST("cmc_gem_hrdps_west"),

    METEO_FRANCE_SEAMLESS("meteofrance_seamless"),
    METEO_FRANCE_ARPEGE_WORLD("meteofrance_arpege_world"),
    METEO_FRANCE_ARPEGE_EUROPE("meteofrance_arpege_europe"),
    METEO_FRANCE_AROME_FRANCE("meteofrance_arome_france"),
    METEO_FRANCE_AROME_FRANCE_HD("meteofrance_arome_france_hd"),

    ITALIAMETEO_ARPAE_ICON_2I("italia_meteo_arpae_icon_2i"),

    MET_NO_SEAMLESS("metno_seamless"),
    MET_NO_NORDIC("metno_nordic"),

    KNMI_SEAMLESS("knmi_seamless"),
    KNMI_HARMONIE_AROME_EUROPE("knmi_harmonie_arome_europe"),
    KNMI_HARMONIE_AROME_NETHERLANDS("knmi_harmonie_arome_netherlands"),

    DMI_SEAMLESS("dmi_seamless"),
    DMI_HARMONIE_AROME_EUROPE("dmi_harmonie_arome_europe"),

    UKMO_SEAMLESS("ukmo_seamless"),
    UKMO_GLOBAL("ukmo_global_deterministic_10km"),
    UKMO_UK("ukmo_uk_deterministic_2km"),

    METEOSWISS_ICON_SEAMLESS("meteoswiss_icon_seamless"),
    METEOSWISS_ICON_CH1("meteoswiss_icon_ch1"),
    METEOSWISS_ICON_CH2("meteoswiss_icon_ch2"),

    GEOSPHERE_SEAMLESS("geosphere_seamless"),
    GEOSPHERE_AROME_AUSTRIA("geosphere_arome_austria"),

    CHMI_ALADIN_SEAMLESS("chmi_aladin_seamless"),
    CHMI_ALADIN_CENTRAL_EUROPE("chmi_aladin_central_europe_2km"),
    CHMI_ALADIN_CZ("chmi_aladin_cz_1km"),
    ;

    companion object {

        fun getInstance(
            value: String,
        ) = OpenMeteoWeatherModel.entries.firstOrNull {
            it.id == value
        }
    }

    override val valueArrayId = R.array.open_meteo_weather_models_values
    override val nameArrayId = R.array.open_meteo_weather_models

    override fun getName(context: Context) =
        UnitUtils.getName(context, this)
            .replace(
                "Best match",
                context.getString(R.string.settings_weather_source_open_meteo_weather_models_best_match)
            )
            .replace(
                "Seamless",
                context.getString(R.string.settings_weather_source_open_meteo_weather_models_seamless)
            )

    fun getDescription(context: Context): String? = if (id == "best_match") {
        context.getString(R.string.settings_weather_source_open_meteo_weather_models_best_match_description)
    } else if (id.endsWith("_seamless")) {
        context.getString(R.string.settings_weather_source_open_meteo_weather_models_seamless_description)
    } else {
        null
    }
}
