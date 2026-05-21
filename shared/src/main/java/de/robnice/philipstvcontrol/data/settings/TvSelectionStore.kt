package de.robnice.philipstvcontrol.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.selectionDataStore by preferencesDataStore(name = "tv_selection")

class TvSelectionStore(private val context: Context) {

    private val selectedIpKey = stringPreferencesKey("selected_tv_ip")
    private val selectedBasePathKey = stringPreferencesKey("selected_tv_base_path")
    private val selectedPairedKey = booleanPreferencesKey("selected_tv_paired")
    private val selectedDigestUserKey = stringPreferencesKey("selected_tv_digest_user")
    private val selectedDigestPassKey = stringPreferencesKey("selected_tv_digest_pass")

    fun selectedIpFlow(): Flow<String?> =
        context.selectionDataStore.data.map { prefs -> prefs[selectedIpKey] }

    fun selectedBasePathFlow(): Flow<String?> =
        context.selectionDataStore.data.map { prefs -> prefs[selectedBasePathKey] }

    fun selectedPairedFlow(): Flow<Boolean> =
        context.selectionDataStore.data.map { prefs -> prefs[selectedPairedKey] ?: false }

    suspend fun getSelectedIp(): String? =
        context.selectionDataStore.data.map { it[selectedIpKey] }.first()

    suspend fun getSelectedBasePath(): String? =
        context.selectionDataStore.data.map { it[selectedBasePathKey] }.first()

    suspend fun getSelectedPaired(): Boolean =
        context.selectionDataStore.data.map { it[selectedPairedKey] ?: false }.first()

    suspend fun getSelectedDigestUser(): String? =
        context.selectionDataStore.data.map { it[selectedDigestUserKey] }.first()

    suspend fun getSelectedDigestPass(): String? =
        context.selectionDataStore.data.map { it[selectedDigestPassKey] }.first()

    suspend fun setSelectedTv(ip: String, basePath: String?, paired: Boolean) {
        context.selectionDataStore.edit { prefs ->
            prefs[selectedIpKey] = ip

            if (basePath != null) {
                prefs[selectedBasePathKey] = basePath
            } else {
                prefs.remove(selectedBasePathKey)
            }

            prefs[selectedPairedKey] = paired
        }
    }

    suspend fun setPairingCredentials(user: String, pass: String) {
        context.selectionDataStore.edit { prefs ->
            prefs[selectedDigestUserKey] = user
            prefs[selectedDigestPassKey] = pass
            prefs[selectedPairedKey] = true
        }
    }

    suspend fun setSelectedPaired(paired: Boolean) {
        context.selectionDataStore.edit { prefs ->
            prefs[selectedPairedKey] = paired
        }
    }

    suspend fun clearSelectedIpOnly() {
        context.selectionDataStore.edit { prefs ->
            prefs.remove(selectedIpKey)
            prefs.remove(selectedBasePathKey)
        }
    }

    suspend fun forgetSelectedTvCompletely() {
        context.selectionDataStore.edit { prefs ->
            prefs.remove(selectedIpKey)
            prefs.remove(selectedBasePathKey)
            prefs.remove(selectedPairedKey)
            prefs.remove(selectedDigestUserKey)
            prefs.remove(selectedDigestPassKey)
        }
    }


}