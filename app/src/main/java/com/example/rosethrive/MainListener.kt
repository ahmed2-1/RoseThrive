package com.example.rosethrive

interface MainListener {
    fun onPostSelected(post: Post)

    fun showPictureDialog(
        listener: MainActivity.ImageListener,
        onPictureSelected: (String) -> Unit
    )
}