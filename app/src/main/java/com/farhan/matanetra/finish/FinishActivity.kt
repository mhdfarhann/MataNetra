package com.farhan.matanetra.finish

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.lifecycle.ViewModelProvider
import com.farhan.matanetra.databinding.ActivityFinishBinding
import com.farhan.matanetra.main.MainActivity
import com.farhan.matanetra.main.MainViewModel
import com.farhan.matanetra.tools.WavConvert
import java.util.Locale

class FinishActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var gestureDetector: GestureDetector

    private lateinit var binding : ActivityFinishBinding

    private lateinit var wavConvert: WavConvert

    private lateinit var textToSpeech: TextToSpeech
    private var isTtsInitialized = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFinishBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gestureDetector = GestureDetector(this, DoubleTapGestureListener())
        textToSpeech = TextToSpeech(this, this)


        wavConvert = WavConvert(this, getExternalFilesDir(null)?.absolutePath ?: "recordings")

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

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            val result: Int = textToSpeech.setLanguage(Locale("id", "ID"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TextToSpeech", "Language data is missing or not supported")
            } else {
                Log.d("TextToSpeech", "TextToSpeech initialized successfully")
                speakText("Anda sudah sampai di tujuan, ${binding.tvTujuan.text}. Ketuk layar 2 kali untuk mencari tujuan lain")
            }
        } else {
            Log.e("TextToSpeech", "TextToSpeech initialization failed")
        }
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