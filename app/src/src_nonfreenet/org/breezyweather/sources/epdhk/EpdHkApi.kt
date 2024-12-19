package org.breezyweather.sources.epdhk

import org.breezyweather.sources.epdhk.xml.EpdHkConcentrationsResult
import retrofit2.Call
import retrofit2.http.GET

interface EpdHkApi {
    @GET("epd/ddata/html/out/24pc_Eng.xml")
    fun getConcentrations(): Call<EpdHkConcentrationsResult>
}
