package org.breezyweather.sources.jma

import breezyweather.domain.weather.model.WeatherCode
import org.breezyweather.R

val JMA_DAILY_WEATHER_CODES = mapOf<String, List<WeatherCode>>(
    "100" to listOf( // 晴 CLEAR
        WeatherCode.CLEAR,
        WeatherCode.CLEAR
    ),
    "101" to listOf( // 晴時々曇 PARTLY CLOUDY
        WeatherCode.PARTLY_CLOUDY,
        WeatherCode.PARTLY_CLOUDY
    ),
    "102" to listOf( // 晴一時雨 CLEAR, OCCASIONAL SCATTERED SHOWERS
        WeatherCode.RAIN,
        WeatherCode.RAIN
    ),
    "103" to listOf( // 晴時々雨 CLEAR, FREQUENT SCATTERED SHOWERS
        WeatherCode.RAIN,
        WeatherCode.RAIN
    ),
    "104" to listOf( // 晴一時雪 CLEAR, SNOW FLURRIES
        WeatherCode.SNOW,
        WeatherCode.SNOW
    ),
    "105" to listOf( // 晴時々雪 CLEAR, FREQUENT SNOW FLURRIES
        WeatherCode.SNOW,
        WeatherCode.SNOW
    ),
    "106" to listOf( // 晴一時雨か雪 CLEAR, OCCASIONAL SCATTERED SHOWERS OR SNOW FLURRIES
        WeatherCode.SLEET,
        WeatherCode.SLEET
    ),
    "107" to listOf( // 晴時々雨か雪 CLEAR, FREQUENT SCATTERED SHOWERS OR SNOW FLURRIES
        WeatherCode.SLEET,
        WeatherCode.SLEET
    ),
    "108" to listOf( // 晴一時雨か雷雨 CLEAR, OCCASIONAL SCATTERED SHOWERS AND/OR THUNDER
        WeatherCode.THUNDERSTORM,
        WeatherCode.THUNDERSTORM
    ),
    "110" to listOf( // 晴後時々曇 CLEAR, PARTLY CLOUDY LATER
        WeatherCode.CLEAR,
        WeatherCode.PARTLY_CLOUDY
    ),
    "111" to listOf( // 晴後曇 CLEAR, CLOUDY LATER
        WeatherCode.CLEAR,
        WeatherCode.CLOUDY
    ),
    "112" to listOf( // 晴後一時雨 CLEAR, OCCASIONAL SCATTERED SHOWERS LATER
        WeatherCode.CLEAR,
        WeatherCode.RAIN
    ),
    "113" to listOf( // 晴後時々雨 CLEAR, FREQUENT SCATTERED SHOWERS LATER
        WeatherCode.CLEAR,
        WeatherCode.RAIN
    ),
    "114" to listOf( // 晴後雨 CLEAR,RAIN LATER
        WeatherCode.CLEAR,
        WeatherCode.RAIN
    ),
    "115" to listOf( // 晴後一時雪 CLEAR, OCCASIONAL SNOW FLURRIES LATER
        WeatherCode.CLEAR,
        WeatherCode.SNOW
    ),
    "116" to listOf( // 晴後時々雪 CLEAR, FREQUENT SNOW FLURRIES LATER
        WeatherCode.CLEAR,
        WeatherCode.SNOW
    ),
    "117" to listOf( // 晴後雪 CLEAR,SNOW LATER
        WeatherCode.CLEAR,
        WeatherCode.SNOW
    ),
    "118" to listOf( // 晴後雨か雪 CLEAR, RAIN OR SNOW LATER
        WeatherCode.CLEAR,
        WeatherCode.SLEET
    ),
    "119" to listOf( // 晴後雨か雷雨 CLEAR, RAIN AND/OR THUNDER LATER
        WeatherCode.CLEAR,
        WeatherCode.THUNDERSTORM
    ),
    "120" to listOf( // 晴朝夕一時雨 OCCASIONAL SCATTERED SHOWERS IN THE MORNING AND EVENING, CLEAR DURING THE DAY
        WeatherCode.CLEAR,
        WeatherCode.RAIN
    ),
    "121" to listOf( // 晴朝の内一時雨 OCCASIONAL SCATTERED SHOWERS IN THE MORNING, CLEAR DURING THE DAY
        WeatherCode.RAIN,
        WeatherCode.CLEAR
    ),
    "122" to listOf( // 晴夕方一時雨 CLEAR, OCCASIONAL SCATTERED SHOWERS IN THE EVENING
        WeatherCode.CLEAR,
        WeatherCode.RAIN
    ),
    "123" to listOf( // 晴山沿い雷雨 CLEAR IN THE PLAINS, RAIN AND THUNDER NEAR MOUTAINOUS AREAS
        WeatherCode.CLEAR,
        WeatherCode.CLEAR
    ),
    "124" to listOf( // 晴山沿い雪 CLEAR IN THE PLAINS, SNOW NEAR MOUTAINOUS AREAS
        WeatherCode.CLEAR,
        WeatherCode.CLEAR
    ),
    "125" to listOf( // 晴午後は雷雨 CLEAR, RAIN AND THUNDER IN THE AFTERNOON
        WeatherCode.THUNDERSTORM,
        WeatherCode.CLEAR
    ),
    "126" to listOf( // 晴昼頃から雨 CLEAR, RAIN IN THE AFTERNOON
        WeatherCode.RAIN,
        WeatherCode.CLEAR
    ),
    "127" to listOf( // 晴夕方から雨 CLEAR, RAIN IN THE EVENING
        WeatherCode.CLEAR,
        WeatherCode.RAIN
    ),
    "128" to listOf( // 晴夜は雨 CLEAR, RAIN IN THE NIGHT
        WeatherCode.CLEAR,
        WeatherCode.RAIN
    ),
    "130" to listOf( // 朝の内霧後晴 FOG IN THE MORNING, CLEAR LATER
        WeatherCode.FOG,
        WeatherCode.CLEAR
    ),
    "131" to listOf( // 晴明け方霧 FOG AROUND DAWN, CLEAR LATER
        WeatherCode.FOG,
        WeatherCode.CLEAR
    ),
    "132" to listOf( // 晴朝夕曇 CLOUDY IN THE MORNING AND EVENING, CLEAR DURING THE DAY
        WeatherCode.PARTLY_CLOUDY,
        WeatherCode.CLOUDY
    ),
    "140" to listOf( // 晴時々雨で雷を伴う CLEAR, FREQUENT SCATTERED SHOWERS AND THUNDER
        WeatherCode.THUNDERSTORM,
        WeatherCode.THUNDERSTORM
    ),
    "160" to listOf( // 晴一時雪か雨 CLEAR, SNOW FLURRIES OR OCCASIONAL SCATTERED SHOWERS
        WeatherCode.SLEET,
        WeatherCode.SLEET
    ),
    "170" to listOf( // 晴時々雪か雨 CLEAR, FREQUENT SNOW FLURRIES OR SCATTERED SHOWERS
        WeatherCode.SLEET,
        WeatherCode.SLEET
    ),
    "181" to listOf( // 晴後雪か雨 CLEAR, SNOW OR RAIN LATER
        WeatherCode.CLEAR,
        WeatherCode.SLEET
    ),
    "200" to listOf( // 曇 CLOUDY
        WeatherCode.CLOUDY,
        WeatherCode.CLOUDY
    ),
    "201" to listOf( // 曇時々晴 MOSTLY CLOUDY
        WeatherCode.PARTLY_CLOUDY,
        WeatherCode.PARTLY_CLOUDY
    ),
    "202" to listOf( // 曇一時雨 CLOUDY, OCCASIONAL SCATTERED SHOWERS
        WeatherCode.RAIN,
        WeatherCode.RAIN
    ),
    "203" to listOf( // 曇時々雨 CLOUDY, FREQUENT SCATTERED SHOWERS
        WeatherCode.RAIN,
        WeatherCode.RAIN
    ),
    "204" to listOf( // 曇一時雪 CLOUDY, OCCASIONAL SNOW FLURRIES
        WeatherCode.SNOW,
        WeatherCode.SNOW
    ),
    "205" to listOf( // 曇時々雪 CLOUDY FREQUENT SNOW FLURRIES
        WeatherCode.SNOW,
        WeatherCode.SNOW
    ),
    "206" to listOf( // 曇一時雨か雪 CLOUDY, OCCASIONAL SCATTERED SHOWERS OR SNOW FLURRIES
        WeatherCode.SNOW,
        WeatherCode.SNOW
    ),
    "207" to listOf( // 曇時々雨か雪 CLOUDY, FREQUENT SCCATERED SHOWERS OR SNOW FLURRIES
        WeatherCode.SLEET,
        WeatherCode.SLEET
    ),
    "208" to listOf( // 曇一時雨か雷雨 CLOUDY, OCCASIONAL SCATTERED SHOWERS AND/OR THUNDER
        WeatherCode.THUNDERSTORM,
        WeatherCode.THUNDERSTORM
    ),
    "209" to listOf( // 霧 FOG
        WeatherCode.FOG,
        WeatherCode.FOG
    ),
    "210" to listOf( // 曇後時々晴 CLOUDY, PARTLY CLOUDY LATER
        WeatherCode.CLOUDY,
        WeatherCode.PARTLY_CLOUDY
    ),
    "211" to listOf( // 曇後晴 CLOUDY, CLEAR LATER
        WeatherCode.CLOUDY,
        WeatherCode.CLEAR
    ),
    "212" to listOf( // 曇後一時雨 CLOUDY, OCCASIONAL SCATTERED SHOWERS LATER
        WeatherCode.CLOUDY,
        WeatherCode.RAIN
    ),
    "213" to listOf( // 曇後時々雨 CLOUDY, FREQUENT SCATTERED SHOWERS LATER
        WeatherCode.CLOUDY,
        WeatherCode.RAIN
    ),
    "214" to listOf( // 曇後雨 CLOUDY, RAIN LATER
        WeatherCode.CLOUDY,
        WeatherCode.RAIN
    ),
    "215" to listOf( // 曇後一時雪 CLOUDY, SNOW FLURRIES LATER
        WeatherCode.CLOUDY,
        WeatherCode.SNOW
    ),
    "216" to listOf( // 曇後時々雪 CLOUDY, FREQUENT SNOW FLURRIES LATER
        WeatherCode.CLOUDY,
        WeatherCode.SNOW
    ),
    "217" to listOf( // 曇後雪 CLOUDY, SNOW LATER
        WeatherCode.CLOUDY,
        WeatherCode.SNOW
    ),
    "218" to listOf( // 曇後雨か雪 CLOUDY, RAIN OR SNOW LATER
        WeatherCode.CLOUDY,
        WeatherCode.SLEET
    ),
    "219" to listOf( // 曇後雨か雷雨 CLOUDY, RAIN AND/OR THUNDER LATER
        WeatherCode.CLOUDY,
        WeatherCode.THUNDERSTORM
    ),
    "220" to listOf( // 曇朝夕一時雨 OCCASIONAL SCCATERED SHOWERS IN THE MORNING AND EVENING, CLOUDY DURING THE DAY
        WeatherCode.RAIN,
        WeatherCode.RAIN
    ),
    "221" to listOf( // 曇朝の内一時雨 CLOUDY OCCASIONAL SCCATERED SHOWERS IN THE MORNING
        WeatherCode.RAIN,
        WeatherCode.CLOUDY
    ),
    "222" to listOf( // 曇夕方一時雨 CLOUDY, OCCASIONAL SCCATERED SHOWERS IN THE EVENING
        WeatherCode.CLOUDY,
        WeatherCode.RAIN
    ),
    "223" to listOf( // 曇日中時々晴 CLOUDY IN THE MORNING AND EVENING, PARTLY CLOUDY DURING THE DAY,
        WeatherCode.PARTLY_CLOUDY,
        WeatherCode.CLOUDY
    ),
    "224" to listOf( // 曇昼頃から雨 CLOUDY, RAIN IN THE AFTERNOON
        WeatherCode.RAIN,
        WeatherCode.CLOUDY
    ),
    "225" to listOf( // 曇夕方から雨 CLOUDY, RAIN IN THE EVENING
        WeatherCode.CLOUDY,
        WeatherCode.RAIN
    ),
    "226" to listOf( // 曇夜は雨 CLOUDY, RAIN IN THE NIGHT
        WeatherCode.CLOUDY,
        WeatherCode.RAIN
    ),
    "228" to listOf( // 曇昼頃から雪 CLOUDY, SNOW IN THE AFTERNOON
        WeatherCode.SNOW,
        WeatherCode.CLOUDY
    ),
    "229" to listOf( // 曇夕方から雪 CLOUDY, SNOW IN THE EVENING
        WeatherCode.CLOUDY,
        WeatherCode.SNOW
    ),
    "230" to listOf( // 曇夜は雪 CLOUDY, SNOW IN THE NIGHT
        WeatherCode.CLOUDY,
        WeatherCode.SNOW
    ),
    "231" to listOf( // 曇海上海岸は霧か霧雨 CLOUDY, FOG OR DRIZZLING ON THE SEA AND NEAR SEASHORE
        WeatherCode.CLOUDY,
        WeatherCode.CLOUDY
    ),
    "240" to listOf( // 曇時々雨で雷を伴う CLOUDY, FREQUENT SCCATERED SHOWERS AND THUNDER
        WeatherCode.THUNDERSTORM,
        WeatherCode.THUNDERSTORM
    ),
    "250" to listOf( // 曇時々雪で雷を伴う CLOUDY, FREQUENT SNOW AND THUNDER
        WeatherCode.SNOW,
        WeatherCode.SNOW
    ),
    "260" to listOf( // 曇一時雪か雨 CLOUDY, SNOW FLURRIES OR OCCASIONAL SCATTERED SHOWERS
        WeatherCode.SLEET,
        WeatherCode.SLEET
    ),
    "270" to listOf( // 曇時々雪か雨 CLOUDY, FREQUENT SNOW FLURRIES OR SCATTERED SHOWERS
        WeatherCode.SLEET,
        WeatherCode.SLEET
    ),
    "281" to listOf( // 曇後雪か雨 CLOUDY, SNOW OR RAIN LATER
        WeatherCode.CLOUDY,
        WeatherCode.SLEET
    ),
    "300" to listOf( // 雨 RAIN
        WeatherCode.RAIN,
        WeatherCode.RAIN
    ),
    "301" to listOf( // 雨時々晴 RAIN, PARTLY CLOUDY
        WeatherCode.RAIN,
        WeatherCode.RAIN
    ),
    "302" to listOf( // 雨時々止む SHOWERS THROUGHOUT THE DAY
        WeatherCode.RAIN,
        WeatherCode.RAIN
    ),
    "303" to listOf( // 雨時々雪 RAIN,FREQUENT SNOW FLURRIES
        WeatherCode.SLEET,
        WeatherCode.SLEET
    ),
    "304" to listOf( // 雨か雪 RAINORSNOW
        WeatherCode.SLEET,
        WeatherCode.SLEET
    ),
    "306" to listOf( // 大雨 HEAVYRAIN
        WeatherCode.RAIN,
        WeatherCode.RAIN
    ),
    "308" to listOf( // 雨で暴風を伴う RAINSTORM
        WeatherCode.RAIN,
        WeatherCode.RAIN
    ),
    "309" to listOf( // 雨一時雪 RAIN,OCCASIONAL SNOW
        WeatherCode.SLEET,
        WeatherCode.SLEET
    ),
    "311" to listOf( // 雨後晴 RAIN,CLEAR LATER
        WeatherCode.RAIN,
        WeatherCode.CLEAR
    ),
    "313" to listOf( // 雨後曇 RAIN,CLOUDY LATER
        WeatherCode.RAIN,
        WeatherCode.CLOUDY
    ),
    "314" to listOf( // 雨後時々雪 RAIN, FREQUENT SNOW FLURRIES LATER
        WeatherCode.RAIN,
        WeatherCode.SNOW
    ),
    "315" to listOf( // 雨後雪 RAIN,SNOW LATER
        WeatherCode.RAIN,
        WeatherCode.SNOW
    ),
    "316" to listOf( // 雨か雪後晴 RAIN OR SNOW, CLEAR LATER
        WeatherCode.SLEET,
        WeatherCode.CLEAR
    ),
    "317" to listOf( // 雨か雪後曇 RAIN OR SNOW, CLOUDY LATER
        WeatherCode.SLEET,
        WeatherCode.CLOUDY
    ),
    "320" to listOf( // 朝の内雨後晴 RAIN IN THE MORNING, CLEAR LATER
        WeatherCode.RAIN,
        WeatherCode.CLEAR
    ),
    "321" to listOf( // 朝の内雨後曇 RAIN IN THE MORNING, CLOUDY LATER
        WeatherCode.RAIN,
        WeatherCode.CLOUDY
    ),
    "322" to listOf( // 雨朝晩一時雪 OCCASIONAL SNOW IN THE MORNING AND EVENING, RAIN DURING THE DAY
        WeatherCode.RAIN,
        WeatherCode.SNOW
    ),
    "323" to listOf( // 雨昼頃から晴 RAIN, CLEAR IN THE AFTERNOON
        WeatherCode.RAIN,
        WeatherCode.CLEAR
    ),
    "324" to listOf( // 雨夕方から晴 RAIN, CLEAR IN THE EVENING
        WeatherCode.RAIN,
        WeatherCode.CLEAR
    ),
    "325" to listOf( // 雨夜は晴 RAIN, CLEAR IN THE NIGHT
        WeatherCode.RAIN,
        WeatherCode.CLEAR
    ),
    "326" to listOf( // 雨夕方から雪 RAIN, SNOW IN THE EVENING
        WeatherCode.RAIN,
        WeatherCode.SNOW
    ),
    "327" to listOf( // 雨夜は雪 RAIN,SNOW IN THE NIGHT
        WeatherCode.RAIN,
        WeatherCode.SNOW
    ),
    "328" to listOf( // 雨一時強く降る RAIN, EXPECT OCCASIONAL HEAVY RAINFALL
        WeatherCode.RAIN,
        WeatherCode.RAIN
    ),
    "329" to listOf( // 雨一時みぞれ RAIN, OCCASIONAL SLEET
        WeatherCode.SLEET,
        WeatherCode.SLEET
    ),
    "340" to listOf( // 雪か雨 SNOWORRAIN
        WeatherCode.SLEET,
        WeatherCode.SLEET
    ),
    "350" to listOf( // 雨で雷を伴う RAIN AND THUNDER
        WeatherCode.THUNDERSTORM,
        WeatherCode.THUNDERSTORM
    ),
    "361" to listOf( // 雪か雨後晴 SNOW OR RAIN, CLEAR LATER
        WeatherCode.SLEET,
        WeatherCode.CLEAR
    ),
    "371" to listOf( // 雪か雨後曇 SNOW OR RAIN, CLOUDY LATER
        WeatherCode.SLEET,
        WeatherCode.CLOUDY
    ),
    "400" to listOf( // 雪 SNOW
        WeatherCode.SNOW,
        WeatherCode.SNOW
    ),
    "401" to listOf( // 雪時々晴 SNOW, FREQUENT CLEAR
        WeatherCode.SNOW,
        WeatherCode.SNOW
    ),
    "402" to listOf( // 雪時々止む SNOWTHROUGHOUT THE DAY
        WeatherCode.SNOW,
        WeatherCode.SNOW
    ),
    "403" to listOf( // 雪時々雨 SNOW,FREQUENT SCCATERED SHOWERS
        WeatherCode.SLEET,
        WeatherCode.SLEET
    ),
    "405" to listOf( // 大雪 HEAVYSNOW
        WeatherCode.SNOW,
        WeatherCode.SNOW
    ),
    "406" to listOf( // 風雪強い SNOWSTORM
        WeatherCode.SNOW,
        WeatherCode.SNOW
    ),
    "407" to listOf( // 暴風雪 HEAVYSNOWSTORM
        WeatherCode.SNOW,
        WeatherCode.SNOW
    ),
    "409" to listOf( // 雪一時雨 SNOW, OCCASIONAL SCCATERED SHOWERS
        WeatherCode.SLEET,
        WeatherCode.SLEET
    ),
    "411" to listOf( // 雪後晴 SNOW,CLEAR LATER
        WeatherCode.SNOW,
        WeatherCode.CLEAR
    ),
    "413" to listOf( // 雪後曇 SNOW,CLOUDY LATER
        WeatherCode.SNOW,
        WeatherCode.CLOUDY
    ),
    "414" to listOf( // 雪後雨 SNOW,RAIN LATER
        WeatherCode.SNOW,
        WeatherCode.RAIN
    ),
    "420" to listOf( // 朝の内雪後晴 SNOW IN THE MORNING, CLEAR LATER
        WeatherCode.SNOW,
        WeatherCode.CLEAR
    ),
    "421" to listOf( // 朝の内雪後曇 SNOW IN THE MORNING, CLOUDY LATER
        WeatherCode.SNOW,
        WeatherCode.CLOUDY
    ),
    "422" to listOf( // 雪昼頃から雨 SNOW, RAIN IN THE AFTERNOON
        WeatherCode.SNOW,
        WeatherCode.RAIN
    ),
    "423" to listOf( // 雪夕方から雨 SNOW, RAIN IN THE EVENING
        WeatherCode.SNOW,
        WeatherCode.RAIN
    ),
    "425" to listOf( // 雪一時強く降る SNOW, EXPECT OCCASIONAL HEAVY SNOWFALL
        WeatherCode.SNOW,
        WeatherCode.SNOW
    ),
    "426" to listOf( // 雪後みぞれ SNOW, SLEET LATER
        WeatherCode.SNOW,
        WeatherCode.SLEET
    ),
    "427" to listOf( // 雪一時みぞれ SNOW, OCCASIONAL SLEET
        WeatherCode.SLEET,
        WeatherCode.SLEET
    ),
    "450" to listOf( // 雪で雷を伴う SNOW AND THUNDER
        WeatherCode.SNOW,
        WeatherCode.SNOW
    )
)

