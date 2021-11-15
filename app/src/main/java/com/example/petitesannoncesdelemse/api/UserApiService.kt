package com.example.petitesannoncesdelemse.api

import com.example.petitesannoncesdelemse.Models.User
import com.example.petitesannoncesdelemse.dto.NoteDto
import com.example.petitesannoncesdelemse.dto.UserDto
import retrofit2.Call
import retrofit2.http.*

interface UserApiService {
    @Headers("Content-Type: application/json")
    @POST("users")
    fun addUser(@Body userData: User): Call<UserDto>

    @GET("users/{email}")
    fun findUser(@Path("email") email: String): Call<UserDto>

}
