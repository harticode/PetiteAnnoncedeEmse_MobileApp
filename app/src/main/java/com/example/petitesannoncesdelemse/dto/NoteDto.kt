package com.example.petitesannoncesdelemse.dto

import com.example.petitesannoncesdelemse.Models.User
import java.util.*

data class NoteDto( var id: Long,
                    var contentOfTheNote: String,
                    var dateofPost: Long,
                    var type: Int,//CentreVille or Request
                    var user: UserDto)