package edu.carleton.baskaufj.weatherapp.network


import edu.carleton.baskaufj.weatherapp.data.WeatherResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPI {
    @GET("/data/2.5/weather")
    fun getWeather(@Query("q") q: String,
                   @Query("units") units: String = "metric",
                   @Query("appid") appId: String = "3cd90055e4a084be8c9903f3d08e4d9e",
                   @Query("lang") lang: String = "ru") : Call<WeatherResult>
}