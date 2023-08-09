package com.dreamreco.firebaseappstreamtest.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// TODO : jooDiary 리펙터링 적용
@Singleton
class MyDataStoreUtil @Inject constructor(@ApplicationContext private val context: Context) {

    // At the top level of your kotlin file:
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "diary_dataStore")

    suspend fun saveDataToDataStore(key: String, inputValue: Any) {
        val dataStoreKey = stringPreferencesKey(key)
        val outputValue = when (inputValue) {
            is List<*> -> Gson().toJson(inputValue)
            else -> inputValue.toString()
        }

        context.dataStore.edit { settings ->
            settings[dataStoreKey] = outputValue
        }
    }

    fun getIntDataFromDataStore(context: Context, key: String, emptyInt: Int): Flow<Int> {
        val dataStoreKey = stringPreferencesKey(key)
        return context.dataStore.data
            .map { preferences ->
                // No type safety.
                preferences[dataStoreKey]?.toInt() ?: emptyInt
            }
    }

    fun getStringDataFromDataStore(
        context: Context,
        key: String,
        emptyString: String
    ): Flow<String> {
        val dataStoreKey = stringPreferencesKey(key)
        return context.dataStore.data
            .map { preferences ->
                // No type safety.
                preferences[dataStoreKey]?.toString() ?: emptyString
            }
    }

    fun getLongDataFromDataStore(context: Context, key: String, emptyLong: Long): Flow<Long> {
        val dataStoreKey = stringPreferencesKey(key)
        return context.dataStore.data
            .map { preferences ->
                // No type safety.
                preferences[dataStoreKey]?.toLong() ?: emptyLong
            }
    }

    fun getBooleanDataFromDataStore(
        context: Context,
        key: String,
        emptyBoolean: Boolean?
    ): Flow<Boolean?> {
        val dataStoreKey = stringPreferencesKey(key)
        return context.dataStore.data
            .map { preferences ->
                // No type safety.
                preferences[dataStoreKey]?.toBoolean() ?: emptyBoolean
            }
    }

    fun getStringListDataFromDataStore(
        context: Context,
        key: String,
        emptyList: List<String> = emptyList()
    ): Flow<List<String>> {
        val dataStoreKey = stringPreferencesKey(key)
        return context.dataStore.data
            .map { preferences ->
                val jsonString = preferences[dataStoreKey] ?: ""
                Gson().fromJson(jsonString, List::class.java) as? List<String> ?: emptyList
            }
    }
}