package wangdaye.com.geometricweather.weather.services

import android.content.Context
import android.text.TextUtils
import wangdaye.com.geometricweather.common.basic.models.Location
import wangdaye.com.geometricweather.common.basic.models.weather.Weather
import wangdaye.com.geometricweather.common.utils.LanguageUtils

/**
 * Weather service.
 */
abstract class WeatherService {

    companion object {

        @JvmStatic
        protected fun formatLocationString(str: String?): String {
            str?.let {
                if (TextUtils.isEmpty(it)) {
                    return ""
                }
                if (it.endsWith("地区")) {
                    return it.substring(0, it.length - 2)
                }
                if (it.endsWith("区")
                        && !it.endsWith("新区")
                        && !it.endsWith("矿区")
                        && !it.endsWith("郊区")
                        && !it.endsWith("风景区")
                        && !it.endsWith("东区")
                        && !it.endsWith("西区")) {
                    return it.substring(0, it.length - 1)
                }
                if (it.endsWith("县")
                        && it.length != 2 && !it.endsWith("通化县")
                        && !it.endsWith("本溪县")
                        && !it.endsWith("辽阳县")
                        && !it.endsWith("建平县")
                        && !it.endsWith("承德县")
                        && !it.endsWith("大同县")
                        && !it.endsWith("五台县")
                        && !it.endsWith("乌鲁木齐县")
                        && !it.endsWith("伊宁县")
                        && !it.endsWith("南昌县")
                        && !it.endsWith("上饶县")
                        && !it.endsWith("吉安县")
                        && !it.endsWith("长沙县")
                        && !it.endsWith("衡阳县")
                        && !it.endsWith("邵阳县")
                        && !it.endsWith("宜宾县")) {
                    return it.substring(0, it.length - 1)
                }
                if (it.endsWith("市")
                        && !it.endsWith("新市")
                        && !it.endsWith("沙市")
                        && !it.endsWith("津市")
                        && !it.endsWith("芒市")
                        && !it.endsWith("西市")
                        && !it.endsWith("峨眉山市")) {
                    return it.substring(0, it.length - 1)
                }
                if (it.endsWith("回族自治州")
                        || it.endsWith("藏族自治州")
                        || it.endsWith("彝族自治州")
                        || it.endsWith("白族自治州")
                        || it.endsWith("傣族自治州")
                        || it.endsWith("蒙古自治州")) {
                    return it.substring(0, it.length - 5)
                }
                if (it.endsWith("朝鲜族自治州")
                        || it.endsWith("哈萨克自治州")
                        || it.endsWith("傈僳族自治州")
                        || it.endsWith("蒙古族自治州")) {
                    return it.substring(0, it.length - 6)
                }
                if (it.endsWith("哈萨克族自治州")
                        || it.endsWith("苗族侗族自治州")
                        || it.endsWith("藏族羌族自治州")
                        || it.endsWith("壮族苗族自治州")
                        || it.endsWith("柯尔克孜自治州")) {
                    return it.substring(0, it.length - 7)
                }
                if (it.endsWith("布依族苗族自治州")
                        || it.endsWith("土家族苗族自治州")
                        || it.endsWith("蒙古族藏族自治州")
                        || it.endsWith("柯尔克孜族自治州")
                        || it.endsWith("傣族景颇族自治州")
                        || it.endsWith("哈尼族彝族自治州")) {
                    return it.substring(0, it.length - 8)
                }
                if (it.endsWith("自治州")) {
                    return it.substring(0, it.length - 3)
                }
                if (it.endsWith("省")) {
                    return it.substring(0, it.length - 1)
                }
                if (it.endsWith("壮族自治区") || it.endsWith("回族自治区")) {
                    return it.substring(0, it.length - 5)
                }
                if (it.endsWith("维吾尔自治区")) {
                    return it.substring(0, it.length - 6)
                }
                if (it.endsWith("维吾尔族自治区")) {
                    return it.substring(0, it.length - 7)
                }
                return if (it.endsWith("自治区")) {
                    it.substring(0, it.length - 3)
                } else {
                    it
                }
            }
            return ""
        }

        @JvmStatic
        protected fun convertChinese(text: String?): String? = try {
            LanguageUtils.traditionalToSimplified(text)
        } catch (e: Exception) {
            text
        }
    }

    class WeatherResultWrapper(val result: Weather?)

    abstract suspend fun getWeather(context: Context, location: Location): Weather?
    abstract suspend fun getLocation(context: Context, query: String): List<Location>
    abstract suspend fun getLocation(context: Context, location: Location): List<Location>
}