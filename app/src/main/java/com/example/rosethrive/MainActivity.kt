package com.example.rosethrive

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.MenuCompat
import com.google.firebase.auth.FirebaseAuth
import edu.rosehulman.rosefire.Rosefire
import java.util.ArrayList

class MainActivity : AppCompatActivity(), MainListener {

    private var uid: String = "default"
    private val auth = FirebaseAuth.getInstance()
    lateinit var authListener: FirebaseAuth.AuthStateListener

    private val RC_ROSEFIRE_LOGIN = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        initializeListener()
    }

    private fun initializeListener() {
        authListener = FirebaseAuth.AuthStateListener {
            val user = it.currentUser
            Log.d(Constants.TAG, "In auth lis! user = $user")

            if (user != null) {
                uid = user.uid
                switchToMainFragment(uid)
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

    private fun switchToLoginFragment() {
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, LoginFragment())

        ft.commit()
    }

    private fun switchToMainFragment(uid: String) {
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, ListFragment.newInstance(uid))
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
            R.id.notif_settings -> true
            R.id.account_link -> {
                switchToAccountFragment()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPostSelected(post: Post) {
        val viewFragment = ViewPostFragment.newInstance(uid, post)
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, viewFragment)
        ft.addToBackStack("view")
        ft.commit()
    }

    override fun onCreatePostRequest(function: (Post) -> Unit) {
        val createFragment = CreateFragment.newInstance("", "", function)
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, createFragment)
        ft.addToBackStack("create")
        ft.commit()
    }

    fun onRosefireLogin() {
        val signInIntent: Intent = Rosefire.getSignInIntent(this, getString(R.string.REGISTRY_TOKEN))
        startActivityForResult(signInIntent, RC_ROSEFIRE_LOGIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == RC_ROSEFIRE_LOGIN) {
            val result = Rosefire.getSignInResultFromIntent(data)
            if (result.isSuccessful) {
                auth.signInWithCustomToken(result.token)
                Log.d(Constants.TAG, "Username: ${result.username}")
                Log.d(Constants.TAG, "Name: ${result.name}")
                Log.d(Constants.TAG, "Email: ${result.email}")
                Log.d(Constants.TAG, "Group: ${result.group}")
                uid = result.username
                switchToMainFragment(result.username)
            } else {
                Log.d(Constants.TAG, "Rosefire failed")
            }
        }
    }
}