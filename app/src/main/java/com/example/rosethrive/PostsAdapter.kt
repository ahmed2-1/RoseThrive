package com.example.rosethrive

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.content_post_elements.view.*
import java.io.ByteArrayOutputStream
import kotlin.math.abs
import kotlin.random.Random

class PostsAdapter(
    var context: Context,
    private var listener: MainListener?,
    val uid: String,
    private val isAccount: Boolean
) :
    RecyclerView.Adapter<PostViewHolder>(),
    MainActivity.ImageListener {
    private var posts = ArrayList<Post>()
    private var newPostImages: ArrayList<String> = ArrayList()

    private val postReference = FirebaseFirestore
        .getInstance()
        .collection(Constants.POSTS_COLLECTION)

    private var storageRef = FirebaseStorage.getInstance().reference.child("images")

    fun addSnapshotListener() {
        val query = if (isAccount) {
            postReference.orderBy(Post.LAST_TOUCHED_KEY, Query.Direction.ASCENDING)
                .whereEqualTo(Post.UID_KEY, uid)
        } else {
            postReference.orderBy(Post.LAST_TOUCHED_KEY, Query.Direction.ASCENDING)
        }

        query.addSnapshotListener {
        querySnapshot, e ->
        if (e != null) {
            Log.w(Constants.TAG, "listen error", e)
        } else {
            processSnapshotChanges(querySnapshot!!)
        }
    }

    }

    private fun processSnapshotChanges(querySnapshot: QuerySnapshot) {
        // Snapshots has documents and documentChanges which are flagged by type,
        // so we can handle C,U,D differently.
        for (documentChange in querySnapshot.documentChanges) {
            val post = Post.fromSnapshot(documentChange.document)
            when (documentChange.type) {
                DocumentChange.Type.ADDED -> {
//                    Log.d(Constants.TAG, "Adding $post")
                    posts.add(0, post)
                    notifyItemInserted(0)
                }
                DocumentChange.Type.REMOVED -> {
//                    Log.d(Constants.TAG, "Removing $post")
                    val index = posts.indexOfFirst { it.id == post.id }
                    posts.removeAt(index)
                    notifyItemRemoved(index)
                }
                DocumentChange.Type.MODIFIED -> {
//                    Log.d(Constants.TAG, "Modifying $post")
                    val index = posts.indexOfFirst { it.id == post.id }
                    posts[index] = post
                    notifyItemChanged(index)
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.post_card_view, parent, false)
        return PostViewHolder(view, this, context)
    }

    override fun getItemCount(): Int = posts.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    fun selectPostAt(adapterPosition: Int) {
        listener?.onPostSelected(posts[adapterPosition])
    }

    private fun add(post: Post) {
        postReference.add(post)
    }

    private fun edit(position: Int) {
        postReference.document(posts[position].id).set(posts[position])
    }

    fun showAddDialog() {
        val builder = AlertDialog.Builder(context)
        //Set options
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_create, null, false)
        builder.setView(view)

        val adapter = DialogImageAdapter(context)
        view.image_grid.adapter = adapter

        view.add_image_button.setOnClickListener {
            listener?.showPictureDialog(this) { location ->
                Log.d(Constants.TAG, "Location: $location")
                val bitmap = when {
                    location.startsWith("content") -> {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, Uri.parse(location))
                    }
                    location.startsWith("/storage") -> {
                        BitmapFactory.decodeFile(location)
                    }
                    else -> throw IllegalStateException("File is in an invalid location")
                }
                Log.d(Constants.TAG, "Bitmap: $bitmap")
                adapter.add(bitmap)
            }
        }

        builder.setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
            val title = view.title_edit_text.text.toString()
            val body = view.description_edit_text.text.toString()
            val categoryName = view.category_spinner.selectedItem.toString()
            val category = Category(categoryName)
            val post = Post(title, body, category, uid)

            uploadPost(post) {
                add(post)
                newPostImages.clear()
            }

        }
        builder.setNegativeButton(android.R.string.cancel) { _: DialogInterface?, _: Int ->
            newPostImages.clear()
        }
        builder.create().show()
    }


    fun showEditDialog(position: Int) {

        val targetPost = posts[position]
        if (targetPost.uid != uid)
            return

        val builder = AlertDialog.Builder(context)
        //Set options
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_edit, null, false)
        view.title_edit_text.setText(targetPost.title)
        view.description_edit_text.setText(targetPost.body)

        val categoryArray = context.resources.getStringArray(R.array.category_array)
        val catPos = categoryArray.indexOfFirst {
            it == targetPost.category.name
        }
        view.category_spinner.setSelection(catPos)

        builder.setView(view)
        builder.setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
            val title = view.title_edit_text.text.toString()
            val body = view.description_edit_text.text.toString()
            val categoryName = view.category_spinner.selectedItem.toString()
            val category = Category(categoryName)

            targetPost.title = title
            targetPost.body = body
            targetPost.category = category

            uploadPost(targetPost) {
                edit(position)
                newPostImages.clear()
            }

        }

        val adapter = DialogImageAdapter(context)
        view.image_grid.adapter = adapter

        val target = RecursiveTarget(targetPost.imageDownloadURI, adapter)

        target.execute()
