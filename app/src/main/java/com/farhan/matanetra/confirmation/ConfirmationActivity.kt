package com.farhan.matanetra.confirmation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.activity.viewModels
import com.farhan.matanetra.R
import com.farhan.matanetra.api.Config
import com.farhan.matanetra.databinding.ActivityConfirmationBinding
import com.farhan.matanetra.navigation.NavActivity

class ConfirmationActivity : AppCompatActivity() {
    private lateinit var gestureDetector: GestureDetector
    private lateinit var binding: ActivityConfirmationBinding
    private val apiService = Config.apiService

    private val confirmationViewModel : ConfirmationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gestureDetector = GestureDetector(this, DoubleTapGestureListener())

        binding.btnGetLoc.setOnClickListener {
            confirmationViewModel.fetchData(apiService)
        }

        confirmationViewModel.selectedRoute.observe(this) { selectedRoute ->
            // Display the data in your TextView
            val displayText = "${selectedRoute.title}: ${selectedRoute.description}: ${selectedRoute.id} : ${selectedRoute.latitude} :  ${selectedRoute.longitude}"
            binding.tvData.text = displayText

        }
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
        val intent = Intent(this, NavActivity::class.java)
        startActivity(intent)
        finish() // Optional: finish ConfirmationActivity if you don't want to keep it in the back stack
    }
}