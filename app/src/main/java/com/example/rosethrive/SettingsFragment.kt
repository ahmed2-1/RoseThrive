package com.example.rosethrive

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_settings.view.*

private const val ARG_UID = "uid"

class SettingsFragment : Fragment() {

    private var uid: String? = null
    private var settings: Settings = Settings()

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
        requireActivity().findViewById<FloatingActionButton>(R.id.fab).visibility = View.GONE
        settingsReference.whereEqualTo(Settings.UID_KEY, uid).get().addOnSuccessListener {
            if (it.size() == 1) {
                settings = Settings.fromSnapshot(it.documents[0])
                Log.d(Constants.TAG, "Settings retrieved: $settings")
                setSwitchesInit(view)
            }
            else{
                settings.uid = uid!!
                settingsReference.add(settings)
            }
        }

        view.new_post_added_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            settings.newPostAdded = isChecked
            settingsReference.document(settings.id).update(Settings.NEW_POST_KEY, isChecked)
        }
        view.new_reply_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            settings.newReplyToYourPost = isChecked
            settingsReference.document(settings.id).update(Settings.NEW_REPLY_KEY, isChecked)
        }
        view.new_reply_to_sub_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            settings.newReplyToSubbedPost = isChecked
            settingsReference.document(settings.id).update(Settings.NEW_REPLY_SUBBED_KEY, isChecked)
        }
        view.edit_to_sub_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            settings.editToSubbedPost = isChecked
            settingsReference.document(settings.id).update(Settings.EDIT_SUBBED_KEY, isChecked)
        }
        view.deleted_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            settings.subbedPostDeleted = isChecked
            settingsReference.document(settings.id).update(Settings.DELETE_SUBBED_KEY, isChecked)
        }
        return view
    }

    private fun setSwitchesInit(view: View) {
        view.new_post_added_switch.isChecked = settings.newPostAdded
        view.new_reply_switch.isChecked = settings.newReplyToYourPost
        view.new_reply_to_sub_switch.isChecked = settings.newReplyToSubbedPost
        view.edit_to_sub_switch.isChecked = settings.editToSubbedPost
        view.deleted_switch.isChecked = settings.subbedPostDeleted
        Log.d(Constants.TAG, "First switch checked: ${settings.newPostAdded}")
        Log.d(Constants.TAG, "First switch checked: ${view.new_post_added_switch.isChecked}")
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        if ((settings.id == null) or (settings.id.isEmpty())) {
//            settings.uid = uid!!
//            settingsReference.add(settings)
//        } else {
//            settingsReference.document(settings.id).set(settings)
//            Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT)
//        }
//    }

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