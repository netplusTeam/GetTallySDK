package com.netplus.qrengine.utils

import android.content.Context

class TallyAppPreferences private constructor(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    companion object {
        val PREFS_NAME = "TallyPref"
        val TOKEN = "token"
        val QRCODE_IMAGE = "qrcode_image"
        val DATE_GENERATED = "date_generated"
        val CARD_AND_BANK_SCHEME = "card_and_bank_scheme"
        val QRCODE_ID = "user_id"
        val IS_NEW_CARD_GENERATED = "is_new_card_generated"
        val IS_NEW_CARD_DISPLAYED = "is_new_card_displayed"

        @Volatile
        private var instance: TallyAppPreferences? = null

        fun getInstance(context: Context): TallyAppPreferences {
            return instance ?: synchronized(this) {
                instance ?: TallyAppPreferences(context).also { instance = it }
            }
        }
    }

    fun setStringValue(key: String, value: String) {
        editor.putString(key, value)
        editor.apply()
    }

    fun getStringValue(key: String) = sharedPreferences.getString(key, "")

    fun setBooleanValue(key: String, value: Boolean) {
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBooleanValue(key: String) = sharedPreferences.getBoolean(key, false)

    fun clearAllData() = editor.clear()
}