val JMA_DAILY_WEATHER_TEXTS = mapOf<String, List<Int>>(
    "100" to listOf( // 晴 CLEAR
        R.string.common_weather_text_clear_sky,
        R.string.common_weather_text_clear_sky
    ),
    "101" to listOf( // 晴時々曇 PARTLY CLOUDY
        R.string.common_weather_text_partly_cloudy,
        R.string.common_weather_text_partly_cloudy
    ),
    "102" to listOf( // 晴一時雨 CLEAR, OCCASIONAL SCATTERED SHOWERS
        R.string.common_weather_text_rain_showers,
        R.string.common_weather_text_rain_showers
    ),
    "103" to listOf( // 晴時々雨 CLEAR, FREQUENT SCATTERED SHOWERS
        R.string.common_weather_text_rain_showers,
        R.string.common_weather_text_rain_showers
    ),
    "104" to listOf( // 晴一時雪 CLEAR, SNOW FLURRIES
        R.string.common_weather_text_snow_showers,
        R.string.common_weather_text_snow_showers
    ),
    "105" to listOf( // 晴時々雪 CLEAR, FREQUENT SNOW FLURRIES
        R.string.common_weather_text_snow_showers,
        R.string.common_weather_text_snow_showers
    ),
    "106" to listOf( // 晴一時雨か雪 CLEAR, OCCASIONAL SCATTERED SHOWERS OR SNOW FLURRIES
        R.string.common_weather_text_rain_snow_mixed_showers,
        R.string.common_weather_text_rain_snow_mixed_showers
    ),
    "107" to listOf( // 晴時々雨か雪 CLEAR, FREQUENT SCATTERED SHOWERS OR SNOW FLURRIES
        R.string.common_weather_text_rain_snow_mixed_showers,
        R.string.common_weather_text_rain_snow_mixed_showers
    ),
    "108" to listOf( // 晴一時雨か雷雨 CLEAR, OCCASIONAL SCATTERED SHOWERS AND/OR THUNDER
        R.string.weather_kind_thunderstorm,
        R.string.weather_kind_thunderstorm
    ),
    "110" to listOf( // 晴後時々曇 CLEAR, PARTLY CLOUDY LATER
        R.string.common_weather_text_clear_sky,
        R.string.common_weather_text_partly_cloudy
    ),
    "111" to listOf( // 晴後曇 CLEAR, CLOUDY LATER
        R.string.common_weather_text_clear_sky,
        R.string.common_weather_text_cloudy
    ),
    "112" to listOf( // 晴後一時雨 CLEAR, OCCASIONAL SCATTERED SHOWERS LATER
        R.string.common_weather_text_clear_sky,
        R.string.common_weather_text_rain_showers
    ),
    "113" to listOf( // 晴後時々雨 CLEAR, FREQUENT SCATTERED SHOWERS LATER
        R.string.common_weather_text_clear_sky,
        R.string.common_weather_text_rain_showers
    ),
    "114" to listOf( // 晴後雨 CLEAR,RAIN LATER
        R.string.common_weather_text_clear_sky,
        R.string.common_weather_text_rain
    ),
    "115" to listOf( // 晴後一時雪 CLEAR, OCCASIONAL SNOW FLURRIES LATER
        R.string.common_weather_text_clear_sky,
        R.string.common_weather_text_snow_showers
    ),
    "116" to listOf( // 晴後時々雪 CLEAR, FREQUENT SNOW FLURRIES LATER
        R.string.common_weather_text_clear_sky,
        R.string.common_weather_text_snow_showers
    ),
    "117" to listOf( // 晴後雪 CLEAR,SNOW LATER
        R.string.common_weather_text_clear_sky,
        R.string.common_weather_text_snow
    ),
    "118" to listOf( // 晴後雨か雪 CLEAR, RAIN OR SNOW LATER
        R.string.common_weather_text_clear_sky,
        R.string.common_weather_text_rain_snow_mixed
    ),
    "119" to listOf( // 晴後雨か雷雨 CLEAR, RAIN AND/OR THUNDER LATER
        R.string.common_weather_text_clear_sky,
        R.string.weather_kind_thunderstorm
    ),
    "120" to listOf( // 晴朝夕一時雨 OCCASIONAL SCATTERED SHOWERS IN THE MORNING AND EVENING, CLEAR DURING THE DAY
        R.string.common_weather_text_clear_sky,
        R.string.common_weather_text_rain_showers
    ),
    "121" to listOf( // 晴朝の内一時雨 OCCASIONAL SCATTERED SHOWERS IN THE MORNING, CLEAR DURING THE DAY
        R.string.common_weather_text_rain_showers,
        R.string.common_weather_text_clear_sky
    ),
    "122" to listOf( // 晴夕方一時雨 CLEAR, OCCASIONAL SCATTERED SHOWERS IN THE EVENING
        R.string.common_weather_text_clear_sky,
        R.string.common_weather_text_rain_showers
    ),
    "123" to listOf( // 晴山沿い雷雨 CLEAR IN THE PLAINS, RAIN AND THUNDER NEAR MOUTAINOUS AREAS
        R.string.common_weather_text_clear_sky,
        R.string.common_weather_text_clear_sky
    ),
    "124" to listOf( // 晴山沿い雪 CLEAR IN THE PLAINS, SNOW NEAR MOUTAINOUS AREAS
        R.string.common_weather_text_clear_sky,
        R.string.common_weather_text_clear_sky
    ),
    "125" to listOf( // 晴午後は雷雨 CLEAR, RAIN AND THUNDER IN THE AFTERNOON
        R.string.weather_kind_thunderstorm,
        R.string.common_weather_text_clear_sky
    ),
    "126" to listOf( // 晴昼頃から雨 CLEAR, RAIN IN THE AFTERNOON
        R.string.common_weather_text_rain,
        R.string.common_weather_text_clear_sky
    ),
    "127" to listOf( // 晴夕方から雨 CLEAR, RAIN IN THE EVENING
        R.string.common_weather_text_clear_sky,
        R.string.common_weather_text_rain
    ),
    "128" to listOf( // 晴夜は雨 CLEAR, RAIN IN THE NIGHT
        R.string.common_weather_text_clear_sky,
        R.string.common_weather_text_rain
    ),
    "130" to listOf( // 朝の内霧後晴 FOG IN THE MORNING, CLEAR LATER
        R.string.common_weather_text_fog,
        R.string.common_weather_text_clear_sky
    ),
    "131" to listOf( // 晴明け方霧 FOG AROUND DAWN, CLEAR LATER
        R.string.common_weather_text_fog,
        R.string.common_weather_text_clear_sky
    ),
    "132" to listOf( // 晴朝夕曇 CLOUDY IN THE MORNING AND EVENING, CLEAR DURING THE DAY
        R.string.common_weather_text_partly_cloudy,
        R.string.common_weather_text_cloudy
    ),
    "140" to listOf( // 晴時々雨で雷を伴う CLEAR, FREQUENT SCATTERED SHOWERS AND THUNDER
        R.string.weather_kind_thunderstorm,
        R.string.weather_kind_thunderstorm
    ),
    "160" to listOf( // 晴一時雪か雨 CLEAR, SNOW FLURRIES OR OCCASIONAL SCATTERED SHOWERS
        R.string.common_weather_text_rain_snow_mixed_showers,
        R.string.common_weather_text_rain_snow_mixed_showers
    ),
    "170" to listOf( // 晴時々雪か雨 CLEAR, FREQUENT SNOW FLURRIES OR SCATTERED SHOWERS
        R.string.common_weather_text_rain_snow_mixed_showers,
        R.string.common_weather_text_rain_snow_mixed_showers
    ),
    "181" to listOf( // 晴後雪か雨 CLEAR, SNOW OR RAIN LATER
        R.string.common_weather_text_clear_sky,
        R.string.common_weather_text_rain_snow_mixed
    ),
    "200" to listOf( // 曇 CLOUDY
        R.string.common_weather_text_cloudy,
        R.string.common_weather_text_cloudy
    ),
    "201" to listOf( // 曇時々晴 MOSTLY CLOUDY
        R.string.common_weather_text_partly_cloudy,
        R.string.common_weather_text_partly_cloudy
    ),
    "202" to listOf( // 曇一時雨 CLOUDY, OCCASIONAL SCATTERED SHOWERS
        R.string.common_weather_text_rain_showers,
        R.string.common_weather_text_rain_showers
    ),
    "203" to listOf( // 曇時々雨 CLOUDY, FREQUENT SCATTERED SHOWERS
        R.string.common_weather_text_rain_showers,
        R.string.common_weather_text_rain_showers
    ),
    "204" to listOf( // 曇一時雪 CLOUDY, OCCASIONAL SNOW FLURRIES
        R.string.common_weather_text_snow_showers,
        R.string.common_weather_text_snow_showers
    ),
    "205" to listOf( // 曇時々雪 CLOUDY FREQUENT SNOW FLURRIES
        R.string.common_weather_text_snow_showers,
        R.string.common_weather_text_snow_showers
    ),
    "206" to listOf( // 曇一時雨か雪 CLOUDY, OCCASIONAL SCATTERED SHOWERS OR SNOW FLURRIES
        R.string.common_weather_text_snow_showers,
        R.string.common_weather_text_snow_showers
    ),
    "207" to listOf( // 曇時々雨か雪 CLOUDY, FREQUENT SCCATERED SHOWERS OR SNOW FLURRIES
        R.string.common_weather_text_rain_snow_mixed_showers,
        R.string.common_weather_text_rain_snow_mixed_showers
    ),
    "208" to listOf( // 曇一時雨か雷雨 CLOUDY, OCCASIONAL SCATTERED SHOWERS AND/OR THUNDER
        R.string.weather_kind_thunderstorm,
        R.string.weather_kind_thunderstorm
    ),
    "209" to listOf( // 霧 FOG
        R.string.common_weather_text_fog,
        R.string.common_weather_text_fog
    ),
    "210" to listOf( // 曇後時々晴 CLOUDY, PARTLY CLOUDY LATER
        R.string.common_weather_text_cloudy,
        R.string.common_weather_text_partly_cloudy
    ),
    "211" to listOf( // 曇後晴 CLOUDY, CLEAR LATER
        R.string.common_weather_text_cloudy,
        R.string.common_weather_text_clear_sky
    ),
    "212" to listOf( // 曇後一時雨 CLOUDY, OCCASIONAL SCATTERED SHOWERS LATER
        R.string.common_weather_text_cloudy,
        R.string.common_weather_text_rain_showers
    ),
    "213" to listOf( // 曇後時々雨 CLOUDY, FREQUENT SCATTERED SHOWERS LATER
        R.string.common_weather_text_cloudy,
        R.string.common_weather_text_rain_showers
    ),
    "214" to listOf( // 曇後雨 CLOUDY, RAIN LATER
        R.string.common_weather_text_cloudy,
        R.string.common_weather_text_rain
    ),
    "215" to listOf( // 曇後一時雪 CLOUDY, SNOW FLURRIES LATER
        R.string.common_weather_text_cloudy,
        R.string.common_weather_text_snow_showers
    ),
    "216" to listOf( // 曇後時々雪 CLOUDY, FREQUENT SNOW FLURRIES LATER
        R.string.common_weather_text_cloudy,
        R.string.common_weather_text_snow_showers
    ),
    "217" to listOf( // 曇後雪 CLOUDY, SNOW LATER
        R.string.common_weather_text_cloudy,
        R.string.common_weather_text_snow
    ),
    "218" to listOf( // 曇後雨か雪 CLOUDY, RAIN OR SNOW LATER
        R.string.common_weather_text_cloudy,
        R.string.common_weather_text_rain_snow_mixed
    ),
    "219" to listOf( // 曇後雨か雷雨 CLOUDY, RAIN AND/OR THUNDER LATER
        R.string.common_weather_text_cloudy,
        R.string.weather_kind_thunderstorm
    ),
    "220" to listOf( // 曇朝夕一時雨 OCCASIONAL SCCATERED SHOWERS IN THE MORNING AND EVENING, CLOUDY DURING THE DAY
        R.string.common_weather_text_rain_showers,
        R.string.common_weather_text_rain_showers
    ),
    "221" to listOf( // 曇朝の内一時雨 CLOUDY OCCASIONAL SCCATERED SHOWERS IN THE MORNING
        R.string.common_weather_text_rain_showers,
        R.string.common_weather_text_cloudy
    ),
    "222" to listOf( // 曇夕方一時雨 CLOUDY, OCCASIONAL SCCATERED SHOWERS IN THE EVENING
        R.string.common_weather_text_cloudy,
        R.string.common_weather_text_rain_showers
    ),
    "223" to listOf( // 曇日中時々晴 CLOUDY IN THE MORNING AND EVENING, PARTLY CLOUDY DURING THE DAY,
        R.string.common_weather_text_partly_cloudy,
        R.string.common_weather_text_cloudy
    ),
    "224" to listOf( // 曇昼頃から雨 CLOUDY, RAIN IN THE AFTERNOON
        R.string.common_weather_text_rain,
        R.string.common_weather_text_cloudy
    ),
    "225" to listOf( // 曇夕方から雨 CLOUDY, RAIN IN THE EVENING
        R.string.common_weather_text_cloudy,
        R.string.common_weather_text_rain
    ),
    "226" to listOf( // 曇夜は雨 CLOUDY, RAIN IN THE NIGHT
        R.string.common_weather_text_cloudy,
        R.string.common_weather_text_rain
    ),
    "228" to listOf( // 曇昼頃から雪 CLOUDY, SNOW IN THE AFTERNOON
        R.string.common_weather_text_snow,
        R.string.common_weather_text_cloudy
    ),
    "229" to listOf( // 曇夕方から雪 CLOUDY, SNOW IN THE EVENING
        R.string.common_weather_text_cloudy,
        R.string.common_weather_text_snow
    ),
    "230" to listOf( // 曇夜は雪 CLOUDY, SNOW IN THE NIGHT
        R.string.common_weather_text_cloudy,
        R.string.common_weather_text_snow
    ),
    "231" to listOf( // 曇海上海岸は霧か霧雨 CLOUDY, FOG OR DRIZZLING ON THE SEA AND NEAR SEASHORE
        R.string.common_weather_text_cloudy,
        R.string.common_weather_text_cloudy
    ),
    "240" to listOf( // 曇時々雨で雷を伴う CLOUDY, FREQUENT SCCATERED SHOWERS AND THUNDER
        R.string.weather_kind_thunderstorm,
        R.string.weather_kind_thunderstorm
    ),
    "250" to listOf( // 曇時々雪で雷を伴う CLOUDY, FREQUENT SNOW AND THUNDER
        R.string.common_weather_text_snow,
        R.string.common_weather_text_snow
    ),
    "260" to listOf( // 曇一時雪か雨 CLOUDY, SNOW FLURRIES OR OCCASIONAL SCATTERED SHOWERS
        R.string.common_weather_text_rain_snow_mixed_showers,
        R.string.common_weather_text_rain_snow_mixed_showers
    ),
    "270" to listOf( // 曇時々雪か雨 CLOUDY, FREQUENT SNOW FLURRIES OR SCATTERED SHOWERS
        R.string.common_weather_text_rain_snow_mixed_showers,
        R.string.common_weather_text_rain_snow_mixed_showers
    ),
    "281" to listOf( // 曇後雪か雨 CLOUDY, SNOW OR RAIN LATER
        R.string.common_weather_text_cloudy,
        R.string.common_weather_text_rain_snow_mixed
    ),
    "300" to listOf( // 雨 RAIN
        R.string.common_weather_text_rain,
        R.string.common_weather_text_rain
    ),
    "301" to listOf( // 雨時々晴 RAIN, PARTLY CLOUDY
        R.string.common_weather_text_rain,
        R.string.common_weather_text_rain
    ),
    "302" to listOf( // 雨時々止む SHOWERS THROUGHOUT THE DAY
        R.string.common_weather_text_rain_showers,
        R.string.common_weather_text_rain_showers
    ),
    "303" to listOf( // 雨時々雪 RAIN,FREQUENT SNOW FLURRIES
        R.string.common_weather_text_rain_snow_mixed_showers,
        R.string.common_weather_text_rain_snow_mixed_showers
    ),
    "304" to listOf( // 雨か雪 RAINORSNOW
        R.string.common_weather_text_rain_snow_mixed,
        R.string.common_weather_text_rain_snow_mixed
    ),
    "306" to listOf( // 大雨 HEAVYRAIN
        R.string.common_weather_text_rain_heavy,
        R.string.common_weather_text_rain_heavy
    ),
    "308" to listOf( // 雨で暴風を伴う RAINSTORM
        R.string.common_weather_text_rain_heavy,
        R.string.common_weather_text_rain_heavy
    ),
    "309" to listOf( // 雨一時雪 RAIN,OCCASIONAL SNOW
        R.string.common_weather_text_rain_snow_mixed,
        R.string.common_weather_text_rain_snow_mixed
    ),
    "311" to listOf( // 雨後晴 RAIN,CLEAR LATER
        R.string.common_weather_text_rain,
        R.string.common_weather_text_clear_sky
    ),
    "313" to listOf( // 雨後曇 RAIN,CLOUDY LATER
        R.string.common_weather_text_rain,
        R.string.common_weather_text_cloudy
    ),
    "314" to listOf( // 雨後時々雪 RAIN, FREQUENT SNOW FLURRIES LATER
        R.string.common_weather_text_rain,
        R.string.common_weather_text_snow_showers
    ),
    "315" to listOf( // 雨後雪 RAIN,SNOW LATER
        R.string.common_weather_text_rain,
        R.string.common_weather_text_snow
    ),
    "316" to listOf( // 雨か雪後晴 RAIN OR SNOW, CLEAR LATER
        R.string.common_weather_text_rain_snow_mixed,
        R.string.common_weather_text_clear_sky
    ),
    "317" to listOf( // 雨か雪後曇 RAIN OR SNOW, CLOUDY LATER
        R.string.common_weather_text_rain_snow_mixed,
        R.string.common_weather_text_cloudy
    ),
    "320" to listOf( // 朝の内雨後晴 RAIN IN THE MORNING, CLEAR LATER
        R.string.common_weather_text_rain,
        R.string.common_weather_text_clear_sky
    ),
    "321" to listOf( // 朝の内雨後曇 RAIN IN THE MORNING, CLOUDY LATER
        R.string.common_weather_text_rain,
        R.string.common_weather_text_cloudy
    ),
    "322" to listOf( // 雨朝晩一時雪 OCCASIONAL SNOW IN THE MORNING AND EVENING, RAIN DURING THE DAY
        R.string.common_weather_text_rain,
        R.string.common_weather_text_snow
    ),
    "323" to listOf( // 雨昼頃から晴 RAIN, CLEAR IN THE AFTERNOON
        R.string.common_weather_text_rain,
        R.string.common_weather_text_clear_sky
    ),
    "324" to listOf( // 雨夕方から晴 RAIN, CLEAR IN THE EVENING
        R.string.common_weather_text_rain,
        R.string.common_weather_text_clear_sky
    ),
    "325" to listOf( // 雨夜は晴 RAIN, CLEAR IN THE NIGHT
        R.string.common_weather_text_rain,
        R.string.common_weather_text_clear_sky
    ),
    "326" to listOf( // 雨夕方から雪 RAIN, SNOW IN THE EVENING
        R.string.common_weather_text_rain,
        R.string.common_weather_text_snow
    ),
    "327" to listOf( // 雨夜は雪 RAIN,SNOW IN THE NIGHT
        R.string.common_weather_text_rain,
        R.string.common_weather_text_snow
    ),
    "328" to listOf( // 雨一時強く降る RAIN, EXPECT OCCASIONAL HEAVY RAINFALL
        R.string.common_weather_text_rain_heavy,
        R.string.common_weather_text_rain_heavy
    ),
    "329" to listOf( // 雨一時みぞれ RAIN, OCCASIONAL SLEET
        R.string.common_weather_text_rain_snow_mixed,
        R.string.common_weather_text_rain_snow_mixed
    ),
    "340" to listOf( // 雪か雨 SNOWORRAIN
        R.string.common_weather_text_rain_snow_mixed,
        R.string.common_weather_text_rain_snow_mixed
    ),
    "350" to listOf( // 雨で雷を伴う RAIN AND THUNDER
        R.string.weather_kind_thunderstorm,
        R.string.weather_kind_thunderstorm
    ),
    "361" to listOf( // 雪か雨後晴 SNOW OR RAIN, CLEAR LATER
        R.string.common_weather_text_rain_snow_mixed,
        R.string.common_weather_text_clear_sky
    ),
    "371" to listOf( // 雪か雨後曇 SNOW OR RAIN, CLOUDY LATER
        R.string.common_weather_text_rain_snow_mixed,
        R.string.common_weather_text_cloudy
    ),
    "400" to listOf( // 雪 SNOW
        R.string.common_weather_text_snow,
        R.string.common_weather_text_snow
    ),
    "401" to listOf( // 雪時々晴 SNOW, FREQUENT CLEAR
        R.string.common_weather_text_snow,
        R.string.common_weather_text_snow
    ),
    "402" to listOf( // 雪時々止む SNOWTHROUGHOUT THE DAY
        R.string.common_weather_text_snow,
        R.string.common_weather_text_snow
    ),
    "403" to listOf( // 雪時々雨 SNOW,FREQUENT SCCATERED SHOWERS
        R.string.common_weather_text_rain_snow_mixed_showers,
        R.string.common_weather_text_rain_snow_mixed_showers
    ),
    "405" to listOf( // 大雪 HEAVYSNOW
        R.string.common_weather_text_snow_heavy,
        R.string.common_weather_text_snow_heavy
    ),
    "406" to listOf( // 風雪強い SNOWSTORM
        R.string.common_weather_text_snow_heavy,
        R.string.common_weather_text_snow_heavy
    ),
    "407" to listOf( // 暴風雪 HEAVYSNOWSTORM
        R.string.common_weather_text_snow_heavy,
        R.string.common_weather_text_snow_heavy
    ),
    "409" to listOf( // 雪一時雨 SNOW, OCCASIONAL SCCATERED SHOWERS
        R.string.common_weather_text_rain_snow_mixed_showers,
        R.string.common_weather_text_rain_snow_mixed_showers
    ),
    "411" to listOf( // 雪後晴 SNOW,CLEAR LATER
        R.string.common_weather_text_snow,
        R.string.common_weather_text_clear_sky
    ),
    "413" to listOf( // 雪後曇 SNOW,CLOUDY LATER
        R.string.common_weather_text_snow,
        R.string.common_weather_text_cloudy
    ),
    "414" to listOf( // 雪後雨 SNOW,RAIN LATER
        R.string.common_weather_text_snow,
        R.string.common_weather_text_rain
    ),
    "420" to listOf( // 朝の内雪後晴 SNOW IN THE MORNING, CLEAR LATER
        R.string.common_weather_text_snow,
        R.string.common_weather_text_clear_sky
    ),
    "421" to listOf( // 朝の内雪後曇 SNOW IN THE MORNING, CLOUDY LATER
        R.string.common_weather_text_snow,
        R.string.common_weather_text_cloudy
    ),
    "422" to listOf( // 雪昼頃から雨 SNOW, RAIN IN THE AFTERNOON
        R.string.common_weather_text_snow,
        R.string.common_weather_text_rain
    ),
    "423" to listOf( // 雪夕方から雨 SNOW, RAIN IN THE EVENING
        R.string.common_weather_text_snow,
        R.string.common_weather_text_rain
    ),
    "425" to listOf( // 雪一時強く降る SNOW, EXPECT OCCASIONAL HEAVY SNOWFALL
        R.string.common_weather_text_snow,
        R.string.common_weather_text_snow
    ),
    "426" to listOf( // 雪後みぞれ SNOW, SLEET LATER
        R.string.common_weather_text_snow,
        R.string.common_weather_text_rain_snow_mixed
    ),
    "427" to listOf( // 雪一時みぞれ SNOW, OCCASIONAL SLEET
        R.string.common_weather_text_rain_snow_mixed,
        R.string.common_weather_text_rain_snow_mixed
    ),
    "450" to listOf( // 雪で雷を伴う SNOW AND THUNDER
        R.string.common_weather_text_snow,
        R.string.common_weather_text_snow
    )
)
