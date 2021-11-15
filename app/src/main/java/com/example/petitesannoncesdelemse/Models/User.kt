package com.example.petitesannoncesdelemse.Models

class User {
    var email: String; //Unique and we verify it using an SMS(firebase authentication)
    var photoUrl: String;
    var username: String;

    constructor(email: String, photoUrl: String, nameuser: String) {
        this.username = nameuser
        this.email = email
        this.photoUrl = photoUrl
    }

}