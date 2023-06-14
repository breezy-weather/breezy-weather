package org.breezyweather.common.basic.models.options._basic

import android.content.Context

interface BaseEnum {
    val id: String
    val nameArrayId: Int
    val valueArrayId: Int
    fun getName(context: Context): String
}

interface VoiceEnum: BaseEnum {
    val voiceArrayId: Int
    fun getVoice(context: Context): String
}

interface UnitEnum<T: Number>: VoiceEnum {
    val unitFactor: Float
    fun getValueWithoutUnit(valueInDefaultUnit: T): T
    fun getValueInDefaultUnit(valueInCurrentUnit: T): T
    fun getValueTextWithoutUnit(valueInDefaultUnit: T): String
    fun getValueText(context: Context, valueInDefaultUnit: T): String
    fun getValueText(context: Context, valueInDefaultUnit: T, rtl: Boolean): String
    fun getValueVoice(context: Context, valueInDefaultUnit: T): String
    fun getValueVoice(context: Context, valueInDefaultUnit: T, rtl: Boolean): String
}