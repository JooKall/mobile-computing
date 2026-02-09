package com.example.project.api

import com.example.project.WeatherModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// Found help here: https://dev.to/ethand91/android-jetpack-compose-api-tutorial-1kh5
interface OpenWeatherService {
    @GET("weather")
    fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Call<WeatherModel>
}