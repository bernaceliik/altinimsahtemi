package com.bernacelik.altinimsahtemi.collector.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "collector_prefs")

/** Kaydı yapan kişi ve cihaz kimliği — her dosyanın metadata'sına yazılır. */
data class Operator(
    val id: String,
    val name: String,
    val deviceNote: String,
) {
    val isConfigured: Boolean get() = name.isNotBlank()
}

/**
 * Operatör bilgisi ve kalıcı cihaz kimliği.
 *
 * Cihaz kimliği ilk açılışta üretilip saklanır: aynı fiziksel telefonun tüm
 * kayıtları eşleşebilsin diye. Mikrofon kalitesi cihazdan cihaza değiştiği için
 * bu kimlik, model eğitiminde cihaz bazlı sapmayı analiz etmenin tek yoludur.
 */
class OperatorStore(private val context: Context) {

    val operator: Flow<Operator> = context.dataStore.data.map { prefs ->
        Operator(
            id = prefs[KEY_DEVICE_ID] ?: "",
            name = prefs[KEY_NAME].orEmpty(),
            deviceNote = prefs[KEY_DEVICE_NOTE].orEmpty(),
        )
    }

    /** Cihaz kimliğini döndürür, yoksa üretip kalıcılaştırır. */
    suspend fun ensureDeviceId(): String {
        var id = ""
        context.dataStore.edit { prefs ->
            id = prefs[KEY_DEVICE_ID] ?: UUID.randomUUID().toString().also { prefs[KEY_DEVICE_ID] = it }
        }
        return id
    }

    suspend fun save(name: String, deviceNote: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NAME] = name.trim()
            prefs[KEY_DEVICE_NOTE] = deviceNote.trim()
            if (prefs[KEY_DEVICE_ID] == null) {
                prefs[KEY_DEVICE_ID] = UUID.randomUUID().toString()
            }
        }
    }

    private companion object {
        val KEY_DEVICE_ID = stringPreferencesKey("device_id")
        val KEY_NAME = stringPreferencesKey("operator_name")
        val KEY_DEVICE_NOTE = stringPreferencesKey("device_note")
    }
}
