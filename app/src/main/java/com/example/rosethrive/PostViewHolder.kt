package com.example.rosethrive

import android.content.Context
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.post_card_view.view.*

class PostViewHolder(itemView: View, adapter: PostsAdapter, var context: Context) :
    RecyclerView.ViewHolder(itemView) {

    private var titleTextView = itemView.post_title_text_view
    private var bodyTextView = itemView.post_body_preview_text_view
    private var ownerTextView = itemView.post_owner_text_view
    private var postIndicatorImageView = itemView.post_category_indicator

    init {
        itemView.setOnClickListener {
            adapter.selectPostAt(adapterPosition)
        }
        itemView.setOnLongClickListener {
            adapter.showEditDialog(adapterPosition)
            true
        }
    }

    fun bind(post: Post) {
        titleTextView.text = post.title
        ownerTextView.text = post.uid
        if (post.body.length <= 75) {
            bodyTextView.text = post.body
        } else {
            bodyTextView.text = post.body.subSequence(0..75)
        }
        setCategoryImage(post)
    }

    private fun setCategoryImage(post: Post) {

        var color: Int = android.R.color.holo_red_light
        var drawable = android.R.drawable.stat_notify_error
        when (post.category.name) {
            "Books for Sale" -> {
                color = R.color.colorBooksForSale
                drawable = R.drawable.books_for_sale
            }
            "Lofts for Sale" -> {
                color = R.color.colorLoftForSale
                drawable = R.drawable.loft_for_sale
            }
            "Furniture for Sale" -> {
                color = R.color.colorFurnitureForSale
                drawable = R.drawable.furniture_for_sale
            }
            "Cars/Bikes for Sale" -> {
                color = R.color.colorCarsBikesForSale
                drawable = R.drawable.cars_for_sale
            }
            "Electronics for Sale" -> {
                color = R.color.colorElectronicsForSale
                drawable = R.drawable.electronics_for_sale
            }
            "General for Sale" -> {
                color = R.color.colorGeneralForSale
                drawable = R.drawable.general_for_sale
            }
            "Ride Share" -> {
                color = R.color.colorRideShare
                drawable = R.drawable.ride_share
            }
            "Housing" -> {
                color = R.color.colorHousing
                drawable = R.drawable.housing
            }
            "Services (Tutoring)" -> {
                color = R.color.colorServicesTutoring
                drawable = R.drawable.serivces_tutoring
            }
            "Wanted" -> {
                color = R.color.colorWanted
                drawable = R.drawable.wanted
            }
            "Lost & Found" -> {
                color = R.color.colorLostAndFound
                drawable = R.drawable.lost_and_found
            }
            "Announcements" -> {
                color = R.color.colorAnnouncements
                drawable = R.drawable.announcements
            }
            "Community" -> {
                color = R.color.colorCommunity
                drawable = R.drawable.community
            }
        }

        Log.d(Constants.TAG, "CAT: ${post.category.name}, COLOR: $color")
        postIndicatorImageView.setBackgroundColor( context.getColor(color) )
        postIndicatorImageView.setImageDrawable(context.getDrawable(drawable))
    }
}