package com.example.petitesannoncesdelemse.api

import com.example.petitesannoncesdelemse.Models.Note
import com.example.petitesannoncesdelemse.Models.User
import com.example.petitesannoncesdelemse.dto.NoteDto
import com.example.petitesannoncesdelemse.dto.UserDto
import retrofit2.Call
import retrofit2.http.*
import java.util.*
import kotlin.collections.ArrayList

interface NoteApiService {

    @Headers("Content-Type: application/json")
    @GET("notes")
    fun findAllNotes(): Call<List<NoteDto>>

    @GET("notes/{type}")
    fun findByType(@Path("type") type: Int): Call<List<NoteDto>>

    @POST("notes")
    fun addNote(@Body noteData: Note): Call<NoteDto>

    @PUT("notes/{id}")
    fun updateNoteContent(@Path("id") id: Long, @Body noteData: Note): Call<NoteDto>

    @DELETE("notes/delete/{id}")
    fun deleteNote(@Path("id") id: Long): Call<Int>

}