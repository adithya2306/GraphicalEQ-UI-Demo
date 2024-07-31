package io.github.adithya2306.graphicaleq.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.adithya2306.graphicaleq.R
import io.github.adithya2306.graphicaleq.util.PREF_KEY
import io.github.adithya2306.graphicaleq.util.dlog
import io.github.adithya2306.graphicaleq.util.tenBandFreqs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "presets")

class EqualizerRepository(
    private val context: Context
) {

    // Preset is saved as a string of comma separated gains in SharedPreferences
    // and is unique to each profile ID
    private var profile = 0 // must be fetched from backend
    private val sharedPreferences by lazy {
        context.getSharedPreferences(
            "profile_$profile",
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

    // User defined presets are stored in a PreferenceDataStore as
    // key - preset name
    // value - comma separated string of gains
    val userPresets: Flow<List<Preset>> = context.dataStore.data
        .map { prefs ->
            prefs.asMap().map { (key, value) ->
                Preset(
                    name = key.name,
                    bandGains = deserializeGains(value.toString()),
                    isUserDefined = true
                )
            }
        }

    suspend fun getBandGains(): List<BandGain> = withContext(Dispatchers.IO) {
        val gains = sharedPreferences.getString(PREF_KEY, "")
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
        sharedPreferences.edit()
            .putString(
                PREF_KEY,
                serializeGains(bandGains)
            )
            .apply()
    }

    suspend fun addPreset(preset: Preset) = withContext(Dispatchers.IO) {
        dlog(TAG, "addPreset($preset)")
        context.dataStore.edit { prefs ->
            prefs[stringPreferencesKey(preset.name)] = serializeGains(preset.bandGains)
        }
    }

    suspend fun removePreset(preset: Preset) = withContext(Dispatchers.IO) {
        dlog(TAG, "removePreset($preset)")
        context.dataStore.edit { prefs ->
            prefs.remove(stringPreferencesKey(preset.name))
        }
    }

    private companion object {
        const val TAG = "EqRepository"

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