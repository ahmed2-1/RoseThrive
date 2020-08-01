package com.example.rosethrive

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.MenuCompat

class MainActivity : AppCompatActivity(), MainListener {

    private val uid: String = "default"
    var postsImp = ArrayList<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        switchToMainFragment("")
    }

    private fun switchToLoginFragment() {
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, LoginFragment())
        ft.commit()
    }

    private fun switchToMainFragment(uid: String) {
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, ListFragment.newInstance(uid, "1"))
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
}