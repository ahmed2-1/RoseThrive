package com.example.rosethrive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_settings.view.*

private const val ARG_UID = "uid"

class SettingsFragment : Fragment() {

    private var uid: String? = null

    val settingsReference = FirebaseFirestore
        .getInstance()
        .collection(Constants.SETTINGS_COLLECTION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uid = it.getString(ARG_UID)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ARG_UID, uid)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        view.new_post_added_switch.setOnCheckedChangeListener { buttonView, isChecked ->

        }

        return view
    }

    companion object {
        const val ARG_NAME = "SettingsFragment"

        @JvmStatic
        fun newInstance(uid: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_UID, uid)
                }
            }
    }
}