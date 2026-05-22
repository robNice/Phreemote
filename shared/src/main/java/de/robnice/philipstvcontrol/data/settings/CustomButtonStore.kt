package de.robnice.philipstvcontrol.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.robnice.philipstvcontrol.domain.model.CUSTOM_BUTTON_COUNT
import de.robnice.philipstvcontrol.domain.model.CustomButton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.customButtonDataStore by preferencesDataStore(name = "custom_buttons")

class CustomButtonStore(private val context: Context) {

    private fun cmdKey(i: Int) = stringPreferencesKey("btn_${i}_cmd")
    private fun lblKey(i: Int) = stringPreferencesKey("btn_${i}_lbl")
    private fun enKey(i: Int) = booleanPreferencesKey("btn_${i}_en")

    fun buttonsFlow(): Flow<List<CustomButton>> =
        context.customButtonDataStore.data.map { prefs ->
            (0 until CUSTOM_BUTTON_COUNT).map { i ->
                CustomButton(
                    command = prefs[cmdKey(i)] ?: "",
                    label = prefs[lblKey(i)] ?: "",
                    enabled = prefs[enKey(i)] ?: false
                )
            }
        }

    suspend fun getAll(): List<CustomButton> = buttonsFlow().first()

    suspend fun set(index: Int, button: CustomButton) {
        context.customButtonDataStore.edit { prefs ->
            prefs[cmdKey(index)] = button.command
            prefs[lblKey(index)] = button.label
            prefs[enKey(index)] = button.enabled
        }
    }
}
