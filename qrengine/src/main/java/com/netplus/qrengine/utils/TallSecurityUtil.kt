package com.netplus.qrengine.utils

import android.content.Context
import android.util.Base64
import com.google.crypto.tink.Aead
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.netplus.qrengine.backendRemote.model.qr.EncryptedQrModel
import java.nio.charset.StandardCharsets

object TallSecurityUtil {

    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "MyKeyAlias"
    private const val PREFERENCE_FILE = "EncryptedDataPrefs"
    private const val DATA_KEY = "EncryptedData"

    init {
        AeadConfig.register()
    }

    private fun getOrGenerateKey(context: Context): Aead {
        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, KEY_ALIAS, KEYSTORE_PROVIDER)
            .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
            .build()
            .keysetHandle

        return keysetHandle.getPrimitive(Aead::class.java)
    }

    private fun encryptData(context: Context, data: String): ByteArray {
        val aead = getOrGenerateKey(context)
        return aead.encrypt(data.toByteArray(StandardCharsets.UTF_8), ByteArray(0))
    }

    private fun decryptData(context: Context, encryptedData: ByteArray): String {
        val aead = getOrGenerateKey(context)
        return String(aead.decrypt(encryptedData, ByteArray(0)), StandardCharsets.UTF_8)
    }

    fun storeData(context: Context, newData: EncryptedQrModel) {
        val currentData = retrieveData(context) ?: listOf()
        val updatedData = currentData + newData
        val jsonData = convertToJson(updatedData)
        val encryptedData = encryptData(context, jsonData)
        val base64EncryptedData = Base64.encodeToString(encryptedData, Base64.DEFAULT)
        val sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(DATA_KEY, base64EncryptedData).apply()
    }

    fun retrieveData(context: Context): List<EncryptedQrModel>? {
        val sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)
        val base64EncryptedData = sharedPreferences.getString(DATA_KEY, null)
        return base64EncryptedData?.let {
            val encryptedData = Base64.decode(it, Base64.DEFAULT)
            val decryptedJson = decryptData(context, encryptedData)
            convertFromJson(decryptedJson)
        }
    }

    fun deleteDataById(context: Context, qrcodeId: String) {
        val currentData = retrieveData(context) ?: return
        val qrcodeData = currentData.filterNot { it.qrcodeId == qrcodeId }
        val jsonData = convertToJson(qrcodeData)
        val encryptedData = encryptData(context, jsonData)
        val base64EncryptedData = Base64.encodeToString(encryptedData, Base64.DEFAULT)
        val sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(DATA_KEY, base64EncryptedData).apply()
    }

    fun deleteAllData(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)
        sharedPreferences.edit().remove(DATA_KEY).apply()
    }

    private fun convertToJson(data: List<EncryptedQrModel>): String {
        val gson = Gson()
        return gson.toJson(data)
    }

    private fun convertFromJson(json: String): List<EncryptedQrModel>? {
        val gson = Gson()
        val type = object : TypeToken<List<EncryptedQrModel>>() {}.type
        return gson.fromJson(json, type)
    }
}