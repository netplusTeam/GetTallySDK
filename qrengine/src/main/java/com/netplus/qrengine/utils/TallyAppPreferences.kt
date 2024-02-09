package com.netplus.qrengine.utils

import android.content.Context

// Defines a class to manage application preferences specifically for the TallyApp.
class TallyAppPreferences private constructor(context: Context) {

    // Initializes sharedPreferences for storing and retrieving preferences.
    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    // Editor for sharedPreferences to make changes in the data.
    private val editor = sharedPreferences.edit()

    companion object {
        // Constant for the preferences file name.
        const val PREFS_NAME = "TallyPref"
        // Keys for different preferences stored.
        const val TOKEN = "token"
        const val QRCODE_IMAGE = "qrcode_image"
        const val DATE_GENERATED = "date_generated"
        const val CARD_AND_BANK_SCHEME = "card_and_bank_scheme"
        const val QRCODE_ID = "user_id"
        const val IS_NEW_CARD_GENERATED = "is_new_card_generated"
        const val IS_NEW_CARD_DISPLAYED = "is_new_card_displayed"

        // Volatile instance to ensure the instance remains a singleton across all threads.
        @Volatile
        private var instance: TallyAppPreferences? = null

        // Provides a global access point to the TallyAppPreferences instance.
        fun getInstance(context: Context): TallyAppPreferences {
            // Double-check locking to ensure only one instance is created.
            return instance ?: synchronized(this) {
                instance ?: TallyAppPreferences(context).also { instance = it }
            }
        }
    }

    // Stores a string value in preferences.
    fun setStringValue(key: String, value: String) {
        editor.putString(key, value)
        editor.apply() // Commit changes asynchronously.
    }

    // Retrieves a string value from preferences, defaulting to an empty string if not found.
    fun getStringValue(key: String) = sharedPreferences.getString(key, "")

    // Stores a boolean value in preferences.
    fun setBooleanValue(key: String, value: Boolean) {
        editor.putBoolean(key, value)
        editor.apply() // Commit changes asynchronously.
    }

    // Retrieves a boolean value from preferences, defaulting to false if not found.
    fun getBooleanValue(key: String) = sharedPreferences.getBoolean(key, false)

    // Clears all data from the preferences.
    fun clearAllData() = editor.clear().apply()
}
