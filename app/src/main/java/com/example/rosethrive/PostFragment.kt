package com.example.rosethrive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_view_post.view.*

private const val ARG_UID = "uid"
private const val ARG_POST = "post"

class PostFragment() : Fragment() {
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

        if(post != null) {
            adapter = CommentAdapter(uid, requireContext(), post!!)
            adapter.addSnapshotListener()
        }

        val recycler = view.replies_recycler_view
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(requireContext())

        requireActivity().findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            adapter.showCommentDialog()
        }
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(uid : String, post: Post) =
            PostFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_UID, uid)
                    putParcelable(ARG_POST, post)
                }
            }
    }
}