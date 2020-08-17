package com.example.rosethrive

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.MenuCompat
import com.google.firebase.auth.FirebaseAuth
import edu.rosehulman.rosefire.Rosefire
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), MainListener {

    private var uid: String = "default"
    private val auth = FirebaseAuth.getInstance()
    lateinit var authListener: FirebaseAuth.AuthStateListener
    var currentPhotoPath = ""

    private val RC_ROSEFIRE_LOGIN = 2
    private val RC_TAKE_PICTURE = 3
    private val RC_CHOOSE_PICTURE = 4

    private var imageListener: ImageListener? = null
    private var onPictureTaken: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        NotificationService.setContext(this)

        initializeListener()
    }


    private fun initializeListener() {
        authListener = FirebaseAuth.AuthStateListener {
            val user = it.currentUser

            val intendedPost = intent.extras?.getParcelable<Post>("postToLoad")

            if (user != null) {
                uid = user.uid
                switchToStartupFragment(uid, intendedPost)
            } else {
                switchToLoginFragment()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authListener)
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(authListener)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun switchToLoginFragment() {
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, LoginFragment())

        ft.commit()
    }

    private fun switchToStartupFragment(uid: String, intendedPost: Post?) {
        val ft = supportFragmentManager.beginTransaction()
        if (intendedPost == null) {
            ft.replace(R.id.fragment_container, ListFragment.newInstance(uid))
        }
        else{
            ft.replace(R.id.fragment_container, PostFragment.newInstance(uid, intendedPost))
        }
        NotificationService.setUID(uid)
        NotificationService.initialize()
        ft.commit()
    }

    private fun switchToAccountFragment() {
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, AccountFragment.newInstance(uid))
        ft.addToBackStack("account")
        ft.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        MenuCompat.setGroupDividerEnabled(menu, true);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.notif_settings -> {
                true
            }
            R.id.account_link -> {
                switchToAccountFragment()
                true
            }
            R.id.sign_out -> {
                auth.signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPostSelected(post: Post) {
        val viewFragment = PostFragment.newInstance(uid, post)
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, viewFragment)
        ft.addToBackStack("view")
        ft.commit()
    }

    fun onRosefireLogin() {
        val signInIntent: Intent =
            Rosefire.getSignInIntent(this, getString(R.string.REGISTRY_TOKEN))
        startActivityForResult(signInIntent, RC_ROSEFIRE_LOGIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RC_TAKE_PICTURE -> {
                    sendCameraPhotoToAdapter()
                }
                RC_CHOOSE_PICTURE -> {
                    sendGalleryPhotoToAdapter(data)
                }
                RC_ROSEFIRE_LOGIN -> {
                    val result = Rosefire.getSignInResultFromIntent(data)
                    if (result.isSuccessful) {
                        auth.signInWithCustomToken(result.token)
                        Log.d(Constants.TAG, "Username: ${result.username}")
                        Log.d(Constants.TAG, "Name: ${result.name}")
                        Log.d(Constants.TAG, "Email: ${result.email}")
                        Log.d(Constants.TAG, "Group: ${result.group}")
                        uid = result.username

                        switchToStartupFragment(result.username, null)
                    } else {
                        Log.d(Constants.TAG, "Rosefire failed")
                    }
                }
            }
        }
    }

    override fun showPictureDialog(
        listener: ImageListener,
        onPictureTaken: (String) -> Unit
    ) {
        this.onPictureTaken = onPictureTaken
        imageListener = listener
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose a photo source")
        builder.setMessage("Would you like to take a new picture?\nOr choose an existing one?")
        builder.setPositiveButton("Take Picture") { _, _ ->
            launchCameraIntent()
        }

        builder.setNegativeButton("Choose Picture") { _, _ ->
            launchChooseIntent()
        }
        builder.create().show()
    }

    // Everything camera- and storage-related is from
    // https://developer.android.com/training/camera/photobasics
    private fun launchCameraIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    // authority declared in manifest
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.rosethrive",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, RC_TAKE_PICTURE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun launchChooseIntent() {
        // https://developer.android.com/guide/topics/providers/document-provider
        val choosePictureIntent = Intent(
            Intent.ACTION_OPEN_DOCUMENT,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        choosePictureIntent.addCategory(Intent.CATEGORY_OPENABLE)
        choosePictureIntent.type = "image/*"
        if (choosePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(choosePictureIntent, RC_CHOOSE_PICTURE)
        }
    }

    private fun sendCameraPhotoToAdapter() {
        addPhotoToGallery()
        Log.d(Constants.TAG, "Sending to adapter this photo: $currentPhotoPath")
        imageListener?.handleImage(currentPhotoPath)
        onPictureTaken?.let { it(currentPhotoPath) }
    }

    private fun sendGalleryPhotoToAdapter(data: Intent?) {
        if (data != null && data.data != null) {
            var location = data.data!!.toString()
            if (location != null) {
                imageListener?.handleImage(location)
                onPictureTaken?.let { it(location) }
            }
        }
    }

    // Works Not working on phone
    private fun addPhotoToGallery() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
        }
    }

    interface ImageListener {
        fun handleImage(location: String)
    }

}

