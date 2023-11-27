package com.farhan.matanetra.finish

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import com.farhan.matanetra.R
import com.farhan.matanetra.main.MainActivity
import com.farhan.matanetra.navigation.NavActivity

class FinishActivity : AppCompatActivity() {

    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish)

        gestureDetector = GestureDetector(this, DoubleTapGestureListener())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    private inner class DoubleTapGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            // Implement the code to start NavActivity here
            startNavActivity()
            return true
        }
    }

    private fun startNavActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Optional: finish ConfirmationActivity if you don't want to keep it in the back stack
    }
}