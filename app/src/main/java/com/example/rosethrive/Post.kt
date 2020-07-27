package com.example.rosethrive

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Post(var title:String, var body:String, var category:Category, val uid:String, var comments:ArrayList<Comment> = ArrayList()) :
    Parcelable