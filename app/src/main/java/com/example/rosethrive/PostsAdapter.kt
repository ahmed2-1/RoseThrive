package com.example.rosethrive

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.content_post_elements.view.*

class PostsAdapter(
    var context: Context,
    var listener: MainListener?,
    val uid: String,
    val isAccount: Boolean
) :
    RecyclerView.Adapter<PostViewHolder>() {
    var posts = ArrayList<Post>()

    val postReference = FirebaseFirestore
        .getInstance()
        .collection(Constants.POSTS_COLLECTION)

    fun addSnapshotListener() {
        val query = if (isAccount) {
            postReference.orderBy(Post.LAST_TOUCHED_KEY, Query.Direction.ASCENDING).whereEqualTo(Post.UID_KEY, uid)
        } else {
            postReference.orderBy(Post.LAST_TOUCHED_KEY, Query.Direction.ASCENDING)
        }

        query.addSnapshotListener { querySnapshot, e ->
            if (e != null) {
                Log.w(Constants.TAG, "listen error", e)
            } else {
                processSnapshotChanges(querySnapshot!!)
            }
        }
    }

    private fun processSnapshotChanges(querySnapshot: QuerySnapshot) {
        // Snapshots has documents and documentChanges which are flagged by type,
        // so we can handle C,U,D differently.
        for (documentChange in querySnapshot.documentChanges) {
            val post = Post.fromSnapshot(documentChange.document)
            when (documentChange.type) {
                DocumentChange.Type.ADDED -> {
                    Log.d(Constants.TAG, "Adding $post")
                    posts.add(0, post)
                    notifyItemInserted(0)
                }
                DocumentChange.Type.REMOVED -> {
                    Log.d(Constants.TAG, "Removing $post")
                    val index = posts.indexOfFirst { it.id == post.id }
                    posts.removeAt(index)
                    notifyItemRemoved(index)
                }
                DocumentChange.Type.MODIFIED -> {
                    Log.d(Constants.TAG, "Modifying $post")
                    val index = posts.indexOfFirst { it.id == post.id }
                    posts[index] = post
                    notifyItemChanged(index)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.post_card_view, parent, false)
        return PostViewHolder(view, this, context)
    }

    override fun getItemCount(): Int = posts.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    fun selectPostAt(adapterPosition: Int) {
        listener?.onPostSelected(posts[adapterPosition])
    }

    fun add(post: Post) {
        postReference.add(post)
    }

    private fun edit(position: Int, post: Post) {
        postReference.document(posts[position].id).set(posts[position])
    }

    fun showAddDialog() {
        val builder = AlertDialog.Builder(context)
        //Set options
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_create, null, false)
        builder.setView(view)

        view.add_image_button.setOnClickListener {

        }

        builder.setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
            val title = view.title_edit_text.text.toString()
            val body = view.description_edit_text.text.toString()
            val categoryName = view.category_spinner.selectedItem.toString()
            val category = Category(categoryName)

            val post = Post(title, body, category, uid)
            add(post)

        }
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.create().show()
    }

    fun showEditDialog(position: Int) {

        val targetPost = posts[position]
        if (targetPost.uid != uid)
            return

        val builder = AlertDialog.Builder(context)
        //Set options
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_edit, null, false)
        view.title_edit_text.setText(targetPost.title)
        view.description_edit_text.setText(targetPost.body)

        val categoryArray = context.resources.getStringArray(R.array.category_array)
        val catPos = categoryArray.indexOfFirst {
            it == targetPost.category.name
        }
        view.category_spinner.setSelection(catPos)

        builder.setView(view)
        builder.setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
            val title = view.title_edit_text.text.toString()
            val body = view.description_edit_text.text.toString()
            val categoryName = view.category_spinner.selectedItem.toString()
            val category = Category(categoryName)
            
            targetPost.title = title
            targetPost.body = body
            targetPost.category = category

            edit(position, targetPost)
        }
        builder.setNeutralButton(context.resources.getString(R.string.delete_post)) { _, _ ->

            showConfirmationDialog(targetPost.id)

        }
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.create().show()
    }

    private fun showConfirmationDialog(id: String) {

        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.are_you_sure))
        builder.setMessage(context.getString(R.string.confirmation_message))

        builder.setNegativeButton(android.R.string.no, null)
        builder.setPositiveButton(android.R.string.yes) { _, _ ->
            deleteAllPostComments(id)
            postReference.document(id).delete()
        }

        builder.create().show()

    }

    private fun deleteAllPostComments(id: String) {
        val commentsReference = FirebaseFirestore
            .getInstance()
            .collection(Constants.COMMENTS_COLLECTION)

        commentsReference.whereEqualTo(Comment.POST_ID_KEY, id).get().addOnSuccessListener {
            for (item in it) {
                commentsReference.document(item.id).delete()
            }
        }


    }

}
