package com.focussense.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focussense.app.sensors.SensorRepository
import com.focussense.app.sensors.model.LightCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SensorsUiState(
    val accelerationX: Float = 0f,
    val accelerationY: Float = 0f,
    val accelerationZ: Float = 0f,
    val netMagnitude: Float = 0f,
    val isSignificantMovement: Boolean = false,
    val accelerometerTimestamp: Long = 0L,
    val isAccelerometerAvailable: Boolean = true,
    val lightLux: Float = 0f,
    val lightCategory: LightCategory = LightCategory.MODERATE,
    val lightTimestamp: Long = 0L,
    val isLightSensorAvailable: Boolean = true,
    val movementThreshold: Float = 1.8f
)

class SensorsViewModel(
    private val sensorRepository: SensorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SensorsUiState(
            isAccelerometerAvailable = sensorRepository.isAccelerometerAvailable,
            isLightSensorAvailable = sensorRepository.isLightSensorAvailable
        )
    )
    val uiState: StateFlow<SensorsUiState> = _uiState.asStateFlow()

    init {
        sensorRepository.startListening()
        observeSensors()
    }

    private fun observeSensors() {
        viewModelScope.launch {
            sensorRepository.accelerometerFlow.collect { data ->
                _uiState.update { current ->
                    current.copy(
                        accelerationX = data.x,
                        accelerationY = data.y,
                        accelerationZ = data.z,
                        netMagnitude = data.magnitude,
                        isSignificantMovement = data.isSignificantMovement,
                        accelerometerTimestamp = data.timestamp
                    )
                }
            }
        }

        viewModelScope.launch {
            sensorRepository.lightFlow.collect { data ->
                _uiState.update { current ->
                    current.copy(
                        lightLux = data.lux,
                        lightCategory = data.category,
                        lightTimestamp = data.timestamp
                    )
                }
            }
        }
    }

    fun setMovementThreshold(threshold: Float) {
        sensorRepository.setMovementThreshold(threshold)
        _uiState.update { it.copy(movementThreshold = threshold) }
    }

    override fun onCleared() {
        super.onCleared()
        sensorRepository.stopListening()
    }
}
