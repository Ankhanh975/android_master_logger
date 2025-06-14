package com.name.myapp

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var contentTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        contentTextView = findViewById(R.id.content_text_view)
        contentTextView.text = "Hello World Tab 1"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_tab1 -> {
                contentTextView.text = "Hello World Tab 1"
                Snackbar.make(findViewById(android.R.id.content), "Switched to Tab 1", Snackbar.LENGTH_SHORT).show()
                true
            }
            R.id.menu_tab2 -> {
                contentTextView.text = "Hello World Tab 2"
                Snackbar.make(findViewById(android.R.id.content), "Switched to Tab 2", Snackbar.LENGTH_SHORT).show()
                true
            }
            R.id.menu_tab3 -> {
                contentTextView.text = "Hello World Tab 3"
                Snackbar.make(findViewById(android.R.id.content), "Switched to Tab 3", Snackbar.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 