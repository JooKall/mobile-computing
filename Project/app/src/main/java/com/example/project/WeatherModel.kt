package com.example.project

// This data class must match the JSON structure returned by OpenWeatherMap API.
// The root object contains a "main" field, which itself contains "temp" and "humidity".
// If the structure here doesn't match the JSON, Retrofit + Gson assign the response properly.
data class WeatherModel(
    val main: Main
)

data class Main(
    val temp: Float,
    val humidity: Int
)
