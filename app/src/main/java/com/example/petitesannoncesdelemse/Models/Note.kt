package com.example.petitesannoncesdelemse.Models

import java.util.*

class Note {
    var contentOfTheNote: String;
    var dateofPost: Long;
    var id: Long;
    var type: Int; //CentreVille or Request
    var user: User;


    constructor(
        contentOfTheNote: String,
        dateofPost: Long,
        id: Long,
        type: Int,
        user: User
    ) {
        this.id = id
        this.dateofPost = dateofPost
        this.contentOfTheNote = contentOfTheNote
        this.type = type
        this.user = user
    }



}