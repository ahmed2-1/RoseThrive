package com.example.rosethrive

import android.os.Parcelable
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Settings(
    var uid: String = "",
    var newPostAdded: Boolean = false,
    var newReplyToYourPost: Boolean = false,
    var newReplyToSubbedPost: Boolean = false,
    var editToSubbedPost: Boolean = false,
    var subbedPostDeleted: Boolean = false
) :
    Parcelable {
    @get:Exclude
    var id = ""
    companion object {
        const val UID_KEY = "uid"
        const val NEW_POST_KEY = "newPostAdded"
        const val NEW_REPLY_KEY = "newReplyToYourPost"
        const val NEW_REPLY_SUBBED_KEY = "newReplyToSubbedPost"
        const val EDIT_SUBBED_KEY = "editToSubbedPost"
        const val DELETE_SUBBED_KEY = "subbedPostDeleted"

        fun fromSnapshot(snapshot: DocumentSnapshot): Settings {
            val settings = snapshot.toObject(Settings::class.java)!!
            settings.id = snapshot.id
            return settings
        }
    }
}