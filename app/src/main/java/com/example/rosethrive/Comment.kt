package com.example.rosethrive

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Comment(var body:String="", var uid:String="", var postID:String = "", var replies:ArrayList<Reply> = ArrayList()) : Parcelable {
    @get:Exclude
    var id = ""
    @ServerTimestamp
    var lastTouched: Timestamp? = null

    companion object {
        const val LAST_TOUCHED_KEY = "lastTouched"
        const val POST_ID_KEY = "postID"
        const val UID_KEY = "uid"

        fun fromSnapshot(snapshot: DocumentSnapshot): Comment {
            val comment = snapshot.toObject(Comment::class.java)!!
            comment.id = snapshot.id
            return comment
        }
    }
}