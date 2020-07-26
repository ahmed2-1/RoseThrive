package com.example.rosethrive

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_post_elements.view.*

class PostAdapter(var context: Context, var listener: MainListener?) :
    RecyclerView.Adapter<PostViewHolder>() {
    var posts = ArrayList<Post>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.post_card_view, parent, false)
        return PostViewHolder(view, this, context)
    }

    override fun getItemCount(): Int = posts.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    fun selectPostAt(adapterPosition: Int) {
        listener?.posts = posts
        listener?.onPostSelected(posts[adapterPosition])
    }

    fun add(post: Post) {
        posts.add(0, post)
        notifyItemInserted(0)
    }

    private fun edit(position:Int, post: Post) {
        posts[position] = post
        notifyItemChanged(position)
    }

    fun showAddDialog() {
        val builder = AlertDialog.Builder(context)
        //Set options
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_create, null, false)
        builder.setView(view)
        builder.setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
            val title = view.title_edit_text.text.toString()
            val body = view.description_edit_text.text.toString()
            val categoryName = view.category_spinner.selectedItem.toString()
            val category = Category(categoryName, 2)
            val uid = "TestAdder"

            val post = Post(title, body, category, uid)
            add(post)

        }
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.create().show()
    }

    fun showEditDialog(position: Int) {
        val builder = AlertDialog.Builder(context)
        //Set options
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_edit, null, false)
        view.title_edit_text.setText(posts[position].title)
        view.description_edit_text.setText(posts[position].body)

        val categoryArray=context.resources.getStringArray(R.array.category_array)
        val catPos = categoryArray.indexOfFirst {
            it.equals(posts[position].category.name)
        }
        view.category_spinner.setSelection(catPos)

        builder.setView(view)
        builder.setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
            val title = view.title_edit_text.text.toString()
            val body = view.description_edit_text.text.toString()
            val categoryName = view.category_spinner.selectedItem.toString()
            val category = Category(categoryName, 2)
            val uid = "TestAdder"

            val post = Post(title, body, category, uid)
            edit(position, post)
        }
        builder.setNeutralButton(context.resources.getString(R.string.delete_post)){_, _ ->
            posts.removeAt(position)
            notifyItemRemoved(position)
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.create().show()
    }



}
