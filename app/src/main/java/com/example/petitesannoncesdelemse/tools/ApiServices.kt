package com.example.petitesannoncesdelemse.tools

import com.example.petitesannoncesdelemse.api.NoteApiService
import com.example.petitesannoncesdelemse.api.UserApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class ApiServices {

    private val client = OkHttpClient.Builder().build()
    //local Testing url = "http://192.168.65.1:8080/api/"
    val url: String = "http://hamza.aitbaali.petiteannoncedeemse.cleverapps.io/api/"

    private val ApiService =
        Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build()


    fun<T> buildService(service: Class<T>): T {
        return ApiService.create(service)
    }
}