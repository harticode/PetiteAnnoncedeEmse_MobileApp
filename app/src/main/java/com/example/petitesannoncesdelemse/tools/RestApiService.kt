package com.example.petitesannoncesdelemse.tools

import com.example.petitesannoncesdelemse.Models.Confession
import com.example.petitesannoncesdelemse.Models.Note
import com.example.petitesannoncesdelemse.Models.User
import com.example.petitesannoncesdelemse.api.ConfessionApiService
import com.example.petitesannoncesdelemse.api.NoteApiService
import com.example.petitesannoncesdelemse.api.UserApiService
import com.example.petitesannoncesdelemse.dto.ConfessionDto
import com.example.petitesannoncesdelemse.dto.NoteDto
import com.example.petitesannoncesdelemse.dto.UserDto
import com.example.petitesannoncesdelemse.tools.ApiServices

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class RestApiService {
    fun addUser(userData: User, onResult: (UserDto?) -> Unit){
        val retrofit = ApiServices().buildService(UserApiService::class.java)
        retrofit.addUser(userData).enqueue(
            object : Callback<UserDto> {
                override fun onFailure(call: Call<UserDto>, t: Throwable) {
                    onResult(null)
                }
                override fun onResponse( call: Call<UserDto>, response: Response<UserDto>) {
                    val addedUser = response.body()
                    onResult(addedUser)
                }
            }
        )
    }

    /*fun findUser(email: String, onResult: (UserDto?) -> Unit){
        val retrofit = ApiServices().buildService(UserApiService::class.java)
        retrofit.findUser(email).enqueue(
            object : Callback<UserDto> {
                override fun onFailure(call: Call<UserDto>, t: Throwable) {
                    onResult(null)
                }
                override fun onResponse( call: Call<UserDto>, response: Response<UserDto>) {
                    val foundedUser = response.body()!!
                    onResult(foundedUser)
                }
            }
        )
    }*/

    fun addNote(note: Note, onResult: (NoteDto?) -> Unit){
        val retrofit = ApiServices().buildService(NoteApiService::class.java)
        retrofit.addNote(note).enqueue(
            object : Callback<NoteDto> {
                override fun onFailure(call: Call<NoteDto>, t: Throwable) {
                    onResult(null)
                }
                override fun onResponse(call: Call<NoteDto>, response: Response<NoteDto>) {
                    val addedNote = response.body()
                    onResult(addedNote)
                }
            }
        )
    }

    fun addConfession(confession: Confession, onResult: (ConfessionDto?) -> Unit){
        val retrofit = ApiServices().buildService(ConfessionApiService::class.java)
        retrofit.addConfession(confession).enqueue(
            object : Callback<ConfessionDto> {
                override fun onFailure(call: Call<ConfessionDto>, t: Throwable) {
                    onResult(null)
                }
                override fun onResponse(call: Call<ConfessionDto>, response: Response<ConfessionDto>) {
                    val addedConfession = response.body()
                    onResult(addedConfession)
                }
            }
        )
    }

    fun updateNoteContent(note: Note, onResult: (NoteDto?) -> Unit){
        val retrofit = ApiServices().buildService(NoteApiService::class.java)
        retrofit.updateNoteContent(note.id, note).enqueue(
            object : Callback<NoteDto> {
                override fun onFailure(call: Call<NoteDto>, t: Throwable) {
                    onResult(null)
                }
                override fun onResponse(call: Call<NoteDto>, response: Response<NoteDto>) {
                    val updatedNote = response.body()
                    onResult(updatedNote)
                }
            }
        )
    }

    fun deleteNote(noteId: Long, onResult: (Int?) -> Unit){
        val retrofit = ApiServices().buildService(NoteApiService::class.java)
        retrofit.deleteNote(noteId).enqueue(
        object : Callback<Int> {
            override fun onFailure(call: Call<Int>, t: Throwable) {
                onResult(null)
            }
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                val responseCode = response.code()
                onResult(responseCode)
            }
        })
    }

}