//        for (uri in targetPost.imageDownloadURI) {
//            Log.d(Constants.TAG, "Loaded URI: $uri")
//            Picasso.get()
//                .load(uri)
//                .into()
//        }

        view.add_image_button.setOnClickListener {
            listener?.showPictureDialog(this) { location ->
                Log.d(Constants.TAG, "Location: $location")
                val bitmap = when {
                    location.startsWith("content") -> {
                        MediaStore.Images.Media.getBitmap(context.contentResolver, Uri.parse(location))
                    }
                    location.startsWith("/storage") -> {
                        BitmapFactory.decodeFile(location)
                    }
                    else -> throw IllegalStateException("File is in an invalid location")
                }
                Log.d(Constants.TAG, "Bitmap: $bitmap")
                adapter.add(bitmap)
            }
        }

        builder.setNeutralButton(context.resources.getString(R.string.delete_post)) { _, _ ->

            showConfirmationDialog(targetPost.id)

        }
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.create().show()
    }

    inner class RecursiveTarget(
        private var downloadUris: List<String>,
        private var adapter: DialogImageAdapter
    ) : Target {

        private var index = 0

        fun execute() {
            downloadIndex(index)
        }

        private fun downloadIndex(index: Int) {
            if (index < downloadUris.size)
                Picasso.get()
                    .load(downloadUris[index])
                    .into(this)
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            // Empty
        }

        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            // Empty
        }

        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            if (bitmap != null) {
                adapter.add(bitmap)
            }
            downloadIndex(++index)
        }

    }

    private fun showConfirmationDialog(id: String) {

        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.are_you_sure))
        builder.setMessage(context.getString(R.string.confirmation_message))

        builder.setNegativeButton(android.R.string.no, null)
        builder.setPositiveButton(android.R.string.yes) { _, _ ->
            deleteAllPostComments(id)
            postReference.document(id).delete()
        }

        builder.create().show()

    }

    private fun deleteAllPostComments(id: String) {
        val commentsReference = FirebaseFirestore
            .getInstance()
            .collection(Constants.COMMENTS_COLLECTION)

        commentsReference.whereEqualTo(Comment.POST_ID_KEY, id).get().addOnSuccessListener {
            for (item in it) {
                commentsReference.document(item.id).delete()
            }
        }
    }

    override fun handleImage(location: String) {
        newPostImages.add(location)
    }

    private fun uploadPost(post: Post, onCompleteListener: () -> Unit = {}) {

        val byteArrays = newPostImages.map { location ->
            val bitmap = BitmapUtils.rotateAndScaleByRatio(context, location, 1)
            val baos = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            baos.toByteArray()
        }

        if (byteArrays.isNotEmpty())
            uploadImagesAndThen(byteArrays, post, byteArrays.size - 1, onCompleteListener)
        else onCompleteListener()

    }

    private fun uploadImagesAndThen(
        byteArray: List<ByteArray>,
        post: Post,
        index: Int,
        onCompleteListener: () -> Unit
    ) {

        val id = abs(Random.nextLong()).toString()

        val uploadTask: UploadTask = storageRef.child(id).putBytes(byteArray[index])
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    Log.d(Constants.TAG, "Task failed")
                    Log.e(Constants.TAG, it.toString())
                    throw it
                }
            }
            storageRef.child(id).downloadUrl
        }.addOnCompleteListener { task ->
            Log.d(Constants.TAG, "Task complete")

            if (task.isSuccessful) {
                val downloadUri = task.result
                post.imageDownloadURI.add(downloadUri.toString())

                if (index > 0) {
                    uploadImagesAndThen(byteArray, post, index - 1, onCompleteListener)
                } else {
                    onCompleteListener()
                }
            }
        }
    }

}
