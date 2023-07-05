package org.breezyweather

import kotlinx.coroutines.test.runTest
import org.breezyweather.common.basic.models.options._basic.Utils
import org.junit.jupiter.api.Test
import java.text.DecimalFormat
import java.util.*
import java.util.regex.Pattern

// TODO: Use functions from AccuResultConverter, or just implement AccuConverter differently so we don't have to
// deal with converting weatherText
class MatchTest {
    @Test
    fun pattern() = runTest {
        val text = "Frigid with snow, acuu an additional 1-3 cm; limited outdoor activity. 2-4 cm, 4-5cm"
        val NumberPattern = "\\d+-\\d+(\\s+)?cm"
        val pattern = Pattern.compile(NumberPattern)
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            println(text.substring(start, end))
        }
    }

    @Test
    fun split() = runTest {
        val text = "dadasd dsad   dad"
        println(text.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().contentToString())
    }

    @Test
    fun convertUnit() = runTest {
        var str = "Frigid with snow, acuu an additional 1-3 cm; limited outdoor activity. 2-4 cm, 4-5cm"
        str = convertUnit(str, "cm") { value: Float -> value * 10 }
        println(str)
    }

    @Test
    fun formatFloat() = runTest {
        println(Utils.formatFloat(7.00646f, 2))
        println(Utils.formatFloat(7.00246f, 2))
    }

    private fun interface MilliMeterConverter {
        fun toMilliMeters(value: Float): Float
    }

    companion object {
        private fun convertUnit(
            strP: String,
            targetUnit: String,
            converter: MilliMeterConverter
        ): String {
            var str = strP
            val numberPattern = "\\d+-\\d+(\\s+)?"

            // cm
            val matcher = Pattern.compile(numberPattern + targetUnit).matcher(str)
            val targetList: MutableList<String> = ArrayList()
            val resultList: MutableList<String> = ArrayList()
            while (matcher.find()) {
                val target = str.substring(matcher.start(), matcher.end())
                targetList.add(target)
                val targetSplitResults =
                    target.replace(" ".toRegex(), "").split(targetUnit.toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                val numberTexts =
                    targetSplitResults[0].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (i in numberTexts.indices) {
                    var number = numberTexts[i].toFloat()
                    number = converter.toMilliMeters(number)
                    numberTexts[i] = floatToString(number)
                }
                resultList.add(
                    arrayToString(numberTexts, '-')
                            + " " + "mm"
                )
            }
            for (i in targetList.indices) {
                str = str.replace(targetList[i], resultList[i])
            }
            return str
        }

        private fun floatToString(number: Float): String {
            return if (number.toInt() * 1000 == (number * 1000).toInt()) {
                number.toInt().toString()
            } else {
                DecimalFormat("######0.0").format(number.toDouble())
            }
        }

        private fun arrayToString(array: Array<String>, separator: Char): String {
            val builder = StringBuilder()
            for (i in array.indices) {
                builder.append(array[i])
                if (i < array.size - 1) {
                    builder.append(separator)
                }
            }
            return builder.toString()
        }
    }
}
