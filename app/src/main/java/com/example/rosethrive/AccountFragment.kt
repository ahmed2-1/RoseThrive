package com.example.rosethrive

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_account.view.*

private const val ARG_UID = "uid"


class AccountFragment : Fragment() {

    private var listener: MainListener? = null
    private var uid: String? = null
    private lateinit var adapter: PostsAdapter

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
        val view =
            inflater.inflate(R.layout.fragment_account, container, false)

        view.account_name_text_view.text = uid
        view.account_email_text_view.text = "$uid@rose-hulman.edu"

        adapter = PostsAdapter(requireContext(), listener, uid!!, true)
        adapter.addSnapshotListener()

        view.account_posts_recycler_view.adapter = adapter
        view.account_posts_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        view.account_posts_recycler_view.setHasFixedSize(true)
        return view
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
        setHasOptionsMenu(true)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(com.example.rosethrive.R.id.account_link).isEnabled = false
    }

    companion object {
        const val ARG_NAME = "AccountFragment"

        @JvmStatic
        fun newInstance(uid: String) =
            AccountFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_UID, uid)
                }
            }
    }
}