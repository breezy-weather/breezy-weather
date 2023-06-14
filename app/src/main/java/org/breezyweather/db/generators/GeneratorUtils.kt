package org.breezyweather.db.generators

object GeneratorUtils {

    @JvmStatic
    fun nonNull(string: String?): String {
        return string ?: ""
    }
}