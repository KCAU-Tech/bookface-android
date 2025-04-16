package com.example.bookface_android

import com.google.firebase.Timestamp

data class Post(
    val text: String = "",
    val userId: String = "",
    val photoUrl: String = "",
    var likes: Int = 0,
    val createdAt: Timestamp? = null,
    var id: String? = null
)


