package com.example.projektrejestratorjazdy

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

// --- Manager Lokalizacji (GPS) ---
class LocationClient(private val context: Context) {
    private val client: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission") // Uprawnienia sprawdzamy w UI
    fun getLocationUpdates(): Flow<Location> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.lastOrNull()?.let { trySend(it) }
            }
        }

        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        awaitClose { client.removeLocationUpdates(callback) }
    }
}

// --- Manager Akcelerometru ---
class AccelerometerManager(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    fun getGForce(): Flow<Float> = callbackFlow {
        if (accelerometer == null) {
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    // Obliczamy wypadkową siłę G (pierwiastek z sumy kwadratów)
                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]
                    val gForce = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat() / 9.81f
                    trySend(gForce)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}