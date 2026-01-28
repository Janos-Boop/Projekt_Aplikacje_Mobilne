package com.example.projektrejestratorjazdy

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RecorderUiState(
    val currentSpeed: Double = 0.0,
    val gForce: Float = 1.0f,
    val isRecording: Boolean = false,
    val maxSpeedSession: Double = 0.0,
    val maxGForceSession: Float = 0.0f,
    val history: List<DriveSession> = emptyList()
)

class RecorderViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DriveDatabase.getDatabase(application)
    private val locationClient = LocationClient(application)
    private val accelManager = AccelerometerManager(application)

    private val _uiState = MutableStateFlow(RecorderUiState())
    val uiState: StateFlow<RecorderUiState> = _uiState.asStateFlow()

    private val currentPoints = mutableListOf<DrivePoint>()

    // Zmienna do przechowywania MAX przeciążenia w ostatniej sekundzie (między punktami GPS)
    private var intervalMaxGForce: Float = 0.0f

    init {
        viewModelScope.launch {
            db.driveDao().getAllSessions().collect { sessions ->
                _uiState.update { it.copy(history = sessions) }
            }
        }
    }

    fun startSensors() {
        // 1. GPS - Główny wyzwalacz zapisu punktów (co ok. 1s)
        viewModelScope.launch {
            locationClient.getLocationUpdates().collect { loc ->
                val speedKmh = (loc.speed * 3.6)

                // Tutaj zapisujemy punkt. Używamy 'intervalMaxGForce' zamiast chwilowego 'g'
                updateRecordingData(speed = speedKmh, savePoint = true)
            }
        }

        // 2. Akcelerometr - działa szybko, aktualizuje "chwilowy max"
        viewModelScope.launch {
            accelManager.getGForce().collect { g ->
                // Aktualizuj max z interwału
                if (g > intervalMaxGForce) {
                    intervalMaxGForce = g
                }

                // Aktualizuj UI na żywo
                updateRecordingData(gForce = g, savePoint = false)
            }
        }
    }

    private fun updateRecordingData(speed: Double? = null, gForce: Float? = null, savePoint: Boolean) {
        _uiState.update { state ->
            val newSpeed = speed ?: state.currentSpeed
            val newGForce = gForce ?: state.gForce

            if (state.isRecording && savePoint) {
                synchronized(currentPoints) {
                    currentPoints.add(
                        DrivePoint(
                            sessionId = 0,
                            timestamp = System.currentTimeMillis(),
                            speed = newSpeed,
                            // ZAPISUJEMY MAX Z OSTATNIEGO INTERWAŁU, A NIE CHWILOWE!
                            gForce = intervalMaxGForce
                        )
                    )
                }
                // Resetujemy max interwału po zapisaniu punktu
                intervalMaxGForce = 0.0f
            }

            state.copy(
                currentSpeed = newSpeed,
                gForce = newGForce,
                maxSpeedSession = if (state.isRecording) maxOf(state.maxSpeedSession, newSpeed) else state.maxSpeedSession,
                maxGForceSession = if (state.isRecording) maxOf(state.maxGForceSession, newGForce) else state.maxGForceSession
            )
        }
    }

    fun toggleRecording() {
        val currentState = _uiState.value
        if (currentState.isRecording) {
            // STOP
            viewModelScope.launch {
                val session = DriveSession(
                    maxSpeed = currentState.maxSpeedSession,
                    maxGForce = currentState.maxGForceSession,
                    distance = 0.0
                )
                val newSessionId = db.driveDao().insertSession(session).toInt()

                val pointsToSave = synchronized(currentPoints) {
                    currentPoints.map { it.copy(sessionId = newSessionId) }.toList()
                }
                db.driveDao().insertPoints(pointsToSave)
                currentPoints.clear()
            }
            _uiState.update { it.copy(isRecording = false, maxSpeedSession = 0.0, maxGForceSession = 0.0f) }
        } else {
            // START
            currentPoints.clear()
            intervalMaxGForce = 0.0f
            _uiState.update { it.copy(isRecording = true, maxSpeedSession = 0.0, maxGForceSession = 0.0f) }
        }
    }

    fun deleteSession(session: DriveSession) {
        viewModelScope.launch { db.driveDao().deleteSession(session) }
    }

    suspend fun getSessionPoints(sessionId: Int): List<DrivePoint> {
        return db.driveDao().getPointsForSession(sessionId)
    }
}