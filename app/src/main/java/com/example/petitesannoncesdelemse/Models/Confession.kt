package com.example.petitesannoncesdelemse.Models

class Confession {
    var contentOfTheConfession: String;
    var dateofPost: Long;
    var id: Long;

    constructor(
        contentOfTheConfession: String,
        dateofPost: Long,
        id: Long
    ) {
        this.id = id
        this.dateofPost = dateofPost
        this.contentOfTheConfession = contentOfTheConfession
    }



}