@file:OptIn(DelicateCoroutinesApi::class)

/**
 * A utility file containing functions related to various operations such as validation, formatting,
 * encryption, decryption, and UI manipulation in an Android application context.
 */
package com.netplus.qrengine.utils

import android.app.Activity
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.core.content.ContextCompat.getColor
import com.google.android.material.snackbar.Snackbar
import com.netplus.qrengine.backendRemote.model.qr.EncryptedQrModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

fun openInAppBrowser(context: Context, url: String?, colorResId: Int) {
    val customTabsServiceConnection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
            val customTabsSession = client.newSession(object : CustomTabsCallback() {
                override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
                    super.onNavigationEvent(navigationEvent, extras)
                    // Here you can handle navigation events
                    // Example: Log page loads, redirects, etc.
                    when (navigationEvent) {
                        NAVIGATION_STARTED -> {
                            // Navigation started
                        }
                        NAVIGATION_FINISHED -> {
                            // Navigation finished
                        }
                        // Add other cases as needed
                    }
                }
            })
            customTabsSession?.let {
                val customTabsIntent = CustomTabsIntent.Builder(it)
                    .setShowTitle(true)
                    .setToolbarColor(getColor(context, colorResId))
                    .build()

                // Important: Keep the service connection until the navigation is finished.
                customTabsIntent.launchUrl(context, Uri.parse(url))
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // Handle the service disconnection if needed
        }
    }

    // Bind the CustomTabsService
    CustomTabsClient.bindCustomTabsService(context, "com.android.chrome", customTabsServiceConnection)
}

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

private fun getBitmapFromImageView(imageView: ImageView): Bitmap {
    imageView.isDrawingCacheEnabled = true
    imageView.buildDrawingCache()
    val bitmap = Bitmap.createBitmap(imageView.drawingCache)
    imageView.isDrawingCacheEnabled = false
    return bitmap
}

/**
 * Saves the image displayed in an ImageView to the device's gallery.
 *
 * This function first attempts to extract a Bitmap from the provided ImageView. If successful,
 * it then proceeds to save this Bitmap to the device's storage using the MediaStore, effectively
 * adding the image to the gallery. This operation is performed asynchronously to avoid blocking
 * the UI thread. If the ImageView does not contain an image, or if the Bitmap extraction fails,
 * no action is taken. Optionally, you can uncomment the Toast messages to provide user feedback
 * on the operation's outcome (success or failure).
 *
 * @param context The context in which the operation is performed, used for accessing the ContentResolver
 *                and for potential UI updates like Toasts.
 * @param imageView The ImageView containing the image to be saved. The function attempts to extract
 *                  a Bitmap from this view.
 *
 * Note: This function assumes the existence of a `getBitmapFromImageView` method that can extract
 * a Bitmap from an ImageView, and a `saveImageToMediaStore` method that handles the actual saving
 * of the Bitmap to the device's storage. Implementations for these methods are required but not provided
 * here. Additionally, proper error handling and permissions checks should be implemented as needed,
 * especially for the MediaStore access.
 */
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

/**
 * Saves a Bitmap image to the device's MediaStore (Gallery).
 *
 * This method saves a given Bitmap image to the device's gallery by inserting it into the MediaStore.
 * It creates a new image file with predefined properties such as its display name, MIME type, and
 * storage directory. Upon successfully saving the image, it returns the Uri of the saved image as
 * a String. If the operation fails due to an exception, the method catches the exception, logs the
 * error, and returns null.
 *
 * Note: This operation requires the appropriate permissions to write to the device's storage. Make
 * sure to request and check these permissions before calling this method.
 *
 * @param context The context used to access the ContentResolver for performing the insert operation
 *                into the MediaStore.
 * @param bitmap The Bitmap image to be saved to the MediaStore. The image is compressed to PNG format
 *               before saving.
 * @return A String representing the Uri of the saved image in the MediaStore, or null if the save operation
 *         failed.
 */
