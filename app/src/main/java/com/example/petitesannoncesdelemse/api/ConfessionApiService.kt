package com.example.petitesannoncesdelemse.api

import com.example.petitesannoncesdelemse.Models.Confession
import com.example.petitesannoncesdelemse.dto.ConfessionDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface ConfessionApiService {

    @Headers("Content-Type: application/json")
    @POST("confessions")
    fun addConfession(@Body confessionData: Confession): Call<ConfessionDto>


    @GET("confessions")
    fun findAllConfessions(): Call<List<ConfessionDto>>
}