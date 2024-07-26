package io.github.adithya2306.graphicaleq.data

import android.content.Context
import android.util.Log
import io.github.adithya2306.graphicaleq.R
import io.github.adithya2306.graphicaleq.utils.PREF_KEY
import io.github.adithya2306.graphicaleq.utils.TAG
import io.github.adithya2306.graphicaleq.utils.dlog
import io.github.adithya2306.graphicaleq.utils.tenBandFreqs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EqualizerRepository(context: Context) {

    private var profile = 0 // must be fetched from backend
    private val sharedPreferences by lazy {
        context.getSharedPreferences(
            "profile_$profile",
            Context.MODE_PRIVATE
        )
    }

    val presets: List<Preset> by lazy {
        val names = context.resources.getStringArray(
            R.array.preset_entries
        )
        val presets = context.resources.getStringArray(
            R.array.preset_values
        )
        List(names.size) { index ->
            Preset(
                name = names[index],
                bandGains = deserializeGains(presets[index])
            )
        }
    }

    val defaultPreset = presets[0]

    fun getPreset(): Preset {
        val gains = sharedPreferences.getString(PREF_KEY, "")
        return if (gains.isNullOrEmpty()) {
            defaultPreset
        } else {
            val bandGains = deserializeGains(gains)
            presets.find {
                it.bandGains == bandGains
            } ?: Preset(
                name = "Custom",
                bandGains = bandGains
            )
        }.also {
            dlog(TAG, "getPreset: $it")
        }
    }

    suspend fun setPreset(preset: Preset) = withContext(Dispatchers.IO) {
        // INSERT AUDIO EFFECT BACKEND LOGIC HERE
        dlog(TAG, "setPreset: $preset")
        sharedPreferences.edit()
            .putString(
                PREF_KEY,
                serializeGains(preset.bandGains)
            )
            .apply()
    }

    private companion object {
        fun deserializeGains(bandGains: String): List<BandGain> {
            val gains: List<Float> =
                bandGains.split(",").runCatching {
                    require(size == 20) {
                        "Preset must have 20 elements, has only $size!"
                    }
                    map { it.toFloat() }
                        .twentyToTenBandGains()
                }.onFailure { exception ->
                    Log.e(TAG, "Failed to parse preset", exception)
                }.getOrDefault(
                    // fallback to flat
                    List<Float>(10) { 0f }
                )
            return List(10) { index ->
                BandGain(
                    band = tenBandFreqs[index],
                    gain = gains[index]
                )
            }
        }

        fun serializeGains(bandGains: List<BandGain>): String {
            return bandGains.map { it.gain }
                .tenToTwentyBandGains()
                .joinToString(",")
        }

        // we show only 10 bands in UI however backend requires 20 bands
        fun List<Float>.tenToTwentyBandGains() =
            List<Float>(20) { index ->
                if (index % 2 == 1 && index < 19) {
                    // every odd element is the average of its surrounding elements
                    (this[(index - 1) / 2] + this[(index + 1) / 2]) / 2
                } else {
                    this[index / 2]
                }
            }

        fun List<Float>.twentyToTenBandGains() =
            // skip every odd element
            filterIndexed { index, _ -> index % 2 == 0 }
    }
}