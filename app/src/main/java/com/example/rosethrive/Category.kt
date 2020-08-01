package com.example.rosethrive

import android.graphics.Color
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Category(val name:String = "", val color:Int = 0) : Parcelable
