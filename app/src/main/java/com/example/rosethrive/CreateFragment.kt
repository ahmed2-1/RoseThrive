package com.example.rosethrive

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.content_post_elements.view.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class CreateFragment() : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var listener: MainListener? = null
    private var post:Post? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_create, container, false)
        requireActivity().findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            val postTitle = view.title_edit_text.text.toString()
            val postBody:String = view.description_edit_text.text.toString()
            val categoryName:String = view.category_spinner.selectedItem as String
            val category = Category(categoryName, 1)
            val uid = "TestUser"
            post = Post(postTitle, postBody, category, uid)
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is MainListener){
            listener = context
        }
        else{
            throw RuntimeException("$context must implement OnPhotoSelectedListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(
            param1: String,
            param2: String,
            function: (Post) -> Unit
        ) =
                CreateFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }

}