private fun saveImageToMediaStore(context: Context, bitmap: Bitmap): String? {
    // Obtain the content resolver from the context.
    val contentResolver = context.contentResolver
    // Prepare content values with metadata about the image to be saved.
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "barcode_image") // Name of the file
        put(MediaStore.Images.Media.MIME_TYPE, "image/png") // MIME type
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES) // Save location
    }

    // Attempt to insert a new record into the MediaStore, which returns a Uri for the new file.
    val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    // If a Uri was successfully obtained, try to open an output stream to write the bitmap.
    try {
        uri?.let {
            contentResolver.openOutputStream(it)?.use { outputStream ->
                // Compress and write the bitmap to the output stream.
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
        }
    } catch (e: Exception) {
        // Log the exception and return null to indicate failure.
        e.printStackTrace()
        return null
    }

    // Return the string representation of the Uri if the operation was successful.
    return uri?.toString()
}


/**
 * Converts a String representing a numerical value to a formatted decimal String.
 *
 * This extension function on the String class takes a numerical value in string format and
 * converts it to a formatted decimal string. The format includes options for including a currency
 * symbol or not. This function is particularly useful for displaying human-readable monetary values
 * or other numerical data. The function uses the DecimalFormat class to ensure the formatted string
 * adheres to specific decimal and grouping conventions.
 *
 * @param includeCurrency A Boolean flag that determines whether the formatted string should include
 *                        a currency symbol. When true, the currency symbol (₦) is prepended to the
 *                        formatted number. When false, the number is formatted without a currency
 *                        symbol.
 * @return A formatted String representing the decimal number. If includeCurrency is true, the
 *         formatted string includes the currency symbol; otherwise, it's formatted as a plain
 *         number with two decimal places.
 * @throws NumberFormatException if the String cannot be converted to a Double. This can happen if
 *         the String does not represent a valid numerical value.
 *
 * Example Usage:
 * "1234.5".toDecimalFormat(true) // Returns "₦ 1,234.50"
 * "1234.5".toDecimalFormat(false) // Returns "1,234.50"
 */
fun String.toDecimalFormat(includeCurrency: Boolean): String {
    val symbols = DecimalFormatSymbols().apply {
        decimalSeparator = '.'
    }
    val pattern = if (includeCurrency) {
        "₦ #,##0.00"
    } else {
        "#,##0.00"
    }
    val decimalFormat = DecimalFormat(pattern, symbols)

    return decimalFormat.format(this.toDouble())
}


/**
 * Launches an activity specified by the generic type [T].
 *
 * This inline function simplifies the process of starting a new activity from a context object
 * (e.g., from within another activity or a fragment). It leverages Kotlin's reified type parameters
 * and higher-order function capabilities to allow for direct specification of the activity class to
 * launch and to optionally configure the intent used to start the activity.
 *
 * @param T The class of the activity to be launched. This is inferred from the call due to Kotlin's
 *          reified type parameter, eliminating the need for manual class specification.
 * @param options An optional Bundle of launch options for the activity. This can be used to animate
 *                the activity entrance and exit, among other things. The default value is null.
 * @param init An optional lambda with the Intent receiver type, allowing for further configuration
 *             of the intent before the activity is started. This could include adding extra data
 *             to the intent, setting action flags, etc. The default implementation does nothing.
 *
 * Example Usage:
 * // To launch an Activity named DetailActivity without any extra configurations:
 * context.launchActivity<DetailActivity>()
 *
 * // To launch an Activity with extra intent configurations:
 * context.launchActivity<DetailActivity> {
 *     putExtra("EXTRA_KEY", "Extra Value")
 *     addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
 * }
 *
 * Note: This function requires the caller to have a Context (e.g., an Activity or Application
 * context). It is an extension function on the Context class.
 */
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

