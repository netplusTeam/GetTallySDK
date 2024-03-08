package com.netplus.qrengine.backendRemote.responseManager

/**
 * @author Anyanwu Nicholas(codeBaron)
 * @since 16-11-2023
 */

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.ResponseBody

class DynamicJsonErrorParser {

    companion object {
        /**
         * Parses the error JSON contained in a ResponseBody from the backend to extract the message
         * and any other details. Assumes the "message" key is always present.
         *
         * @param responseBody The optional ResponseBody received from the backend.
         * @return A pair where the first element is the message and the second is the original JsonObject
         * for further processing, or null if responseBody is null or cannot be parsed.
         */
        fun parseError(responseBody: ResponseBody?): Pair<String?, JsonObject?>? {
            if (responseBody == null) return null

            return try {
                // Convert ResponseBody to string and then to JsonObject
                val errorJsonStr = responseBody.string()
                val jsonObj = JsonParser.parseString(errorJsonStr).asJsonObject

                // Extract the message key
                val message = jsonObj.get("message")?.asString

                // Return the message and the original JsonObject
                Pair(message, jsonObj)
            } catch (e: Exception) {
                // Handle or log parsing error
                null
            }
        }
    }
}

