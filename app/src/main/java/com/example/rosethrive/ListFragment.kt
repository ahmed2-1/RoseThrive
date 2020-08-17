package com.example.rosethrive

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

private const val ARG_UID = "uid"

class ListFragment : Fragment() {
    private var listener: MainListener? = null
    private lateinit var adapter: PostsAdapter

    private var uid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uid = it.getString(ARG_UID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val recyclerView =
            inflater.inflate(R.layout.fragment_list, container, false) as RecyclerView

//        Log.d(Constants.TAG, "ADAPTER CREATED!")
        adapter = PostsAdapter(requireContext(), listener, uid!!, false)
        adapter.addSnapshotListener()

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        requireActivity().findViewById<FloatingActionButton>(R.id.fab).visibility = View.VISIBLE
        requireActivity().findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            adapter.showAddDialog()
        }

        return recyclerView
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ARG_UID, uid)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnPhotoSelectedListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        const val ARG_NAME = "ListFragment"

        @JvmStatic
        fun newInstance(uid: String) =
            ListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_UID, uid)
                }
            }
    }
}
