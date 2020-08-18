package com.example.rosethrive

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_view_post.view.*
import kotlinx.android.synthetic.main.view_image_dialog.view.*

private const val ARG_UID = "uid"
private const val ARG_POST = "post"

class PostFragment : Fragment() {
    private var post: Post? = null
    private lateinit var uid: String
    private lateinit var adapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uid = it.getString(ARG_UID).toString()
            post = it.getParcelable(ARG_POST)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_view_post, container, false)
        view.post_title_text_view.text = post?.title
        view.post_category_text_view.text = post?.category?.name
        view.poster_text_view.text = post?.uid
        view.post_body_text_view.text = post?.body
        Log.d(Constants.TAG, "Post, $post")
        if(post != null) {
            Log.d(Constants.TAG, "Post is not null, ${post!!.id}")
            for(image in post!!.imageDownloadURI){
                val imageView = ImageView(context)
                imageView.setOnClickListener {
                    showImageDialog(image)
                }
                Picasso.get()
                    .load(image)
                    .into(imageView)
                view.image_button_linear_layout.addView(imageView)
            }
            adapter = CommentAdapter(uid, requireContext(), post!!)
            adapter.addSnapshotListener()
        }

        val recycler = view.replies_recycler_view
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(requireContext())

        requireActivity().findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            adapter.showCommentDialog()
        }
        return view
    }

    private fun showImageDialog(image: String) {
        val builder =
            AlertDialog.Builder(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val view = LayoutInflater.from(context).inflate(R.layout.view_image_dialog, null, false)
        builder.setView(view)
        Picasso.get()
            .load(image)
            .into(view.view_image_zoomable)
        builder.create().show()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(ARG_UID, uid)
        outState.putParcelable(ARG_POST, post)
    }

    companion object {
        const val ARG_NAME = "PostFragment"

        @JvmStatic
        fun newInstance(uid: String, post: Post) =
            PostFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_UID, uid)
                    putParcelable(ARG_POST, post)
                }
            }
    }
}