/**
 * Extracts QR code IDs from a list of encrypted QR model objects.
 *
 * This function processes a list of `EncryptedQrModel` instances, extracting the QR code ID from each
 * and converting it to a string representation. The primary use case for this function is to retrieve
 * a list of QR code IDs in their string form from a collection of encrypted QR code data models, which
 * can then be used for display, logging, or further processing in the application.
 *
 * @param dataList A list of `EncryptedQrModel` objects from which QR code IDs are to be extracted.
 *                 Each `EncryptedQrModel` is expected to contain a QR code ID as one of its properties.
 * @return A list of strings, where each string represents the QR code ID of a corresponding
 *         `EncryptedQrModel` in the input list. The order of IDs in the returned list matches the order
 *         of models in the input list.
 *
 * Example Usage:
 * Assume `encryptedQrList` is a List<EncryptedQrModel> with populated QR code IDs.
 * val qrCodeIds: List<String> = extractQrCodeIds(encryptedQrList)
 * // `qrCodeIds` now contains the string representations of QR code IDs from `encryptedQrList`.
 */
fun extractQrCodeIds(dataList: List<EncryptedQrModel>): List<String> {
    return dataList.map { it.qrcodeId.toString() }
}


/**
 * Encrypts a given input string using AES encryption and encodes the result in Base64.
 *
 * This function takes a plaintext input string and a secret key, encrypts the input using AES
 * encryption with the ECB mode and PKCS5 padding, and then encodes the encrypted bytes into a
 * Base64 string. This method is useful for securely transmitting or storing sensitive information
 * that should not be readable without decryption.
 *
 * @param input The plaintext string to be encrypted.
 * @param secretKey The secret key used for encryption. The security of the encrypted data depends
 *                  significantly on the complexity and secrecy of this key.
 * @return A Base64 encoded string representing the encrypted version of the input text.
 * @throws RuntimeException If an encryption error occurs, encapsulating the original exception message.
 *
 * Example Usage:
 * ```
 * val secretMessage = "Hello, World!"
 * val secretKey = "ComplexKey12345" // In practice, use a more secure key generation strategy.
 * val encryptedMessage = encryptBase64(secretMessage, secretKey)
 * println(encryptedMessage) // Outputs the encrypted message in Base64 format.
 * ```
 *
 * Note: It's crucial to handle the secret key securely and ensure it is not hard-coded in a real application.
 * Also, using ECB mode is generally not recommended for encrypting multiple blocks of data with the same key
 * due to potential vulnerabilities. Consider using a more secure mode like CBC with an initialization vector.
 */
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


/**
 * Decrypts a given Base64 encoded input string using AES decryption.
 *
 * This function reverses the encryption process performed by a corresponding encryption function.
 * It takes a Base64 encoded string that was encrypted with AES, decodes it from Base64, and then
 * decrypts it using the same secret key. The result is the original plaintext. This method is
 * useful for securely retrieving original data from encrypted and encoded strings.
 *
 * @param input The Base64 encoded string that was previously encrypted.
 * @param secretKey The secret key used for decryption, which must match the key used for encryption.
 * @return The decrypted plaintext string.
 * @throws RuntimeException If a decryption error occurs, encapsulating the original exception message.
 *
 * Example Usage:
 * ```
 * val encryptedBase64Message = "EncryptedAndEncodedString"
 * val secretKey = "ComplexKey12345" // Must match the key used for encryption.
 * val decryptedMessage = decryptBase64(encryptedBase64Message, secretKey)
 * println(decryptedMessage) // Outputs the original plaintext message.
 * ```
 *
 * Note: Security of the decrypted data relies on the secrecy and complexity of the secret key.
 * It is critical to ensure that the secret key is managed securely and not exposed or hard-coded
 * in a real application. Additionally, as with the encryption function, using ECB mode for decryption
 * is generally not recommended for data security reasons. Consider using a more secure mode like CBC
 * with an initialization vector for both encryption and decryption.
 */
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


