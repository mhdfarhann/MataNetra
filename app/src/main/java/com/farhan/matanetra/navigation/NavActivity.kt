package com.farhan.matanetra.navigation

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.farhan.matanetra.databinding.ActivityNavBinding
import com.farhan.matanetra.finish.FinishActivity
import com.farhan.matanetra.helper.ImageClassifierHelper
import com.farhan.matanetra.main.MainActivity
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.text.NumberFormat
import java.util.concurrent.Executors

class NavActivity : AppCompatActivity() {

    private lateinit var gestureDetector: GestureDetector
    private lateinit var binding: ActivityNavBinding
    private lateinit var imageClassifierHelper: ImageClassifierHelper

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            val message = if (isGranted) {
                "Camera permission granted"
            } else {
                "Camera permission rejected"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        gestureDetector = GestureDetector(this, DoubleTapGestureListener())

        startCamera()
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
        val intent = Intent(this, FinishActivity::class.java)
        startActivity(intent)
        finish() // Optional: finish ConfirmationActivity if you don't want to keep it in the back stack
    }


    private fun startCamera() {
        imageClassifierHelper =
            ImageClassifierHelper(
                context = this,
                imageClassifierListener = object : ImageClassifierHelper.ClassifierListener {
                    override fun onError(error: String) {
                        runOnUiThread {
                            Toast.makeText(this@NavActivity, error, Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
                        runOnUiThread {
                            results?.let { it ->
                                if (it.isNotEmpty() && it[0].categories.isNotEmpty()) {
                                    println(it)
                                    val sortedCategories =
                                        it[0].categories.sortedByDescending { it?.score }
                                    val displayResult =
                                        sortedCategories.joinToString("\n") {
                                            "${it.label} " + NumberFormat.getPercentInstance().format(it.score).trim()
                                        }
                                    binding.tvNav.text = displayResult
                                }
                            }
                        }
                    }
                })
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            val imageAnalyzer =
                ImageAnalysis.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .setTargetRotation(binding.viewFinder.display.rotation)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()
                    .also {
                        it.setAnalyzer(Executors.newSingleThreadExecutor()) { image ->
                            imageClassifierHelper.classify(image)
                        }
                    }
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalyzer
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "Gagal memunculkan kamera.", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }
}