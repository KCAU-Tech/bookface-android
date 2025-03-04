package com.example.bookface_android

data class Post(
    val text: String = "",  // Post content
    val userId: String = "", // ID of the user who created the post
    val photoUrl: String = ""
)
