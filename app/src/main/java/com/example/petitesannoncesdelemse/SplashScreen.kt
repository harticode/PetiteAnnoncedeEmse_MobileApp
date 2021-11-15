package com.example.petitesannoncesdelemse

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen2)

        val splashscreenview = findViewById<LinearLayout>(R.id.splashscreenview);

        splashscreenview.alpha = 0f;
        splashscreenview.animate().setDuration(1500).alpha(1f).withEndAction{
            val i = Intent(this, SignInActivity::class.java)
            startActivity(i);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

    }
}