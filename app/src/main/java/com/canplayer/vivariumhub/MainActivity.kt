package com.canplayer.vivariumhub

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btn_search).setOnClickListener {
         Toast.makeText(this,"77777",Toast.LENGTH_LONG).show()
        }
    }
}
