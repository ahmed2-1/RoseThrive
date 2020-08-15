package com.example.rosethrive

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout

class DialogImageAdapter(
    context: Context,
    newPostImages: ArrayList<String>
) : BaseAdapter() {
    val context = context
    var images = ArrayList<Bitmap>()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val imageView = ImageView(context)
        imageView.setImageBitmap(images[position])
        var layoutParams = LinearLayout.LayoutParams(600, 600)
        imageView.layoutParams = layoutParams
        return imageView
    }

    override fun getItem(position: Int):Bitmap = images[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount() = images.size

    fun add(bitmap: Bitmap){
        images.add(bitmap)
        notifyDataSetChanged()
    }
}
