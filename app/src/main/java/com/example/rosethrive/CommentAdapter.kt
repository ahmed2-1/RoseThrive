package com.example.rosethrive

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_reply.view.*

class CommentAdapter(private val uid: String, var context: Context, val post:Post): RecyclerView.Adapter<CommentViewHolder>() {

    private var comments = ArrayList<Comment>()

    private val commentsReference = FirebaseFirestore
        .getInstance()
        .collection(Constants.COMMENTS_COLLECTION)


    private lateinit var listenerRegistration: ListenerRegistration

    fun addSnapshotListener() {
        listenerRegistration = commentsReference
            .orderBy(Comment.LAST_TOUCHED_KEY, Query.Direction.ASCENDING)
            .whereEqualTo(Comment.POST_ID_KEY, post.id)
            .addSnapshotListener { querySnapshot, e ->
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
            val comment = Comment.fromSnapshot(documentChange.document)
            when (documentChange.type) {
                DocumentChange.Type.ADDED -> {
//                    Log.d(Constants.TAG, "Adding $comment")
                    comments.add(0, comment)
                    notifyItemInserted(0)
                }
                DocumentChange.Type.REMOVED -> {
//                    Log.d(Constants.TAG, "Removing $comment")
                    val index = comments.indexOfFirst { it.id == comment.id }
                    comments.removeAt(index)
                    notifyItemRemoved(index)
                }
                DocumentChange.Type.MODIFIED -> {
//                    Log.d(Constants.TAG, "Modifying $comment")
                    val index = comments.indexOfFirst { it.id == comment.id }
                    comments[index] = comment
                    notifyItemChanged(index)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.comment_card_view, parent, false)
        return CommentViewHolder(view, this, context)
    }

    override fun getItemCount() = comments.size

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    fun showCommentDialog() {
        val builder = AlertDialog.Builder(context)
        //Set options
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_reply, null, false)
        builder.setView(view)
        builder.setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
            val body = view.comment_body_edit_text.text.toString()

            val comment = Comment(body, uid, post.id)
            commentsReference.add(comment)
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.create().show()
    }

    fun showReplyDialog(adapterPosition: Int) {
        val comment = comments[adapterPosition]
        val builder = AlertDialog.Builder(context)
        //Set options
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_reply, null, false)
        builder.setView(view)
        builder.setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
            val body = view.comment_body_edit_text.text.toString()

            val reply = Reply(body, uid)
            comment.replies.add(reply)
            commentsReference.document(comment.id).set(comment)
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.create().show()
    }
}