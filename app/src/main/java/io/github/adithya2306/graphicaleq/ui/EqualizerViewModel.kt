package io.github.adithya2306.graphicaleq.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.adithya2306.graphicaleq.data.EqualizerRepository
import io.github.adithya2306.graphicaleq.data.Preset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class EqualizerViewModel(
    private val repository: EqualizerRepository
) : ViewModel() {

    val presets = repository.presets

    // Backing property to avoid state updates from other classes
    private val _preset = MutableStateFlow(repository.getPreset())
    val preset = _preset.asStateFlow()

    init {
        _preset
            .drop(1) // skip the initial value
            .onEach { repository.setPreset(it) }
            .launchIn(viewModelScope)
    }

    fun reset() {
        _preset.value = repository.defaultPreset
    }

    fun setPreset(preset: Preset) {
        this._preset.value = preset
    }

    fun setGain(index: Int, gain: Float) {
        _preset.value = Preset(
            name = "Custom",
            bandGains = _preset.value.bandGains
                .toMutableList()
                // create a new object to ensure the flow emits an update.
                .apply { this[index] = this[index].copy(gain = gain) }
                .toList()
        )
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                EqualizerViewModel(
                    repository = EqualizerRepository(
                        this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                    )
                )
            }
        }
    }
}