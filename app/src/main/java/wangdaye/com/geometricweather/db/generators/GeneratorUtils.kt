package wangdaye.com.geometricweather.db.generators

object GeneratorUtils {

    @JvmStatic
    fun nonNull(string: String?): String {
        return string ?: ""
    }
}