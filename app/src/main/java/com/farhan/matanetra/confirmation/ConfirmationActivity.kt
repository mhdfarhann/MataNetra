package com.farhan.matanetra.confirmation

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.farhan.matanetra.databinding.ActivityConfirmationBinding
import com.farhan.matanetra.main.MainActivity
import com.farhan.matanetra.main.MainViewModel
import com.farhan.matanetra.navigation.NavActivity
import java.util.*
import kotlin.math.abs

class ConfirmationActivity : AppCompatActivity(), OnInitListener {

    private lateinit var gestureDetector: GestureDetector
    private lateinit var binding: ActivityConfirmationBinding
    private lateinit var textToSpeech: TextToSpeech
    private var isTtsInitialized = false

    private var x1: Float = 0f
    private var x2: Float = 0f
    private val MIN_DISTANCE = 150

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        viewModel.destinationTitleCallback = { title ->
            // Handle the title here, you can pass it to another activity
            startNavActivity(title)
        }

        gestureDetector = GestureDetector(this, GestureListener())
        textToSpeech = TextToSpeech(this, this)

        val title = intent.getStringExtra("title")
        if (title != null) {
            binding.tvTujuan.text = title
        } else {
            binding.tvTujuan.text = ""
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> x1 = event.x
            MotionEvent.ACTION_UP -> {
                x2 = event.x
                val deltaX: Float = x2 - x1
                if (abs(deltaX) > MIN_DISTANCE) {
                    startMainActivity()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            val result: Int = textToSpeech.setLanguage(Locale("id", "ID"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TextToSpeech", "Language data is missing or not supported")
            } else {
                Log.d("TextToSpeech", "TextToSpeech initialized successfully")
                speakText("Lokasi Tujuan Anda Adalah ${binding.tvTujuan.text}. Ketuk layar 2 kali untuk lanjut atau swipe ke kiri untuk batal")
            }
        } else {
            Log.e("TextToSpeech", "TextToSpeech initialization failed")
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            val title = binding.tvTujuan.text.toString()
            startNavActivity(title)
            return true
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startNavActivity(title : String) {
        val intent = Intent(this, NavActivity::class.java)
        intent.putExtra("title", title)
        startActivity(intent)
        finish()
    }

    private fun speakText(text: String) {
        if (isTtsInitialized) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.e("TextToSpeech", "TextToSpeech not initialized, speakText aborted")
        }
    }

    override fun onDestroy() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}
