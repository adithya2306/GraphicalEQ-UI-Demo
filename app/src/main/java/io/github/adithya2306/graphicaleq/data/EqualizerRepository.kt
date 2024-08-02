package io.github.adithya2306.graphicaleq.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import io.github.adithya2306.graphicaleq.R
import io.github.adithya2306.graphicaleq.util.dlog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

class EqualizerRepository(
    private val context: Context
) {

    // Preset is saved as a string of comma separated gains in SharedPreferences
    // and is unique to each profile ID
    private var profile = 0 // must be fetched from backend
    private val profileSharedPrefs by lazy {
        context.getSharedPreferences(
            "profile_$profile",
            Context.MODE_PRIVATE
        )
    }

    private val presetsSharedPrefs by lazy {
        context.getSharedPreferences(
            "presets",
            Context.MODE_PRIVATE
        )
    }

    val defaultPreset = Preset(
        name = "Flat",
        bandGains = List<BandGain>(10) { index ->
            BandGain(band = tenBandFreqs[index])
        }
    )

    val builtInPresets: List<Preset> by lazy {
        val names = context.resources.getStringArray(
            R.array.preset_entries
        )
        val presets = context.resources.getStringArray(
            R.array.preset_values
        )
        List(names.size + 1) { index ->
            if (index == 0) {
                defaultPreset
            } else {
                Preset(
                    name = names[index - 1],
                    bandGains = deserializeGains(presets[index - 1]),
                )
            }
        }
    }

    // User defined presets are stored in a SharedPreferences as
    // key - preset name
    // value - comma separated string of gains
    val userPresets: Flow<List<Preset>> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            dlog(TAG, "presetsSharedPrefs changed")
            trySend(
                presetsSharedPrefs.all.map { (key, value) ->
                    Preset(
                        name = key,
                        bandGains = deserializeGains(value.toString()),
                        isUserDefined = true
                    )
                }
            )
        }

        presetsSharedPrefs.registerOnSharedPreferenceChangeListener(listener)
        dlog(TAG, "presetsSharedPrefs registered listener")
        // trigger an initial emission
        listener.onSharedPreferenceChanged(presetsSharedPrefs, null)

        awaitClose {
            presetsSharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
            dlog(TAG, "presetsSharedPrefs unregistered listener")
        }
    }

    suspend fun getBandGains(): List<BandGain> = withContext(Dispatchers.IO) {
        val gains = profileSharedPrefs.getString(PREF_KEY, "")
        return@withContext if (gains.isNullOrEmpty()) {
            defaultPreset.bandGains
        } else {
            deserializeGains(gains)
        }.also {
            dlog(TAG, "getBandGains: $it")
        }
    }

    suspend fun setBandGains(bandGains: List<BandGain>) = withContext(Dispatchers.IO) {
        // INSERT AUDIO EFFECT BACKEND LOGIC HERE
        dlog(TAG, "setBandGains($bandGains)")
        profileSharedPrefs.edit()
            .putString(
                PREF_KEY,
                serializeGains(bandGains)
            )
            .apply()
    }

    suspend fun addPreset(preset: Preset) = withContext(Dispatchers.IO) {
        dlog(TAG, "addPreset($preset)")
        presetsSharedPrefs.edit()
            .putString(preset.name, serializeGains(preset.bandGains))
            .apply()
    }

    suspend fun removePreset(preset: Preset) = withContext(Dispatchers.IO) {
        dlog(TAG, "removePreset($preset)")
        presetsSharedPrefs.edit()
            .remove(preset.name)
            .apply()
    }

    private companion object {
        const val TAG = "EqRepository"
        const val PREF_KEY = "geq_preset"

        val tenBandFreqs = intArrayOf(
            32,
            64,
            125,
            250,
            500,
            1000,
            2000,
            4000,
            8000,
            16000
        )

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