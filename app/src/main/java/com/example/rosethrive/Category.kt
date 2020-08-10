package com.example.rosethrive

import android.content.res.Resources
import android.graphics.Color
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.lang.Exception
import java.lang.IllegalArgumentException

@Parcelize
data class Category(val name: String = "") : Parcelable
