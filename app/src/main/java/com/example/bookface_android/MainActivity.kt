package com.example.bookface_android

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bookface_android.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.android.flexbox.FlexboxLayout

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private var selectedImageUri: Uri? = null
    private val selectedInterests = mutableSetOf<String>()
    private val minInterests = 5
    private val maxInterests = 10

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val courseSpinner: Spinner = findViewById(R.id.courseSpinner)
        val interestsContainer: FlexboxLayout = findViewById(R.id.interestsContainer)
        val uploadButton: Button = findViewById(R.id.uploadButton)
        val submitButton: Button = findViewById(R.id.submitButton)

        // Populate Courses Dropdown
        val courses = arrayOf("Computer Science", "Engineering", "Business", "Medicine", "Law", "Education", "Agriculture")
        val adapter = ArrayAdapter(this, R.layout.spinner_item, courses)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        courseSpinner.adapter = adapter

        // Handle Image Upload
        uploadButton.setOnClickListener {
            openFileChooser()
        }

        // Interest Selection
        setupInterestButtons(interestsContainer)

        // Handle Submit
        submitButton.setOnClickListener {
            saveUserProfile()
        }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data?.data != null) {
            selectedImageUri = data.data
            binding.profileImage.setImageURI(selectedImageUri) // Show image in ImageView
        }
    }

    private fun uploadImageAndSaveProfile(username: String, bio: String, course: String) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val storageRef = storage.reference.child("users/${user.uid}/profile/profile.jpg")

        selectedImageUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        saveProfileToFirestore(username, bio, course, downloadUrl.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: saveProfileToFirestore(username, bio, course, null)
    }


    private fun saveProfileToFirestore(username: String, bio: String, course: String, imageUrl: String?) {
        val user = firebaseAuth.currentUser ?: return

        val userProfile = mapOf(
            "username" to username,
            "bio" to bio,
            "course" to course,
            "interests" to selectedInterests.toList(),
            "profileImage" to imageUrl
        )

        firestore.collection("users").document(user.uid)
            .set(userProfile)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, home::class.java)
                startActivity(intent)
                finish()
            }

            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserProfile() {
        val username = binding.usernameEditText.text.toString().trim()
        val bio = binding.bioEditText.text.toString().trim()
        val course = binding.courseSpinner.selectedItem.toString()

        if (username.isEmpty() || bio.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedInterests.size < minInterests) {
            Toast.makeText(this, "Select at least 5 interests", Toast.LENGTH_SHORT).show()
            return
        }

        uploadImageAndSaveProfile(username, bio, course)
    }

    private fun setupInterestButtons(container: FlexboxLayout) {
        for (i in 0 until container.childCount) {
            val button = container.getChildAt(i) as? Button ?: continue
            button.setOnClickListener {
                val interest = button.text.toString()
                if (selectedInterests.contains(interest)) {
                    selectedInterests.remove(interest)
                    button.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
                } else {
                    if (selectedInterests.size < maxInterests) {
                        selectedInterests.add(interest)
                        button.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
                    } else {
                        Toast.makeText(this, "You can select up to 10 interests", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
