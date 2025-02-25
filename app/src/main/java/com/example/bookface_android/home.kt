package com.example.bookface_android

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bookface_android.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth

class home : AppCompatActivity() {

    private lateinit var binding:ActivityHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()


        binding.btnlogout.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        firebaseAuth.signOut()
        val intent = Intent(this, sign_in::class.java)
        startActivity(intent)
        finish()
    }
}




