package de.robnice.philipstvcontrol.data.trust

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
private val Context.dataStore by preferencesDataStore(name = "tv_trust")

const val DEV_TRUST_ALL = "DEV_TRUST_ALL"
class TvTrustStore(private val context: Context) {

    private fun keyFor(host: String) = stringPreferencesKey("pin_$host")

    fun pinFlow(host: String): Flow<String?> =
        context.dataStore.data.map { prefs -> prefs[keyFor(host)] }

    suspend fun getPin(host: String): String? =
        context.dataStore.data
            .map { it[keyFor(host)] }
            .first()

    suspend fun setPin(host: String, sha256Pin: String) {
        context.dataStore.edit { prefs ->
            prefs[keyFor(host)] = sha256Pin
        }
    }

    suspend fun clearPin(host: String) {
        context.dataStore.edit { prefs ->
            prefs.remove(keyFor(host))
        }
    }
}
