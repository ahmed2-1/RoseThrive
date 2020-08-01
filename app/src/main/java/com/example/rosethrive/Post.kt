package com.example.rosethrive

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Post(var title:String = "", var body:String = "", var category:Category = Category("", 0), val uid:String = "") :
    Parcelable{
    @get:Exclude
    var id = ""
    @ServerTimestamp
    var lastTouched: Timestamp? = null

    companion object {
        const val LAST_TOUCHED_KEY = "lastTouched"

        fun fromSnapshot(snapshot: DocumentSnapshot): Post {
            val post = snapshot.toObject(Post::class.java)!!
            post.id = snapshot.id
            return post
        }
    }
}