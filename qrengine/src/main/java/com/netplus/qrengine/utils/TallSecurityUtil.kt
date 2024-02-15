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

// Singleton object for handling security utilities.
object TallSecurityUtil {

    // Constants for the keystore provider, key alias, preference file name, and data key.
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "MyKeyAlias"
    private const val PREFERENCE_FILE = "EncryptedDataPrefs"
    private const val DATA_KEY = "EncryptedData"

    // Static initializer block to register AEAD configuration.
    init {
        AeadConfig.register()
    }

    /**
     * Retrieves or generates an AEAD (Authenticated Encryption with Associated Data) key.
     * @param context The Android context.
     * @return An instance of Aead for encryption or decryption.
     */
    private fun getOrGenerateKey(context: Context): Aead {
        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, KEY_ALIAS, KEYSTORE_PROVIDER) // Associates the key with a shared preferences file.
            .withKeyTemplate(AeadKeyTemplates.AES256_GCM) // Specifies the key template to use.
            .build()
            .keysetHandle

        return keysetHandle.getPrimitive(Aead::class.java)
    }

    /**
     * Encrypts the provided data.
     * @param context The Android context.
     * @param data The data to be encrypted as a String.
     * @return The encrypted data as a ByteArray.
     */
    private fun encryptData(context: Context, data: String): ByteArray {
        val aead = getOrGenerateKey(context)
        return aead.encrypt(data.toByteArray(StandardCharsets.UTF_8), ByteArray(0))
    }

    /**
     * Decrypts the provided encrypted data.
     * @param context The Android context.
     * @param encryptedData The data to be decrypted as a ByteArray.
     * @return The decrypted data as a String.
     */
    private fun decryptData(context: Context, encryptedData: ByteArray): String {
        val aead = getOrGenerateKey(context)
        return String(aead.decrypt(encryptedData, ByteArray(0)), StandardCharsets.UTF_8)
    }

    /**
     * Stores encrypted data in shared preferences.
     * @param context The Android context.
     * @param newData The data to be stored, wrapped in an EncryptedQrModel object.
     */
    fun storeData(context: Context, newData: EncryptedQrModel) {
        val currentData = retrieveData(context) ?: listOf()
        val updatedData = currentData + newData
        val jsonData = convertToJson(updatedData)
        val encryptedData = encryptData(context, jsonData)
        val base64EncryptedData = Base64.encodeToString(encryptedData, Base64.DEFAULT)
        val sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(DATA_KEY, base64EncryptedData).apply()
    }

    /**
     * Retrieves encrypted data from shared preferences and decrypts it.
     * @param context The Android context.
     * @return A list of EncryptedQrModel objects or null if no data is found.
     */
    fun retrieveData(context: Context): List<EncryptedQrModel>? {
        val sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)
        val base64EncryptedData = sharedPreferences.getString(DATA_KEY, null)
        return base64EncryptedData?.let {
            val encryptedData = Base64.decode(it, Base64.DEFAULT)
            val decryptedJson = decryptData(context, encryptedData)
            convertFromJson(decryptedJson)
        }
    }

    /**
     * Deletes data associated with a specific QR code ID.
     * @param context The Android context.
     * @param qrcodeId The ID of the QR code to delete.
     */
    fun deleteDataById(context: Context, qrcodeId: String) {
        val currentData = retrieveData(context) ?: return
        val qrcodeData = currentData.filterNot { it.qrcodeId == qrcodeId }
        val jsonData = convertToJson(qrcodeData)
        val encryptedData = encryptData(context, jsonData)
        val base64EncryptedData = Base64.encodeToString(encryptedData, Base64.DEFAULT)
        val sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(DATA_KEY, base64EncryptedData).apply()
    }

    /**
     * Deletes all stored data.
     * @param context The Android context.
     */
    fun deleteAllData(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)
        sharedPreferences.edit().remove(DATA_KEY).apply()
    }

    /**
     * Converts a list of EncryptedQrModel objects to a JSON String.
     * @param data The list of EncryptedQrModel objects to be converted.
     * @return A JSON String representing the list.
     */
    private fun convertToJson(data: List<EncryptedQrModel>): String {
        val gson = Gson()
        return gson.toJson(data)
    }

    /**
     * Converts a JSON String to a list of EncryptedQrModel objects.
     * @param json The JSON String to be converted.
     * @return A list of EncryptedQrModel objects.
     */
    private fun convertFromJson(json: String): List<EncryptedQrModel>? {
        val gson = Gson()
        val type = object : TypeToken<List<EncryptedQrModel>>() {}.type
        return gson.fromJson(json, type)
    }
}
