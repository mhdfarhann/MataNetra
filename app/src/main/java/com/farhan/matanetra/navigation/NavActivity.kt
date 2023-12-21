package com.farhan.matanetra.navigation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.farhan.matanetra.databinding.ActivityNavBinding
import com.farhan.matanetra.finish.FinishActivity
import com.farhan.matanetra.main.MainViewModel
import com.farhan.matanetra.response.Destination
import com.farhan.matanetra.tools.WavConvert
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class NavActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityNavBinding
    private lateinit var gestureDetector: GestureDetector
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var sensorManager: SensorManager
    private var compassSensor: Sensor? = null

    private lateinit var vibrator: Vibrator


    private lateinit var navViewModel: NavViewModel
    private lateinit var destinationPoints: List<Destination>

    private lateinit var mainViewModel: MainViewModel

    private val VIBRATION_DISTANCE_THRESHOLD = 5.0
    private val VIBRATION_AZIMUTH_THRESHOLD = 180.0
    private val VIBRATION_AZIMUTH_RANGE = 5.0

    private val REQUEST_CODE = 123

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Check if the device has a compass sensor
        compassSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        navViewModel = ViewModelProvider(this)[NavViewModel::class.java]

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        navViewModel.getShortestPath("G1", "C103") { shortestPath ->
            handleShortestPath(shortestPath)
        }

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]


        if (checkPermissions()) {
            startLocationUpdates()
            registerCompassListener()
        } else {
            requestLocationPermissions()
        }

        val title = intent.getStringExtra("title")
        if (title != null) {
            binding.tvTujuanNav.text = title
        } else {
            binding.tvTujuanNav.text = ""
        }

        startCamera()
    }

    private fun handleShortestPath(shortestPath: List<Destination>) {
        // Handle the obtained shortest path
        // You might want to update UI or initiate navigation based on the path
        destinationPoints = shortestPath

        // Start location updates
        startLocationUpdates()
    }

    private fun checkPermissions(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_CODE
        )
    }

    private fun startLocationUpdates() {
        locationCallback = object : LocationCallback() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    // Do something with latitude and longitude values
                    updateUI(latitude, longitude)
                    checkVibrationTrigger(location.latitude, location.longitude)
                }
            }
        }

        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(5000) // Update every 5 seconds
            .setFastestInterval(1000) // The fastest interval for location updates (every 1 second)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkVibrationTrigger(currentLatitude: Double, currentLongitude: Double) {
        for (destination in destinationPoints) {
            val distance = calculateDistance(
                currentLatitude,
                currentLongitude,
                destination.lat,
                destination.long
            )
            if (distance < VIBRATION_DISTANCE_THRESHOLD) {
                // Trigger vibration
                vibrate()
            }
        }
    }

    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val R = 6371 // Radius of the earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = (
                sin(dLat / 2) * sin(dLat / 2) +
                        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                        sin(dLon / 2) * sin(dLon / 2)
                )
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c * 1000 // Convert to meters
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun vibrate() {
        // Vibrate for 500 milliseconds (adjust as needed)
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun registerCompassListener() {
        compassSensor?.let {
            sensorManager.registerListener(
                this,
                compassSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun unregisterCompassListener() {
        sensorManager.unregisterListener(this)
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(latitude: Double, longitude: Double) {
        // Update your UI components with the new latitude and longitude values
        // For example, you can display them on TextViews
        binding.tvLat.text = "Latitude: $latitude"
        binding.tvLon.text = "Longitude: $longitude"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {
            if (checkPermissions()) {
                startLocationUpdates()
                registerCompassListener()
            } else {
                // Handle the case when the user denies permission
                // You may display a message or disable location-related functionality
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        unregisterCompassListener()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle changes in sensor accuracy (if needed)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            val azimuth = getAzimuth(event.values[0], event.values[1])
            rotateCompass(azimuth)

            // Check if azimuth is close to 180 degrees
            if (isAzimuth180(azimuth)) {
                vibrate()
            }
        }
    }

    private fun isAzimuth180(azimuth: Float): Boolean {
        return azimuth >= VIBRATION_AZIMUTH_THRESHOLD - VIBRATION_AZIMUTH_RANGE &&
                azimuth <= VIBRATION_AZIMUTH_THRESHOLD + VIBRATION_AZIMUTH_RANGE
    }

    private fun getAzimuth(x: Float, y: Float): Float {
        val radians = atan2(y.toDouble(), x.toDouble())
        var azimuth = Math.toDegrees(radians).toFloat()
        if (azimuth < 0) {
            azimuth += 360f
        }
        return azimuth
    }

    @SuppressLint("SetTextI18n")
    private fun rotateCompass(azimuth: Float) {
        // Update your compass UI here (e.g., set azimuth to a TextView)
        binding.tvAzimuth.text = "Azimuth: $azimuth°"
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    private inner class DoubleTapGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            // Implement the code to start NavActivity here
            startFinishActivity()
            return true
        }
    }

    private fun startFinishActivity() {
        val intent = Intent(this, FinishActivity::class.java)
        startActivity(intent)
        finish() // Optional: finish ConfirmationActivity if you don't want to keep it in the back stack
    }

    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "Failed to open camera.", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }
}
