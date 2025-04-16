package com.example.bookface_android

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class PostAdapter(
    private val postList: List<Pair<Post, String>>,
    private val context: Context
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    companion object {
        private val likedPosts = mutableSetOf<String>()  // Cache
        private val processingLikes = mutableSetOf<String>()  // Prevent spamming clicks
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postText: TextView = itemView.findViewById(R.id.tv_post_content)
        val usernameText: TextView = itemView.findViewById(R.id.tv_username)
        val postTimeText: TextView = itemView.findViewById(R.id.tv_post_time) // Time view
        val imageView: ImageView = itemView.findViewById(R.id.postImage)
        val likeButton: ImageView = itemView.findViewById(R.id.btn_like)
        val likeCountText: TextView = itemView.findViewById(R.id.tv_likes)
        val profileImage: ImageView = itemView.findViewById(R.id.profile_image)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_list_view, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val (post, username) = postList[position]
        val postId = post.id ?: return
        val firestore = FirebaseFirestore.getInstance()
        val postRef = firestore.collection("posts").document(postId)
        val userRef = FirebaseFirestore.getInstance().collection("users").document(post.userId)

        userRef.get().addOnSuccessListener { document ->
            val profileUrl = document.getString("profileImage")
            if (!profileUrl.isNullOrEmpty()) {
                Glide.with(context)
                    .load(profileUrl)
                    .placeholder(R.drawable.default_profile) // Optional placeholder
                    .circleCrop()
                    .into(holder.profileImage)
            } else {
                holder.profileImage.setImageResource(R.drawable.default_profile)
            }
        }


        // Set content
        holder.postText.text = post.text
        holder.usernameText.text = username
        holder.likeCountText.text = "${post.likes} likes"

        // Show "x time ago"
        holder.postTimeText.text = getTimeAgo(post.createdAt)

        // Show or hide image based on photoUrl
        if (post.photoUrl.isNullOrEmpty()) {
            holder.imageView.visibility = View.GONE
        } else {
            holder.imageView.visibility = View.VISIBLE
            Glide.with(context)
                .load(post.photoUrl)
                .into(holder.imageView)
        }

        val isLiked = likedPosts.contains(postId)
        holder.likeButton.setImageResource(
            if (isLiked) R.drawable.heart_filled else R.drawable.heart_empty
        )

        // Like button logic
        holder.likeButton.setOnClickListener {
            val increment = if (!isLiked) 1 else -1

            postRef.update("likes", FieldValue.increment(increment.toLong()))
                .addOnSuccessListener {
                    if (!isLiked) {
                        likedPosts.add(postId)
                        post.likes += 1
                        holder.likeButton.setImageResource(R.drawable.heart_filled)
                    } else {
                        likedPosts.remove(postId)
                        post.likes -= 1
                        holder.likeButton.setImageResource(R.drawable.heart_empty)
                    }

                    holder.likeCountText.text = "${post.likes} likes"
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to update like", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun getItemCount(): Int = postList.size
}

// Function to format time ago from Firestore Timestamp
fun getTimeAgo(timestamp: Timestamp?): String {
    if (timestamp == null) return "unknown time"

    val time = timestamp.toDate().time
    val now = System.currentTimeMillis()
    val diff = now - time

    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        seconds < 60 -> "just now"
        minutes < 60 -> "$minutes minute${if (minutes != 1L) "s" else ""} ago"
        hours < 24 -> "$hours hour${if (hours != 1L) "s" else ""} ago"
        else -> "$days day${if (days != 1L) "s" else ""} ago"
    }
}
