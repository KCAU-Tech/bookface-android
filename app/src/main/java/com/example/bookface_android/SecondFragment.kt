import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.bookface_android.adapter
import com.example.bookface_android.R
import com.example.bookface_android.posts
import com.google.firebase.firestore.FirebaseFirestore

class SecondFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_second, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val db = FirebaseFirestore.getInstance()
        val postsRef = db.collection("posts")

        val postList = mutableListOf<posts>()
        val adapter = adapter(postList, requireContext())

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        postsRef.get().addOnSuccessListener { queryDocumentSnapshots ->
            for (document in queryDocumentSnapshots) {
                val post = document.toObject(posts::class.java)
                postList.add(post)
            }
            adapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error loading posts", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}