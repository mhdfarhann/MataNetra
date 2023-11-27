package com.farhan.matanetra.main

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import com.farhan.matanetra.confirmation.ConfirmationActivity
import com.farhan.matanetra.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var handlerAnimation = Handler(Looper.getMainLooper())
    private var statusAnimation = false
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fabRecord.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                statusAnimation = true
                startPulse()
            } else if (motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) {
                statusAnimation = false
                stopPulse()
                startActivity(Intent(this, ConfirmationActivity::class.java))
            }
            view.performClick()
            true
        }

        // Set up a custom touch listener for the entire screen
        window.decorView.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                statusAnimation = true
                startPulse()
            } else if (motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) {
                statusAnimation = false
                stopPulse()
                startActivity(Intent(this, ConfirmationActivity::class.java))
            }
            true // Consume the touch event so it doesn't propagate to other views
        }
    }

    private fun startPulse() {
        CoroutineScope(Dispatchers.Main).launch {
            while (statusAnimation) {
                binding.imgAnimation1.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(1000).start()
                delay(500)
                binding.imgAnimation1.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(1000).start()
                delay(500)
                binding.imgAnimation2.animate().scaleX(4f).scaleY(4f).alpha(1f).setDuration(700).start()
                delay(300)
                binding.imgAnimation2.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(700).start()
                delay(300)
            }
        }
    }

    private fun stopPulse() {
        handlerAnimation.removeCallbacksAndMessages(null)
    }

}
