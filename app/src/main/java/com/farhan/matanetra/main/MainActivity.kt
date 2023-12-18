package com.farhan.matanetra.main

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.farhan.matanetra.confirmation.ConfirmationActivity
import com.farhan.matanetra.databinding.ActivityMainBinding
import com.farhan.matanetra.tools.WavConvert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

const val REQUEST_CODE = 200

class MainActivity : AppCompatActivity(), OnInitListener {

    private var handlerAnimation = Handler(Looper.getMainLooper())
    private var statusAnimation = false
    private var permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO)
    private var permissionGranted = false

    private lateinit var wavConvert: WavConvert

    private lateinit var binding: ActivityMainBinding
    private lateinit var progressDialog: ProgressDialog
    private var isRecording = false


    private lateinit var textToSpeech: TextToSpeech

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.sleep(3000)
        installSplashScreen()

        wavConvert = WavConvert(this, getExternalFilesDir(null)?.absolutePath ?: "recordings")


        progressDialog = ProgressDialog(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionGranted = ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)

        binding.fabRecord.setOnTouchListener { view, motionEvent ->

            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                statusAnimation = true
                startPulse()
                startRecording()
            } else if (motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) {
                statusAnimation = false
                stopPulse()
                if (isRecording) {
                    stopRecording()
                    isRecording = false
                }
            }
            view.performClick()
            true
        }

        window.decorView.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                statusAnimation = true
                startPulse()
                startRecording()
            } else if (motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) {
                statusAnimation = false
                stopPulse()
                if (isRecording) {
                    stopRecording()
                    isRecording = false
                }
                startActivity(Intent(this, ConfirmationActivity::class.java))
            }
            true
        }

        // Initialize TextToSpeech
        textToSpeech = TextToSpeech(this, this)
    }

    override fun onDestroy() {
        // Shutdown TextToSpeech when the activity is destroyed
        textToSpeech.shutdown()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE)
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    private fun startRecording() {
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            return
        }

        try {
            wavConvert.startRecording()
            isRecording = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        try {
            wavConvert.stopRecording { isSuccess, title ->
                if (isSuccess) {
                    // Handle successful audio upload here
                    // You can use the 'title' variable as needed
                    navigateToConfirmationActivity(title)
                } else {
                    // Handle failure of audio upload here
                    showToast("Failed to upload audio. Please try again.")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun navigateToConfirmationActivity(title: String) {
        val intent = Intent(this@MainActivity, ConfirmationActivity::class.java)
        intent.putExtra("title", title)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }


    private fun speakWelcomeMessage() {
        val welcomeMessage =
            "Selamat datang di MataNetra. Ketuk sekali dan tahan layar kemudian sebutkan tujuan anda."
        textToSpeech.speak(welcomeMessage, TextToSpeech.QUEUE_FLUSH, null, null)
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

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val locale = Locale("id", "ID") // Language code for Bahasa Indonesia
            val languageResult = textToSpeech.setLanguage(locale)

            if (languageResult == TextToSpeech.LANG_MISSING_DATA || languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Handle language not supported or missing data
            } else {
                // Delay for 1 second before speaking the welcome message
                Handler(Looper.getMainLooper()).postDelayed({
                    speakWelcomeMessage()
                }, 1000)
            }
        } else {
            // Handle initialization failure
        }
    }
}
