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
         */
        fun onError(errorMessage: String?)
    }

    /**
     * Handles the API response and notifies the client code through the provided callback.
     *
     * @param response The response data from the API.
     * @param errorMessage A nullable error message associated with the API response.
     * @param callback The callback interface instance to notify about success or error.
     */
    fun handleResponse(
        response: T?,
        errorMessage: String?,
        callback: Callback<T>
    ) {
        if (errorMessage != null) {
            callback.onError(errorMessage)
        } else {
            callback.onSuccess(response)
        }
    }
}