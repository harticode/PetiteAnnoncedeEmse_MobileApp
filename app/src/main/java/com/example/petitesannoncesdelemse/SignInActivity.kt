package com.example.petitesannoncesdelemse

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser

class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentification)
        val intent = Intent(this, SetupProfileActivity::class.java)
        val loginButton = findViewById<ImageButton>(R.id.Login);


        loginButton.setOnClickListener {
            startActivity(intent)
        }

        val isUserSignedIn = FirebaseAuth.getInstance().currentUser != null
        if(isUserSignedIn){
            startActivity(intent)
        }
    }
}