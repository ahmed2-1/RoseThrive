package com.example.rosethrive

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.post_card_view.view.*

class PostViewHolder: RecyclerView.ViewHolder {
    lateinit var context: Context

    var titleTextView = itemView.post_title_text_view
    var bodyTextView = itemView.post_body_preview_text_view

    constructor(itemView: View, adapter: PostAdapter, context: Context): super(itemView){
        this.context = context
        itemView.setOnClickListener{
            adapter.selectPostAt(adapterPosition)
        }
        itemView.setOnLongClickListener {
            adapter.showEditDialog(adapterPosition)
            true
        }
    }

    fun bind(post: Post) {
        titleTextView.text = post.title
        if(post.body.length <= 75){
            bodyTextView.text = post.body
        }
        else{
            bodyTextView.text = post.body.subSequence(0..75)
        }
    }
}