
package com.example.bookface_android

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

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
        val imageView: ImageView = itemView.findViewById(R.id.postImage)
        val likeButton: ImageView = itemView.findViewById(R.id.btn_like)
        val likeCountText: TextView = itemView.findViewById(R.id.tv_likes)
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

        holder.postText.text = post.text
        holder.usernameText.text = username
        holder.likeCountText.text = "${post.likes} likes"

        // ✅ Show or hide image based on photoUrl
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

        // Like button click logic
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
