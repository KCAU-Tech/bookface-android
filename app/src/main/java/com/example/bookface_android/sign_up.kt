package com.example.bookface_android

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bookface_android.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class sign_up : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize ProgressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Waiting for email verification...")
        progressDialog.setCancelable(false) // User cannot cancel manually

        binding.toSignIn.setOnClickListener {
            startActivity(Intent(this, sign_in::class.java))
            finish()
        }

        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val pass = binding.passET.text.toString().trim()
            val confirmPass = binding.confirmPassEt.text.toString().trim()

            val emailRegex = Regex("^[0-9]{7}@students\\.kcau\\.ac\\.ke\$") // 6 digits before domain
            val passwordRegex = Regex("^(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}\$") // Strong password

            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (!email.matches(emailRegex)) {
                    Toast.makeText(this, "Invalid email! Use: 2205852@students.kcau.ac.ke", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (!pass.matches(passwordRegex)) {
                    Toast.makeText(
                        this,
                        "Password must be at least 8 characters long, contain a capital letter, a number, and a special character!",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }
                if (pass != confirmPass) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                firebaseAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            user?.sendEmailVerification()?.addOnCompleteListener { emailTask ->
                                if (emailTask.isSuccessful) {
                                    Toast.makeText(this, "Verification email sent. Check your inbox!", Toast.LENGTH_LONG).show()

                                    // Show loading spinner
                                    progressDialog.show()

                                    // Coroutine to check for email verification
                                    CoroutineScope(Dispatchers.Main).launch {
                                        while (true) {
                                            user.reload()
                                            if (user.isEmailVerified) {
                                                progressDialog.dismiss() // Hide loading spinner
                                                Toast.makeText(this@sign_up, "Email verified! Proceeding...", Toast.LENGTH_SHORT).show()
                                                startActivity(Intent(this@sign_up, sign_in::class.java))
                                                finish()
                                                break
                                            }
                                            delay(3000) // Check every 3 seconds
                                        }
                                    }
                                } else {
                                    Toast.makeText(this, "Failed to send verification email", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(this, task.exception?.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Empty fields are not allowed!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
