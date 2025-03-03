import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookface_android.Post
import com.example.bookface_android.R

class PostAdapter(private val postList: List<Pair<Post, String>>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postText: TextView = itemView.findViewById(R.id.tv_post_content)
        val usernameText: TextView = itemView.findViewById(R.id.tv_username) // New
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_list_view, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val (post, username) = postList[position] // Get post and username
        holder.postText.text = post.text
        holder.usernameText.text = username
    }

    override fun getItemCount(): Int {
        return postList.size
    }
}

