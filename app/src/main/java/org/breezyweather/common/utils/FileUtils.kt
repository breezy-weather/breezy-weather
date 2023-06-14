package org.breezyweather.common.utils

import android.content.Context
import kotlinx.serialization.json.Json
import org.breezyweather.common.basic.models.ChineseCity
import java.io.BufferedReader
import java.io.Closeable
import java.io.IOException
import java.io.InputStreamReader

/**
 * File utils.
 */
object FileUtils {
    fun readCityList(context: Context): List<ChineseCity> {
        return Json.decodeFromString<List<ChineseCity>>(readAssetFileToString(context, "city_list.txt"))
    }

    private fun readAssetFileToString(context: Context, fileName: String): String {
        val result = StringBuilder()
        var inputReader: InputStreamReader? = null
        var bufReader: BufferedReader? = null
        try {
            inputReader = InputStreamReader(context.resources.assets.open(fileName))
            bufReader = BufferedReader(inputReader)
            var line: String?
            while (bufReader.readLine().also { line = it } != null) {
                result.append(line)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        closeIO(inputReader!!, bufReader!!)
        return result.toString()
    }

    private fun closeIO(vararg closeables: Closeable) {
        try {
            for (closeable in closeables) {
                closeable.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
