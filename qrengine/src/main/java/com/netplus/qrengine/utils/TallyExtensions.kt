@file:OptIn(DelicateCoroutinesApi::class)

/**
 * A utility file containing functions related to various operations such as validation, formatting,
 * encryption, decryption, and UI manipulation in an Android application context.
 */
package com.netplus.qrengine.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatEditText
import com.netplus.qrengine.backendRemote.model.qr.EncryptedQrModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.security.Key
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

val listOfCardSchemes = listOf("Visa", "MasterCard", "American Express", "Discover", "Verve")

/**
 * Checks if the given card number is valid for the specified card type.
 *
 * @param cardNumber The card number as a String.
 * @param cardType The type of the card (e.g., Visa, MasterCard).
 * @return Boolean indicating whether the card number is valid for the given card type.
 */
fun isValidCardNumber(cardNumber: String, cardType: String): Boolean {
    val lengthMap = mapOf(
        "Visa" to setOf(13, 16),
        "MasterCard" to setOf(16),
        "American Express" to setOf(15),
        "Discover" to setOf(16),
        "Verve" to setOf(16) // Update if the actual length is different
    )

    return lengthMap[cardType]?.contains(cardNumber.length) == true
}

/**
 * Determines the card scheme based on the given card number.
 *
 * @param cardNumber The card number as a String.
 * @return The card scheme as a String (e.g., Visa, MasterCard, American Express, Discover, Verve).
 */
fun getCardScheme(cardNumber: String): String {
    val visaPattern = "^4[0-9]{6,}$"
    val mastercardPattern = "^5[1-5][0-9]{5,}$"
    val amexPattern = "^3[47][0-9]{5,}$"
    val discoverPattern = "^6(?:011|5[0-9]{2})[0-9]{3,}$"
    val vervePattern = "^5[0-9]{6,}$" // Assuming Verve uses a pattern similar to MasterCard

    val patterns = mapOf(
        "Visa" to visaPattern,
        "MasterCard" to mastercardPattern,
        "American Express" to amexPattern,
        "Discover" to discoverPattern,
        "Verve" to vervePattern
    )

    for ((scheme, pattern) in patterns) {
        if (Pattern.matches(pattern, cardNumber)) {
            return scheme
        }
    }

    return "Unknown"
}

/**
 * Determines the card type based on the given card number.
 *
 * @param cardNumber The card number as a String.
 * @return The card type as a String (e.g., Visa, MasterCard, American Express, Discover, Verve).
 */
fun getCardType(cardNumber: String): String {
    return if (Pattern.matches("^4[0-9]{6,}$", cardNumber)) {
        "Visa"
    } else if (Pattern.matches("^5[1-5][0-9]{5,}$", cardNumber)) {
        "MasterCard"
    } else if (Pattern.matches("^3[47][0-9]{5,}$", cardNumber)) {
        "American Express"
    } else if (Pattern.matches("^6(?:011|5[0-9]{2})[0-9]{3,}$", cardNumber)) {
        "Discover"
    } else if (Pattern.matches("^5[0-9]{6,}$", cardNumber)) {
        "Verve"
    } else {
        "Unknown"
    }
}

/**
 * Formats the given card number by inserting hyphens at specific intervals.
 *
 * @param cardNumber The card number as a String.
 * @return The formatted card number as a String.
 */
fun formatCardNumber(cardNumber: String): String {
    val spaceIndices = calculateSpaceIndices(cardNumber.length)
    val formattedCardNumber = StringBuilder()

    for (i in cardNumber.indices) {
        if (i in spaceIndices) {
            formattedCardNumber.append(" - ")
        }
        formattedCardNumber.append(cardNumber[i])
    }

    return formattedCardNumber.toString()
}

/**
 * Calculates the indices where spaces should be inserted in a card number.
 *
 * @param cardNumberLength The length of the card number.
 * @return A List of Integers indicating the indices for space insertion.
 */
fun calculateSpaceIndices(cardNumberLength: Int): List<Int> {
    val spaceIndices = mutableListOf<Int>()

    // Calculate hyphen insertion points for groups of four digits
    for (i in 4 until cardNumberLength step 4) {
        spaceIndices.add(i)
    }

    return spaceIndices
}

/**
 * Validates if the given expiry date is not in the past.
 *
 * @param month The expiry month as an Integer.
 * @param year The expiry year as an Integer (last two digits).
 * @return Boolean indicating whether the expiry date is valid (not in the past).
 */
fun isValidExpiryDate(month: Int, year: Int): Boolean {
    val currentDate = Calendar.getInstance()
    val currentYear =
        currentDate.get(Calendar.YEAR) % 100 // Get last two digits of the current year
    val currentMonth = currentDate.get(Calendar.MONTH) + 1 // Month is 0-indexed

    return year > currentYear || (year == currentYear && month >= currentMonth)
}