/**
 * Generates a cryptographic key for AES encryption or decryption from a given password.
 *
 * This function uses the PBKDF2 (Password-Based Key Derivation Function 2) with HMAC-SHA256 to
 * produce a secure cryptographic key from a specified password. The key derivation process
 * involves using a salt and a high number of iterations to enhance the security of the derived
 * key against brute-force attacks. The generated key is suitable for AES encryption or decryption
 * operations.
 *
 * @param password The password input used to generate the cryptographic key.
 * @return A SecretKeySpec object representing the generated AES key.
 *
 * Note:
 * - The salt used in this function is a fixed array of bytes. For improved security, it's recommended
 *   to use a securely generated, random salt that is unique for each encryption operation. The salt
 *   should be stored or transmitted along with the encrypted data for use in the decryption process.
 * - The iteration count (65536) is chosen to make the key derivation process computationally expensive,
 *   thereby increasing security. However, this also affects performance, so the iteration count should
 *   be chosen based on a balance between security and performance requirements.
 * - The key length is set to 256 bits, which is a common choice for AES encryption, offering a high
 *   level of security.
 *
 * Example Usage:
 * ```
 * val password = "VerySecurePassword123!"
 * val aesKey = generateKey(password)
 * // Now, aesKey can be used for AES encryption or decryption operations.
 * ```
 *
 * Important: Managing cryptographic keys and passwords securely is crucial. Ensure that passwords
 * are strong and stored securely (not hard-coded) in production applications. Additionally, consider
 * the security implications of using a fixed salt and adjust the implementation to use a random, unique
 * salt for each encryption operation for enhanced security.
 */
private fun generateKey(password: String): Key {
    val salt = ByteArray(16) // Should be securely generated and stored
    val iterationCount = 65536
    val keyLength = 256
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val spec = PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLength)
    return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
}


/**
 * Converts a date string from ISO 8601 format to a more readable date and time format.
 *
 * This function takes a date string in the ISO 8601 format (e.g., "2023-01-01T12:00:00.000Z") and
 * converts it into a more human-friendly date and time format (e.g., "01 January 2023, 12:00").
 * The conversion process accounts for time zone differences, converting the input UTC time to the
 * local time zone of the device.
 *
 * @param dateStr The date string in ISO 8601 format to be converted.
 * @return A string representing the formatted date and time in the local time zone.
 *
 * Usage Example:
 * ```
 * val isoDateStr = "2023-01-01T12:00:00.000Z"
 * val readableDate = convertDateToReadableFormat(isoDateStr)
 * println(readableDate) // Output: "01 January 2023, 14:00" (output may vary based on the local time zone)
 * ```
 *
 * Note:
 * - The input date string must conform to the ISO 8601 format with the 'Z' designator for UTC time.
 * - The output format is fixed to "dd MMMM yyyy, HH:mm". If a different format is needed, adjust the
 *   outputFormat pattern accordingly.
 * - This function automatically adjusts the output to the local time zone of the device. To use a
 *   different time zone, modify the timeZone property of the outputFormat object.
 *
 * Important: Ensure that the input date string format matches the expected format. Incorrect or
 * malformed input strings may lead to ParseExceptions or incorrect conversions.
 */
fun convertDateToReadableFormat(dateStr: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    inputFormat.timeZone = TimeZone.getTimeZone("UTC")

    val outputFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
    outputFormat.timeZone = TimeZone.getDefault() // Or specify a particular timezone if needed

    val date = inputFormat.parse(dateStr) ?: return "Invalid date"
    return outputFormat.format(date)
}

/**
 * Shows a Snackbar message from any Context (e.g., Activity).
 *
 * @param message The message to show.
 * @param length The duration for which the Snackbar is shown. Default is LENGTH_LONG.
 * @param actionText The text of the action item on the Snackbar.
 * @param action The lambda function to be invoked when the action item is clicked.
 */
fun Context.showSnackbar(
    message: String,
    length: Int = Snackbar.LENGTH_LONG,
    actionText: String? = null,
    action: ((View) -> Unit)? = null
) {
    val view = getActivityRootView() ?: return // Return if a suitable view is not found
    val snackbar = Snackbar.make(view, message, length)

    if (actionText != null && action != null) {
        snackbar.setAction(actionText) {
            action(it)
        }
    }
    snackbar.show()
}

/**
 * Finds the root view of the current Activity context.
 *
 * @return The root view of the current Activity, or `null` if not found.
 */
private fun Context.getActivityRootView(): View? {
    val activity = this as? Activity ?: return null
    val contentView = activity.findViewById<View>(android.R.id.content)
    return contentView.rootView
}