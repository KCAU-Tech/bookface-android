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

class adapter(
    private val postList: List<posts>,
    private val context: Context
) : RecyclerView.Adapter<adapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.textView.text = post.text

        // Log the image URL for debugging
        Log.d("Adapter", "Image URL: ${post.photoUrl}")

        // Load image using Glide with error handling
        Glide.with(context)
            .load(post.photoUrl)
            .error(R.drawable.logo) // Replace with your error placeholder
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}