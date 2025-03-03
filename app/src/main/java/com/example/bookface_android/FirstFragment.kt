package com.example.bookface_android

import PostAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookface_android.databinding.FragmentFirstBinding
import com.google.firebase.firestore.FirebaseFirestore


class FirstFragment : Fragment() {

    private lateinit var binding: FragmentFirstBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val postList = mutableListOf<Pair<Post, String>>() // Pair<Post, Username>
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFirstBinding.inflate(inflater, container, false)
        val view = binding.root

        recyclerView = view.findViewById(R.id.rvPosts)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = PostAdapter(postList)
        recyclerView.adapter = postAdapter

        binding.btnToPost.setOnClickListener {
            val intent = Intent(requireActivity(), PostingActivity2::class.java)
            startActivity(intent)
        }

        loadPosts()
        return view
    }

    private fun loadPosts() {
        db.collection("posts")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("Firestore", "Error fetching posts", e)
                    return@addSnapshotListener
                }

                postList.clear()

                snapshots?.documents?.forEach { document ->
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        fetchUsername(post) // Fetch username for each post
                    }
                }
            }
    }

    private fun fetchUsername(post: Post) {
        db.collection("users").document(post.userId)
            .get()
            .addOnSuccessListener { userDoc ->
                val username = userDoc.getString("username") ?: "Unknown"
                postList.add(Pair(post, username))
                postAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching username", e)
            }
    }
}

