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
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlin.random.Random

object NotificationService {


    var isListening: Boolean = false
    private var context: Context? = null
    private val postReference = FirebaseFirestore
        .getInstance()
        .collection(Constants.POSTS_COLLECTION)

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

    fun makeNotification(textTitle: String, textContent: String) {

        if (context != null) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

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

    fun setContext(newContext: Context) {
        context = newContext
    }

    fun initialize() {
        createNotificationChannel()

        postReference.addSnapshotListener { querySnapshot, e ->
            if (e != null) {
                Log.w(Constants.TAG, "listen error", e)
            } else {
                sendChangesNotifications(querySnapshot!!)
            }
        }
    }

    private fun sendChangesNotifications(querySnapshot: QuerySnapshot) {
        if (isListening)
            for (documentChange in querySnapshot.documentChanges) {
                val post = Post.fromSnapshot(documentChange.document)
                when (documentChange.type) {
                    DocumentChange.Type.ADDED -> {
                        // check if they want to be notified about this
                        makeNotification("New post!", post.title)
                    }
                    DocumentChange.Type.REMOVED -> {
                        // check if they want to be notified about this
                        makeNotification("Post deleted!", post.title)
                    }
                    DocumentChange.Type.MODIFIED -> {
                        // check if they want to be notified about this
                        Log.d(Constants.TAG, "NOTIFICATION: " + post.toString())
                        makeNotification("Post edited!", post.title)
                    }
                }
            }
        else
            startListening()
    }

    fun startListening() {
        isListening = true
    }


}