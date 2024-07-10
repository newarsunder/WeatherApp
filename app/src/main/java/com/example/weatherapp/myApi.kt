package com.example.weatherapp

import com.example.todolist.Comments
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface myApi {
    @Headers("x-api-key: ad4a30a1de8141f9a3c80956240907")
    @GET("current.json")
    fun getWeather(@Query("key") api_key: String,@Query("q") query: String): Call<Comments>
}