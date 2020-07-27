package com.example.rosethrive

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.content_post_elements.view.*
import kotlinx.android.synthetic.main.fragment_view_post.view.*

private const val ARG_POST = "post"

class ViewPostFragment : Fragment() {
    private var post: Post? = null
    private lateinit var adapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
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

        if(post != null) {
            adapter = CommentAdapter(requireContext(), post!!)
        }

        val recycler = view.replies_recycler_view
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(requireContext())

        requireActivity().findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            adapter.showReplyDialog()
        }
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(post: Post) =
            ViewPostFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_POST, post)
                }
            }
    }
}