package com.example.rosethrive

interface MainListener {
    fun onPostSelected(post: Post)

    fun onCreatePostRequest(function: (Post) -> Unit)

    fun showPictureDialog(
        listener: MainActivity.ImageListener,
        onPictureSelected: (String) -> Unit
    )
}