/**
 * Sets the visibility of the View to VISIBLE.
 */
fun View.visible() {
    this.visibility = View.VISIBLE
}

/**
 * Sets the visibility of the View to INVISIBLE.
 */
fun View.invisible() {
    this.visibility = View.INVISIBLE
}

/**
 * Sets the visibility of the View to GONE.
 */
fun View.gone() {
    this.visibility = View.GONE
}

/**
 * Sets a text changed listener and an editor action listener on the given AppCompatEditText to move focus to the next EditText.
 *
 * @param editText The current AppCompatEditText.
 * @param nextEditText The next AppCompatEditText to focus on. Nullable if it's the last one.
 */
fun setEditTextListener(editText: AppCompatEditText, nextEditText: AppCompatEditText?) {
    editText.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (s?.length == 1) {
                nextEditText?.requestFocus()
            }
        }
    })

    editText.setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            nextEditText?.requestFocus()
            return@setOnEditorActionListener true
        }
        false
    }
}

/**
 * Converts a Base64 encoded string to a Bitmap image.
 *
 * @param base64String The Base64 encoded string.
 * @return The decoded Bitmap image.
 */
fun decodeBase64ToBitmap(base64String: String): Bitmap? {
    try {
        val byteData = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(byteData, 0, byteData.size)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

private fun getBitmapFromImageView(imageView: ImageView): Bitmap? {
    imageView.isDrawingCacheEnabled = true
    imageView.buildDrawingCache()
    val bitmap = Bitmap.createBitmap(imageView.drawingCache)
    imageView.isDrawingCacheEnabled = false
    return bitmap
}


fun saveImageToGallery(context: Context, imageView: ImageView) {
    val bitmap = getBitmapFromImageView(imageView)

    if (bitmap != null) {
        GlobalScope.launch(Dispatchers.IO) {
            val uri = saveImageToMediaStore(context, bitmap)
            // You can perform any additional actions here, like showing a toast or updating UI
            //Toast.makeText(context, "Saved successfully", Toast.LENGTH_SHORT).show()
        }
    } else {
        // Handle the case when the bitmap is null (e.g., if the ImageView doesn't have an image)
        //Toast.makeText(context, "Unable to find QRCODE", Toast.LENGTH_SHORT).show()
    }
}

private fun saveImageToMediaStore(context: Context, bitmap: Bitmap): String? {
    val contentResolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "barcode_image")
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    }

    val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    try {
        if (uri != null) {
            val outputStream: OutputStream? = contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }

    return uri?.toString()
}

fun String.toDecimalFormat(includeCurrency: Boolean): String {

    val symbols = DecimalFormatSymbols()
    symbols.decimalSeparator = '.'
    val decimalFormat = if (includeCurrency) {
        DecimalFormat("₦ #,##0.00")
    } else {
        DecimalFormat("#,##0.00")
    }

    return decimalFormat.format(toDouble())
}

inline fun <reified T : Any> Context.launchActivity(
    options: Bundle? = null,
    noinline init: Intent.() -> Unit = {}
) {
    val intent = newIntent<T>(this)
    intent.init()
    startActivity(intent, options)
}

inline fun <reified T : Any> newIntent(context: Context): Intent = Intent(context, T::class.java)

inline fun <reified T : Any> Activity.extra(key: String, default: T? = null) = lazy {
    val value = intent?.extras?.get(key)
    if (value is T) value else default
}

fun extractQrCodeIds(dataList: List<EncryptedQrModel>): List<String> {
    return dataList.map { it.qrcodeId.toString() }
}

fun encryptBase64(input: String, secretKey: String): String {
    try {
        val key = generateKey(secretKey)
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes = cipher.doFinal(input.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    } catch (e: Exception) {
        throw RuntimeException("Error encrypting: ${e.message}")
    }
}

fun decryptBase64(input: String, secretKey: String): String {
    try {
        val key = generateKey(secretKey)
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decodedBytes = Base64.decode(input, Base64.NO_WRAP)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes)
    } catch (e: Exception) {
        throw RuntimeException("Error decrypting: ${e.message}")
    }
}

private fun generateKey(password: String): Key {
    val salt = ByteArray(16) // Should be securely generated and stored
    val iterationCount = 65536
    val keyLength = 256
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val spec = PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLength)
    return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
}

fun convertDateToReadableFormat(dateStr: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    inputFormat.timeZone = TimeZone.getTimeZone("UTC")

    val outputFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
    outputFormat.timeZone = TimeZone.getDefault() // Or specify a particular timezone if needed

    val date = inputFormat.parse(dateStr)
    return outputFormat.format(date)
}
