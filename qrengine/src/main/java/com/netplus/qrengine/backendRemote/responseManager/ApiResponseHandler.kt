package com.netplus.qrengine.backendRemote.responseManager

/**
 * Generic utility class for handling API responses.
 *
 * @param T The type of data in the API response.
 */
class ApiResponseHandler<T> {

    /**
     * Callback interface to handle success and error scenarios of API responses.
     */
    interface Callback<T> {
        /**
         * Invoked when the API response is successful.
         *
         * @param data The response data of type T.
         */
        fun onSuccess(data: T?)

        /**
         * Invoked when there is an error in the API response.
         *
         * @param errorMessage The error message associated with the API response.
         * @param statusCode The HTTP status code associated with the error, if applicable.
         */
        fun onError(errorMessage: String?, statusCode: Int? = null)
    }

    /**
     * Handles the API response, including success, error, and various HTTP error status codes,
     * and notifies the client code through the provided callback.
     *
     * @param response The response data from the API.
     * @param errorMessage A nullable error message associated with the API response.
     * @param statusCode A nullable HTTP status code associated with the API response.
     * @param callback The callback interface instance to notify about success or error.
     */
    fun handleResponse(
        response: T?,
        errorMessage: String?,
        statusCode: Int? = null,
        callback: Callback<T>
    ) {
        if (errorMessage != null || statusCode != null) {
            // Providing additional context to the onError callback based on the status code
            val adjustedErrorMessage = when (statusCode) {
                in 500..599 -> "Internal error: $errorMessage"
                else -> errorMessage // You can handle more specific cases or provide more detailed messages based on status codes
            }
            callback.onError(adjustedErrorMessage, statusCode)
        } else {
            callback.onSuccess(response)
        }
    }
}
