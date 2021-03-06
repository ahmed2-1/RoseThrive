package com.example.rosethrive

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Reply(var body:String="", var uid:String="") : Parcelable{
    @get:Exclude
    var id = ""

    companion object {
        const val LAST_TOUCHED_KEY = "lastTouched"

        fun fromSnapshot(snapshot: DocumentSnapshot): Comment {
            val comment = snapshot.toObject(Comment::class.java)!!
            comment.id = snapshot.id
            return comment
        }
    }
}
