package com.example.bookface_android


import android.R
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookface_android.databinding.ActivityPostingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.util.UUID

class PostingActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityPostingBinding
    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null
    private var storage: FirebaseStorage? = null
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostingBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Select Image
        binding.btnPhoto.setOnClickListener { v -> pickImage() }

        // Post Content
        binding.btnPost.setOnClickListener { v ->
            val postText: String = binding.etPost.getText().toString().trim()
            if (postText.isEmpty()) {
                Toast.makeText(this, "Post text cannot be empty!", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val userId =
                if (auth!!.currentUser != null) auth!!.currentUser!!.uid else null
            if (userId == null) return@setOnClickListener
            if (selectedImageUri != null) {
                uploadImageToStorage(userId, postText, selectedImageUri!!)
            } else {
                submitPost(userId, postText, null)
            }
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setType("image/*")
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            binding.btnPhoto.setImageURI(selectedImageUri)
        }
    }

    private fun compressImage(photoUri: Uri): ByteArray? {
        try {
            val contentResolver = contentResolver
            val inputStream = contentResolver.openInputStream(photoUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)

            return outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun uploadImageToStorage(userId: String, postText: String, imageUri: Uri) {
        val filename = UUID.randomUUID().toString()
        val storageRef = storage!!.reference.child("posts/$userId/$filename.webp")

        val compressedImage = compressImage(imageUri)
        if (compressedImage == null) {
            Toast.makeText(this, "Image compression failed!", Toast.LENGTH_SHORT).show()
            return
        }

        storageRef.putBytes(compressedImage)
            .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->
                storageRef.downloadUrl.addOnSuccessListener { imageUrl: Uri ->
                    submitPost(
                        userId,
                        postText,
                        imageUrl.toString()
                    )
                }
            }
            .addOnFailureListener { e: Exception? ->
                Toast.makeText(
                    this,
                    "Image upload failed!",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun submitPost(userId: String, text: String, imageUrl: String?) {
        val timestamp = System.currentTimeMillis()

        val post: MutableMap<String, Any?> = HashMap()
        post["userId"] = userId
        post["text"] = text
        post["photoUrl"] = imageUrl
        post["likes"] = 0
        post["comments"] = ArrayList<Any>()
        post["createdAt"] = timestamp
        post["updatedAt"] = timestamp

        firestore!!.collection("posts")
            .add(post)
            .addOnSuccessListener { documentReference: DocumentReference? ->
                Toast.makeText(this, "Post uploaded!", Toast.LENGTH_SHORT).show()
                binding.etPost.setText("")
                binding.btnPhoto.setImageResource(R.drawable.bottom_bar)
                selectedImageUri = null
            }
            .addOnFailureListener { e: Exception? ->
                Toast.makeText(
                    this,
                    "Failed to post!",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
