package com.example.rosethrive

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.reply_card_view.view.*

class CommentViewHolder:RecyclerView.ViewHolder {
    val context:Context
    val nameTextView = itemView.replier_name_text_view
    val bodyTextView = itemView.reply_body_text_view

    constructor(itemView: View, adapter: CommentAdapter, context: Context): super(itemView){
        this.context = context
    }
    fun bind(comment: Comment) {
        nameTextView.text = comment.uid
        bodyTextView.text = comment.body
    }
}