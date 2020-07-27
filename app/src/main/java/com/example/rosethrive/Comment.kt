package com.example.rosethrive

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Comment(var body:String, var uid:String) : Parcelable