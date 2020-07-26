package com.example.rosethrive

interface MainListener {
    var posts: ArrayList<Post>
    fun onPostSelected(post: Post)

    fun onCreatePostRequest(function: (Post) -> Unit)
}