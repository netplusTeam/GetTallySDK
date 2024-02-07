package com.netplus.qrengine.backendRemote.responseManager

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit

/**
 * @author Anyanwu Nicholas(codeBaron)
 * @since 16-11-2023
 */

/**
 * Utility class for mapping error responses to ErrorModel instances.
 */
class ErrorMapper {

    companion object {
        // Singleton Retrofit instance for response conversion.
        private var retrofit: Retrofit? = null
    }

    /**
     * Parses the error message from the response body and converts it to an ErrorModel.
     * @param response The response body containing error information.
     * @return ErrorModel representing the parsed error information or null if parsing fails.
     */
    fun parseErrorMessage(response: ResponseBody?): ErrorModel? {
        // Get a converter for converting the response body to an ErrorModel instance.
        val converter: Converter<ResponseBody, ErrorModel>? =
            retrofit()
                ?.responseBodyConverter(ErrorModel::class.java, arrayOfNulls<Annotation>(0))
        // Attempt to convert the response body to an ErrorModel.
        return response?.let { converter?.convert(it) }
    }

    /**
     * Retrieves the Retrofit instance. (For internal use only)
     * @return Retrofit instance for response conversion.
     */
    private fun retrofit(): Retrofit? {
        return retrofit
    }
}