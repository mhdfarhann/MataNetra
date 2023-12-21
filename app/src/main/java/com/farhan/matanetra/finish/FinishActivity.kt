package com.farhan.matanetra.finish

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.lifecycle.ViewModelProvider
import com.farhan.matanetra.confirmation.ConfirmationActivity
import com.farhan.matanetra.databinding.ActivityFinishBinding
import com.farhan.matanetra.main.MainActivity
import com.farhan.matanetra.main.MainViewModel
import com.farhan.matanetra.tools.WavConvert

class FinishActivity : AppCompatActivity() {

    private lateinit var gestureDetector: GestureDetector

    private lateinit var binding : ActivityFinishBinding

    private lateinit var mainViewModel: MainViewModel

    private lateinit var confirmationActivity: ConfirmationActivity

    private lateinit var wavConvert: WavConvert


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFinishBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gestureDetector = GestureDetector(this, DoubleTapGestureListener())

        wavConvert = WavConvert(this, getExternalFilesDir(null)?.absolutePath ?: "recordings")

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        val title = intent.getStringExtra("title")
        if (title != null) {
            binding.tvTujuan.text = title
        } else {
            binding.tvTujuan.text = ""
        }

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    private inner class DoubleTapGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            // Implement the code to start NavActivity here
            startMainActivity()
            return true
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Optional: finish ConfirmationActivity if you don't want to keep it in the back stack
    }
}