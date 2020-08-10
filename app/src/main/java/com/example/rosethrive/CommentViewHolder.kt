package com.example.rosethrive

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.comment_card_view.view.*


class CommentViewHolder:RecyclerView.ViewHolder {
    val context:Context
    val nameTextView = itemView.replier_name_text_view
    val bodyTextView = itemView.reply_body_text_view
    val replyLinearLayout = itemView.comments_linear_layout

    constructor(itemView: View, adapter: CommentAdapter, context: Context): super(itemView){
        this.context = context
        itemView.setOnLongClickListener {
            adapter.showReplyDialog(adapterPosition)
            true
        }
    }

    fun bind(comment: Comment) {
        nameTextView.text = comment.uid
        bodyTextView.text = comment.body
        replyLinearLayout.removeAllViews()
        for(reply in comment.replies){
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.reply_card_view, null)
            view.replier_name_text_view.text = reply.uid
            view.reply_body_text_view.text = reply.body
            replyLinearLayout.addView(view)
        }
    }
}