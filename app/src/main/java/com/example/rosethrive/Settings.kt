package com.example.rosethrive

import android.os.Parcelable
import com.google.firebase.firestore.DocumentSnapshot
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

    companion object {
        fun fromSnapshot(snapshot: DocumentSnapshot): Settings {
            return snapshot.toObject(Settings::class.java)!!
        }
    }
}