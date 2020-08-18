package com.example.rosethrive

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlin.random.Random


object NotificationService {


    var isListeningPosts: Boolean = false
    var isListeningComments: Boolean = false

    var uid: String = ""
    private var context: Context? = null
    private var settings: Settings = Settings()

    private val postReference = FirebaseFirestore
        .getInstance()
        .collection(Constants.POSTS_COLLECTION)

    private val commentsReference = FirebaseFirestore
        .getInstance()
        .collection(Constants.COMMENTS_COLLECTION)

    private val settingsReference = FirebaseFirestore
        .getInstance()
        .collection(Constants.SETTINGS_COLLECTION)

    private fun createNotificationChannel() {
        if (context != null) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = context!!.getString(R.string.channel_name)
                val descriptionText = context!!.getString(R.string.channel_desc)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(Constants.CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                // Register the channel with the system
                val notificationManager: NotificationManager =
                    context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    fun makeNotification(textTitle: String, textContent: String, post: Post?) {
        if (context != null) {
            val intent = Intent(context, MainActivity::class.java).apply {
                //flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                this.putExtra("postToLoad", post)
            }
            val pendingIntent: PendingIntent = addBackStack(context!!, intent)!!

            var builder = NotificationCompat.Builder(context!!, Constants.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(textTitle)
                .setContentText(textContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationId = Random.nextInt()

            with(NotificationManagerCompat.from(context!!)) {
                // notificationId is a unique int for each notification that you must define
                notify(notificationId, builder.build())
            }
        }
    }

    fun addBackStack(context: Context, intent: Intent): PendingIntent? {
        val stackBuilder: TaskStackBuilder =
            TaskStackBuilder.create(context.applicationContext)
        stackBuilder.addNextIntentWithParentStack(intent)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun setContext(newContext: Context) {
        context = newContext
    }

    fun setUID(newUID: String) {
        uid = newUID
    }

    fun initialize() {
        createNotificationChannel()

        settingsReference.whereEqualTo(Settings.UID_KEY, uid).addSnapshotListener{ querySnapshot, e ->
            if (e != null) {
                Log.w(Constants.TAG, "listen error", e)
            } else {
                if ((querySnapshot != null) and (querySnapshot?.size() == 1)) {
                    settings = Settings.fromSnapshot(querySnapshot!!.documents[0])
                }
            }
        }

        if (!isListeningPosts) {
            postReference.addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    Log.w(Constants.TAG, "listen error", e)
                } else {
                    sendPostChangesNotifications(querySnapshot!!)
                }
            }

            commentsReference.addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    Log.w(Constants.TAG, "listen error", e)
                } else {
                    sendCommentChangesNotifications(querySnapshot!!)
                }
            }
        }
        Log.d(Constants.TAG, settings.toString())
    }

    private fun sendPostChangesNotifications(querySnapshot: QuerySnapshot) {
        if (isListeningPosts)
            for (documentChange in querySnapshot.documentChanges) {
                val post = Post.fromSnapshot(documentChange.document)
                if (post.uid != uid) {
                    when (documentChange.type) {
                        DocumentChange.Type.ADDED -> {
                            if (settings.newPostAdded) {
                                makeNotification(context!!.getString(R.string.new_post), post.title, post)
                            }
                        }
                        DocumentChange.Type.REMOVED -> {
                            notifyDelete(post)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            notifyEdited(post)
                        }
                    }
                }
            }
        else
            startListeningToPosts()
    }

    private fun notifyDelete(post: Post) {
        if(settings.subbedPostDeleted){
            commentsReference.whereEqualTo(Comment.POST_ID_KEY, post.id).whereEqualTo(Comment.UID_KEY, uid).get().addOnSuccessListener {
                if(it.size() > 0){
                    makeNotification(context!!.getString(R.string.post_deleted), post.title, null)
                }
            }
        }
    }

    private fun notifyEdited(post: Post) {
        if(settings.editToSubbedPost){
            commentsReference.whereEqualTo(Comment.POST_ID_KEY, post.id).whereEqualTo(Comment.UID_KEY, uid).get().addOnSuccessListener {
                if(it.size() > 0){
                    makeNotification(context!!.getString(R.string.post_edited), post.title, null)
                }
            }
        }
    }

    var count = 0
    private fun sendCommentChangesNotifications(querySnapshot: QuerySnapshot) {
        count++
//        Log.d(Constants.TAG, "In sendCommentNotif, $count")
        if (isListeningComments)
            for (documentChange in querySnapshot.documentChanges) {
                val comment = Comment.fromSnapshot(documentChange.document)
//                Log.d(Constants.TAG, "in notif service: ${comment.toString()}")
                postReference.document(comment.postID).get().addOnSuccessListener {
                    if (comment.uid != uid) {
                        when (documentChange.type) {
                            DocumentChange.Type.ADDED -> {
                                // check if they want to be notified about this
                                val post = Post.fromSnapshot(it)
                                notifyNewComment(post)
                            }
                            DocumentChange.Type.REMOVED -> {
                                // check if they want to be notified about this
                                //makeNotification("Comment deleted!", "On post: ${post.title}", null)
                            }
                            DocumentChange.Type.MODIFIED -> {
                                // check if they want to be notified about this
                                //makeNotification("Comment edited!", "On post: ${post.title}", post)
                            }
                        }
                    }
                }

            }
        else
            startListeningToComments()
    }

    private fun notifyNewComment(post: Post) {
        if(settings.newReplyToYourPost){
            if(post.uid == uid){
                makeNotification(context!!.getString(R.string.new_comment), "On your post: ${post.title}", post)
            }
        }
        if(settings.newReplyToSubbedPost){
            commentsReference.whereEqualTo(Comment.POST_ID_KEY, post.id).whereEqualTo(Comment.UID_KEY, uid).get().addOnSuccessListener {
                if (it.size() > 0) {
                    makeNotification(context!!.getString(R.string.new_comment), "On subbed post: ${post.title}", post)
                }
            }
        }
    }

    fun startListeningToPosts() {
        isListeningPosts = true
    }

    fun startListeningToComments() {
        isListeningComments = true
    }


}