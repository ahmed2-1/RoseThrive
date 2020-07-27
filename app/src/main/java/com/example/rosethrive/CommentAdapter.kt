package com.example.rosethrive

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_post_elements.view.*
import kotlinx.android.synthetic.main.fragment_reply.view.*

class CommentAdapter(var context: Context, val post:Post): RecyclerView.Adapter<CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.reply_card_view, parent, false)
        return CommentViewHolder(view, this, context)
    }

    override fun getItemCount() = post.comments.size

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(post.comments[position])
    }

    fun showReplyDialog() {
        val builder = AlertDialog.Builder(context)
        //Set options
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_reply, null, false)
        builder.setView(view)
        builder.setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
            val body = view.comment_body_edit_text.text.toString()

            val comment = Comment(body, "testUser")
            post.comments.add(0, comment)
            notifyItemInserted(0)
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.create().show()
    }
}