package com.kratos.fflauncher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kratos.fflauncher.data.AppPreferences
import com.kratos.fflauncher.model.GameStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = AppPreferences(application)

    private val _uiState = MutableStateFlow(
        MainUiState(
            autoLaunch = preferences.autoLaunch,
            showLines = preferences.showLines,
            showBoxes = preferences.showBoxes,
            showObstacles = preferences.showObstacles,
            overlayEnabled = false,
            message = "Pronto."
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun updateAutoLaunch(enabled: Boolean) {
        preferences.autoLaunch = enabled
        _uiState.value = _uiState.value.copy(autoLaunch = enabled)
    }

    fun updateLines(enabled: Boolean) {
        preferences.showLines = enabled
        _uiState.value = _uiState.value.copy(showLines = enabled)
    }

    fun updateBoxes(enabled: Boolean) {
        preferences.showBoxes = enabled
        _uiState.value = _uiState.value.copy(showBoxes = enabled)
    }

    fun updateObstacles(enabled: Boolean) {
        preferences.showObstacles = enabled
        _uiState.value = _uiState.value.copy(showObstacles = enabled)
    }

    fun setOverlayEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(overlayEnabled = enabled)
    }

    fun setMessage(message: String) {
        _uiState.value = _uiState.value.copy(message = message)
    }

    fun refreshGameStatus() {
        viewModelScope.launch {
            val status = GameStatusReader.readGameStatus(getApplication())
            _uiState.value = _uiState.value.copy(
                gameStatus = status,
                message = status.message
            )
        }
    }

data class MainUiState(
    val autoLaunch: Boolean = false,
    val showLines: Boolean = true,
    val showBoxes: Boolean = true,
    val showObstacles: Boolean = true,
    val overlayEnabled: Boolean = false,
    val message: String = "Pronto.",
    val gameStatus: GameStatus = GameStatus(false, message = "Free Fire não encontrado.")
)
}
