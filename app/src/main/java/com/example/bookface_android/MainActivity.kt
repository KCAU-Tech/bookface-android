package com.example.bookface_android

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bookface_android.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        //binding.btnlogout.setOnClickListener{
            //firebaseAuth.signOut()

            //val intent=Intent(this, sign_in::class.java)
            //startActivity(intent)
          //  finish()//
        //}
        val courses = arrayOf("Computer Science", "Engineering", "Business", "Medicine", "Law", "Education", "Agriculture")

        val courseSpinner: Spinner = findViewById(R.id.courseSpinner)
        val adapter = ArrayAdapter(this, R.layout.spinner_item, courses)
        adapter.setDropDownViewResource(R.layout.spinner_item) // Ensures dropdown text is black
        courseSpinner.adapter = adapter



    }
}