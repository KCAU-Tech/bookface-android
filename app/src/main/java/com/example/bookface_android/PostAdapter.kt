package com.example.bookface_android

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PostAdapter(
    private val postList: List<Pair<Post, String>>,
    private val context: Context
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postText: TextView = itemView.findViewById(R.id.tv_post_content)
        val usernameText: TextView = itemView.findViewById(R.id.tv_username)
        val imageView: ImageView = itemView.findViewById(R.id.postImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_list_view, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val (post, username) = postList[position]
        holder.postText.text = post.text
        holder.usernameText.text = username

        // Check if the post has an image URL
        if (post.photoUrl.isNullOrEmpty()) {
            // If there is no image URL, hide the ImageView
            holder.imageView.visibility = View.GONE
        } else {
            // If there is an image URL, show the ImageView and load the image
            holder.imageView.visibility = View.VISIBLE

            // Log the image URL for debugging
            Log.d("PostAdapter", "Image URL: ${post.photoUrl}")

            // Load image using Glide with error handling
            Glide.with(context)
                .load(post.photoUrl)
                .error(R.drawable.logo) // Replace with your error placeholder
                .into(holder.imageView)
        }
    }

    override fun getItemCount(): Int {
        return postList.size
    }
}