package com.example.petitesannoncesdelemse.dto

import com.example.petitesannoncesdelemse.Models.User
import java.util.*

data class ConfessionDto(var id: Long,
                         var contentOfTheConfession: String,
                         var dateofPost: Long)