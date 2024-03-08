package com.netplus.qrengine.utils

import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import com.netplus.qrengine.backendRemote.model.card.CheckOutResponse
import com.netplus.qrengine.backendRemote.model.card.PayPayload
import com.netplus.qrengine.backendRemote.model.card.PayResponse
import com.netplus.qrengine.backendRemote.model.keys.FinancialInstitutionKeyResponse
import com.netplus.qrengine.backendRemote.model.keys.FinancialInstitutionPayload
import com.netplus.qrengine.backendRemote.model.keys.get.GetFinancialInstitutionKeyResponse
import com.netplus.qrengine.backendRemote.model.login.LoginResponse
import com.netplus.qrengine.backendRemote.model.merchants.AllMerchantResponse
import com.netplus.qrengine.backendRemote.model.merchants.MerchantResponse
import com.netplus.qrengine.backendRemote.model.qr.GenerateQrcodeResponse
import com.netplus.qrengine.backendRemote.model.qr.retreive.GetTokenizedCardsResponse
import com.netplus.qrengine.backendRemote.model.qr.store.StoreTokenizedCardsResponse
import com.netplus.qrengine.backendRemote.model.transactions.updatedTransaction.UpdatedTransactionResponse
import com.netplus.qrengine.backendRemote.model.verve.VerveOtpPayload
import com.netplus.qrengine.backendRemote.responseManager.ApiResponseHandler
import com.netplus.qrengine.internet.handler.InternetConfigViewModel
import com.netplus.qrengine.mvvm.TallyViewModel
import org.koin.java.KoinJavaComponent.getKoin

/**
 * Activity class responsible for authenticating the bank and generating QR codes.
 */
class TallyQrcodeGenerator : AppCompatActivity() {

    companion object {
        // Lazy initialization of TallyViewModel and InternetConfigViewModel
        private val tallyViewModel: TallyViewModel by lazy { getKoin().get<TallyViewModel>() }
        private val internetConfigViewModel: InternetConfigViewModel by lazy { getKoin().get<InternetConfigViewModel>() }

    }

    /**
     * Authenticates a bank user with the provided email and password, handling the login response.
     *
     * This function attempts to log in a user using their email and password credentials through the `tallyViewModel.login` method.
     * Upon receiving the login response, it either triggers a success or error callback to handle the result appropriately.
     * In the case of a successful login, it also saves the authentication token to the application's shared preferences for
     * subsequent use.
     *
     * @param email The email address of the user attempting to log in.
     * @param password The password of the user.
     * @param callback A [TallyResponseCallback] instance to handle the success or failure of the login attempt.
     *
     * Usage Example:
     * ```
     * authenticateBank("user@example.com", "password123", object : TallyResponseCallback<LoginResponse> {
     *     override fun success(data: LoginResponse?) {
     *         // Handle successful authentication here, e.g., navigate to the next screen
     *     }
     *
     *     override fun failed(errorMessage: String?) {
     *         // Handle failed authentication here, e.g., show an error message to the user
     *     }
     * })
     * ```
     *
     * Note:
     * - The `tallyViewModel` used in this function should be initialized and provided as part of the calling context or scope.
     * - The `TallyAppPreferences.getInstance(this@TallyQrcodeGenerator)` call should refer to a valid context. Ensure `this@TallyQrcodeGenerator`
     *   correctly references an existing context or change it accordingly to fit your implementation context.
     * - The callback mechanism allows for asynchronous handling of the login process, enabling the UI to remain responsive while awaiting the
     *   login response.
     *
     * Important:
     * - Ensure that the email and password provided to this function are collected in a secure manner to protect user credentials.
     * - Handling of the authentication token should follow best practices for security, ensuring that it is stored securely and not exposed
     *   to potential security threats.
     */
    fun authenticateBank(
        email: String,
        password: String,
        callback: TallyResponseCallback<LoginResponse>
    ) {
        tallyViewModel.login(
            email,
            password,
            object : ApiResponseHandler.Callback<LoginResponse> {
                override fun onSuccess(data: LoginResponse?) {
                    // Handle successful login response
                    callback.success(data)
                    // Save the authentication token to preferences
                    TallyAppPreferences.getInstance(this@TallyQrcodeGenerator)
                        .setStringValue(TallyAppPreferences.TOKEN, data?.token ?: "")
                }

                override fun onError(errorMessage: String?, statusCode: Int?) {
                    // Handle login error
                    callback.failed(errorMessage)
                }
            })
    }

    /**
     * Generates a QR code based on user and card details, handling the generation response.
     *
     * This function initiates a QR code generation request using various user and card details through the `tallyViewModel.generateQrcode` method.
     * Upon receiving the generation response, it either triggers a success or error callback to manage the outcome as needed.
     * In the event of successful QR code generation, it also passes the generated QR data for further processing or transfer.
     *
     * @param userId The unique identifier of the user.
     * @param cardCvv The CVV of the user's card.
     * @param cardExpiry The expiry date of the card.
     * @param cardNumber The card number.
     * @param cardScheme The card scheme (e.g., Visa, MasterCard).
     * @param email The email address of the user.
     * @param fullName The full name of the cardholder.
     * @param issuingBank The bank issuing the card.
     * @param mobilePhone The mobile phone number of the user.
     * @param appCode An application-specific code, if applicable.
     * @param cardPin The PIN associated with the card.
     * @param callback A [TallyResponseCallback] instance to handle the success or failure of the QR code generation attempt.
     *
     * Usage Example:
     * ```
     * generateQrcode(123, "123", "12/34", "1234567890123456", "Visa", "user@example.com", "John Doe", "Bank Name", "1234567890",
     * "AppCode", "1234", object : TallyResponseCallback<GenerateQrcodeResponse> {
     *     override fun success(data: GenerateQrcodeResponse?) {
     *         // Handle successful QR code generation here, e.g., display the QR code
     *     }
     *
     *     override fun failed(errorMessage: String?) {
     *         // Handle QR code generation failure here, e.g., show an error message
     *     }
     * })
     * ```
     *
     * Note:
     * - The `tallyViewModel` used in this function should be initialized and provided as part of the calling context or scope.
     * - Ensure that the user and card details provided to this function are collected and used in compliance with relevant data protection regulations.
     * - The callback mechanism allows for asynchronous handling of the QR code generation process, enabling the UI to remain responsive while awaiting the
     *   response.
     *
     * Important:
     * - Validate all inputs to ensure they are in the correct format and meet any necessary criteria before attempting to generate a QR code.
     * - Handle the generated QR code data securely, especially if it contains sensitive information.
     */
    fun generateQrcode(
        userId: Int,
        cardCvv: String,
        cardExpiry: String,
        cardNumber: String,
        cardScheme: String,
        email: String,
        fullName: String,
        issuingBank: String,
        mobilePhone: String,
        appCode: String,
        cardPin: String,
        callback: TallyResponseCallback<GenerateQrcodeResponse>
    ) {
        tallyViewModel.generateQrcode(
            userId,
            cardCvv,
            cardExpiry,
            cardNumber,
            cardScheme,
            email,
            fullName,
            issuingBank,
            mobilePhone,
            appCode,
            cardPin,
            object : ApiResponseHandler.Callback<GenerateQrcodeResponse> {
                // Handle successful QR code generation response
                override fun onSuccess(data: GenerateQrcodeResponse?) {
                    callback.success(data)
                    tallyViewModel.transferGeneratedQrData(data)
                }

                // Handle QR code generation error
                override fun onError(errorMessage: String?, statusCode: Int?) {
                    callback.failed(errorMessage)
                }
            }
        )
    }

    /**
     * Stores tokenized card information along with the QR code and token details.
     *
     * This function facilitates the storage of tokenized card details by submitting them to the `tallyViewModel` for processing.
     * Upon submission, it awaits a response indicating whether the storage operation was successful or encountered an error.
     * Based on the response, it triggers appropriate callbacks to handle the outcome.
     *
     * @param cardScheme The card scheme (e.g., Visa, MasterCard) associated with the tokenized card.
     * @param email The email address associated with the tokenized card.
     * @param issuingBank The name of the bank issuing the tokenized card.
     * @param qrCodeId The unique identifier of the QR code linked with the tokenized card.
     * @param qrToken The token associated with the QR code, providing an additional layer of security or identification.
     * @param callback A [TallyResponseCallback] instance to manage the success or failure of the storage operation.
     *
     * Usage Example:
     * ```
     * storeTokenizedCards("Visa", "user@example.com", "Bank Name", "QR12345", "Token123", object : TallyResponseCallback<StoreTokenizedCardsResponse> {
     *     override fun success(data: StoreTokenizedCardsResponse?) {
     *         // Handle successful storage of tokenized card information
     *     }
     *
     *     override fun failed(errorMessage: String?) {
     *         // Handle storage operation error
     *     }
     * })
     * ```
     *
     * Note:
     * - The `tallyViewModel` utilized within this function is assumed to be initialized and provided in the context or scope of where this function is called.
     * - The callback mechanism facilitates asynchronous handling of the storage operation, allowing for a non-blocking user experience.
     *
     * Important:
     * - Ensure that all information provided to this function, especially personal and financial data, is handled in compliance with applicable data protection laws.
     * - Validate inputs to guarantee that they conform to the expected formats and standards before proceeding with the storage operation.
     * - Consider implementing appropriate error handling and feedback mechanisms to inform users of the status of the storage operation.
     */
    fun storeTokenizedCards(
        cardScheme: String,
        email: String,
        issuingBank: String,
        qrCodeId: String,
        qrToken: String,
        callback: TallyResponseCallback<StoreTokenizedCardsResponse>
    ) {
        tallyViewModel.storeTokenizedCards(
            cardScheme,
            email,
            issuingBank,
            qrCodeId,
            qrToken,
            object : ApiResponseHandler.Callback<StoreTokenizedCardsResponse> {
                override fun onSuccess(data: StoreTokenizedCardsResponse?) {
                    callback.success(data = data)
                }

                override fun onError(errorMessage: String?, statusCode: Int?) {
                    callback.failed(message = errorMessage)
                }
            })
    }

    /**
     * Retrieves stored tokenized card information.
     *
     * This function is responsible for fetching the details of tokenized cards that have been previously stored. It makes a request through
     * the `tallyViewModel` to access the stored data. Upon receiving a response, it invokes the appropriate callback based on whether the
     * operation succeeded or failed.
     *
     * @param callback A [TallyResponseCallback] instance to handle the outcome of the fetch operation. It receives a [GetTokenizedCardsResponse]
     * object on success or an error message string on failure.
     *
     * Usage Example:
     * ```
     * getStoredTokenizedCards(object : TallyResponseCallback<GetTokenizedCardsResponse> {
     *     override fun success(data: GetTokenizedCardsResponse?) {
     *         // Handle successful retrieval of tokenized card information.
     *         // 'data' contains the retrieved tokenized card details.
     *     }
     *
     *     override fun failed(errorMessage: String?) {
     *         // Handle errors encountered during the retrieval process.
     *         // 'errorMessage' contains the error message detailing the failure.
     *     }
     * })
     * ```
     *
     * Note:
     * - The `tallyViewModel` used within this function is assumed to be initialized and accessible in the context or scope of where this function is called.
     * - The callback mechanism enables asynchronous handling of the retrieval operation, ensuring a non-blocking user experience.
     *
     * Important:
     * - Ensure that the retrieval and subsequent handling of tokenized card information comply with applicable data protection laws to safeguard user privacy.
     * - Implement error handling and feedback mechanisms to inform users about the status of their request, especially in cases where retrieval fails or encounters issues.
     */
    fun getStoredTokenizedCards(callback: TallyResponseCallback<GetTokenizedCardsResponse>) {
        tallyViewModel.getStoredTokenizedCards(object :
            ApiResponseHandler.Callback<GetTokenizedCardsResponse> {
            override fun onSuccess(data: GetTokenizedCardsResponse?) {
                callback.success(data = data)
            }

            override fun onError(errorMessage: String?, statusCode: Int?) {
                callback.failed(message = errorMessage)
            }
        })
    }

    /**
     * Retrieves transaction records based on a list of QR code IDs.
     *
     * This function initiates a request to fetch transaction details for a specified list of QR code IDs. It leverages the `tallyViewModel`
     * to make an API call, providing parameters such as page number and page size for pagination purposes. The response is then handled
     * through a callback mechanism, allowing for asynchronous processing and updating of UI or data models based on the outcome.
     *
     * @param qr_code_ids A list of QR code IDs for which transactions are to be retrieved.
     * @param page The page number for pagination, indicating the set of transactions to fetch.
     * @param pageSize The number of transaction records per page.
     * @param callback A [TallyResponseCallback] instance to handle the outcome of the transaction retrieval operation. It receives an
     * [UpdatedTransactionResponse] object on success or an error message string on failure.
     *
     * Usage Example:
     * ```
     * getTransactions(listOf("qrCodeId1", "qrCodeId2"), 1, 10, object : TallyResponseCallback<UpdatedTransactionResponse> {
     *     override fun success(data: UpdatedTransactionResponse?) {
     *         // Process the successful retrieval of transaction data.
     *         // 'data' contains the fetched transactions.
     *     }
     *
     *     override fun failed(errorMessage: String?) {
     *         // Handle any errors encountered during the transaction retrieval process.
     *         // 'errorMessage' provides details on why the retrieval failed.
     *     }
     * })
     * ```
     *
     * Note:
     * - Ensure the `tallyViewModel` is properly initialized and capable of executing the transaction retrieval operation.
     * - This function is designed to support pagination, allowing for efficient data fetching and handling in scenarios where the number
     * of transactions is large.
     *
     * Important:
     * - Ensure to implement appropriate error handling and user feedback mechanisms to communicate the status of the transaction retrieval
     * process effectively.
     * - When dealing with financial transaction data, it is critical to adhere to security best practices and compliance requirements
     * to protect user information and maintain trust.
     */
    fun getTransactions(
        qr_code_ids: List<String>,
        page: Int,
        pageSize: Int,
        callback: TallyResponseCallback<UpdatedTransactionResponse>
    ) {
        tallyViewModel.getTransactions(
            qr_code_ids,
            page,
            pageSize,
            object : ApiResponseHandler.Callback<UpdatedTransactionResponse> {
                override fun onSuccess(data: UpdatedTransactionResponse?) {
                    callback.success(data)
                }

                // Handle error when getting transactions
                override fun onError(errorMessage: String?, statusCode: Int?) {
                    callback.failed(errorMessage)
                }
            }
        )
    }

    /**
     * Fetches a list of merchants based on the search criteria.
     *
     * This function queries a list of merchants by sending a request with specified search parameters and pagination options.
     * The operation requires an authentication token and utilizes the `tallyViewModel` for executing the API call. Results are
     * delivered asynchronously through a callback mechanism, which processes the response to either return a list of merchants
     * or handle an error scenario.
     *
     * @param token The authentication token required for API access.
     * @param search The search query string used to filter the list of merchants.
     * @param limit The maximum number of merchant records to return per page.
     * @param page The page number for pagination, indicating the set of merchants to fetch.
     * @param callback A [TallyResponseCallback] instance to handle the outcome of the merchant search operation. It receives a
     * [MerchantResponse] object on success or an error message string on failure.
     *
     * Usage Example:
     * ```
     * getMerchant("yourAuthToken", "searchQuery", 10, 1, object : TallyResponseCallback<MerchantResponse> {
     *     override fun success(data: MerchantResponse?) {
     *         // Process the successful retrieval of merchant data.
     *         // 'data' contains the fetched list of merchants based on the search query.
     *     }
     *
     *     override fun failed(errorMessage: String?) {
     *         // Handle any errors encountered during the merchant retrieval process.
     *         // 'errorMessage' provides details on why the retrieval failed.
     *     }
     * })
     * ```
     *
     * Note:
     * - The authentication token must be valid and have sufficient permissions to access the merchant data.
     * - This function supports searching and pagination, which facilitates efficient data retrieval in applications with
     *   potentially large sets of merchant information.
     *
     * Important:
     * - Implement appropriate error handling and user feedback mechanisms to communicate the status of the merchant retrieval process effectively.
     * - Consider security implications related to the handling and storage of the authentication token and sensitive merchant information.
     */
    fun getMerchant(
        token: String,
        search: String,
        limit: Int,
        page: Int,
        callback: TallyResponseCallback<MerchantResponse>
    ) {
        tallyViewModel.getMerchant(
            token,
            search,
            limit,
            page,
            object : ApiResponseHandler.Callback<MerchantResponse> {
                override fun onSuccess(data: MerchantResponse?) {
                    callback.success(data)
                }

                override fun onError(errorMessage: String?, statusCode: Int?) {
                    callback.failed(errorMessage)
                }
            }
        )
    }

    /**
     * Retrieves a paginated list of all merchants from a specified URL endpoint.
     *
     * This function requests a comprehensive list of merchants, applying pagination parameters to manage the data volume returned in a single request. It requires an authentication token for secure API access and employs a callback mechanism to asynchronously handle the response, facilitating either the successful retrieval of merchant data or error handling.
     *
     * @param url The URL endpoint from which to fetch the merchant data. This should point to the specific API responsible for returning merchant information.
     * @param token The authentication token required to authorize the request. This ensures that only authorized requests can access the merchant data.
     * @param limit The maximum number of merchant records to return per page. This helps in managing the data load and optimizes the response time.
     * @param page The page number to retrieve, which aids in navigating through the paginated list of merchants.
     * @param callback A [TallyResponseCallback] instance that handles the result of the fetch operation. It receives an [AllMerchantResponse] object on success or an error message string on failure.
     *
     * Usage Example:
     * ```
     * getAllMerchants("https://api.example.com/merchants", "yourAuthToken", 20, 1, object : TallyResponseCallback<AllMerchantResponse> {
     *     override fun success(data: AllMerchantResponse?) {
     *         // Process the successful retrieval of all merchant data.
     *         // 'data' contains the fetched list of merchants based on the specified limit and page.
     *     }
     *
     *     override fun failed(errorMessage: String?) {
     *         // Handle any errors encountered during the fetch process.
     *         // 'errorMessage' provides details on why the fetch operation failed.
     *     }
     * })
     * ```
     *
     * Note:
     * - Ensure the provided URL endpoint and authentication token are valid to avoid request failures.
     * - This function is designed to efficiently handle large sets of merchant data through pagination.
     *
     * Important:
     * - Implement suitable error handling and user feedback mechanisms to effectively communicate the status of the merchant fetch operation.
     * - Given the sensitivity of the authentication token and potentially the merchant data, ensure secure handling and storage practices.
     */
    fun getAllMerchants(
        url: String,
        token: String,
        limit: Int,
        page: Int,
        callback: TallyResponseCallback<AllMerchantResponse>
    ) {
        tallyViewModel.getAllMerchant(
            url,
            token,
            limit,
            page,
            object : ApiResponseHandler.Callback<AllMerchantResponse> {
                override fun onSuccess(data: AllMerchantResponse?) {
                    callback.success(data)
                }

                override fun onError(errorMessage: String?, statusCode: Int?) {
                    callback.failed(errorMessage)
                }
            }
        )
    }

    /**
     * Initiates the generation of keys for a financial institution.
     *
     * This function triggers the generation of unique keys necessary for data encryption and decryption in communication with financial institutions. It delegates the task to the ViewModel layer, which handles the interaction with the repository and UI components. The provided callback receives notifications upon successful key generation or in case of any errors encountered during the process.
     *
     * @param url The URL endpoint for the key generation service. This endpoint is responsible for creating the encryption and decryption keys specific to a financial institution.
     * @param token An authentication token to secure the request and ensure it is processed by authorized personnel or systems only.
     * @param financialInstitutionPayload A payload containing necessary details about the financial institution, such as its name, ID, and other relevant information required for key generation.
     * @param callback An instance of [TallyResponseCallback] that handles the response. It processes both the successful generation of keys and any errors encountered during the request.
     *
     * Note:
     * - This function facilitates secure communication with financial institutions by ensuring the proper generation of encryption keys.
     * - It abstracts away the complexities of the key generation process, providing a simple interface for initiating the operation.
     * - The provided callback allows for asynchronous handling of the response, ensuring that the application remains responsive during key generation.
     * - Proper error handling within the callback enables the application to gracefully manage failures and provide appropriate feedback to the user.
     */
    fun storeFinancialInstitutionKeys(
        url: String,
        token: String,
        financialInstitutionPayload: FinancialInstitutionPayload,
        callback: TallyResponseCallback<FinancialInstitutionKeyResponse>
    ) {
        tallyViewModel.storeFinancialInstitutionKeys(
            url,
            token,
            financialInstitutionPayload,
            object : ApiResponseHandler.Callback<FinancialInstitutionKeyResponse> {
                override fun onSuccess(data: FinancialInstitutionKeyResponse?) {
                    callback.success(data)
                }

                override fun onError(errorMessage: String?, statusCode: Int?) {
                    callback.failed(errorMessage)
                }
            }
        )
    }

    /**
     * Initiates a request to retrieve the generated keys for a financial institution through the ViewModel layer.
     *
     * This function utilizes the ViewModel to abstract the process of fetching encryption and decryption keys for a specified financial institution or partner. It demonstrates a clean architecture approach by separating the concerns and utilizing the ViewModel layer for UI-related data handling.
     *
     * @param url The URL endpoint to retrieve the generated keys. This URL points to the specific service capable of returning the encryption keys for the financial institution.
     * @param partnerName The name of the partner or financial institution for which the keys are to be fetched. This identifies the specific set of keys to be retrieved.
     * @param callback An implementation of [TallyResponseCallback] that handles the result of the request. It provides a success callback for when the keys are successfully retrieved and a failure callback for handling any errors.
     *
     * Note:
     * - The function demonstrates how to effectively use a ViewModel for managing data retrieval in a way that is decoupled from the UI logic.
     * - Utilizing callbacks in this manner allows for a flexible response handling mechanism, enabling the UI to update based on the success or failure of the key retrieval operation.
     * - This approach ensures that UI components remain responsive and are updated appropriately based on the asynchronous outcome of the request.
     */
    fun getGenerateFinancialInstitutionKeys(
        url: String,
        partnerName: String,
        callback: TallyResponseCallback<GetFinancialInstitutionKeyResponse>
    ) {
        tallyViewModel.getGenerateFinancialInstitutionKeys(
            url,
            partnerName,
            object : ApiResponseHandler.Callback<GetFinancialInstitutionKeyResponse> {
                override fun onSuccess(data: GetFinancialInstitutionKeyResponse?) {
                    callback.success(data)
                }

                override fun onError(errorMessage: String?, statusCode: Int?) {
                    callback.failed(errorMessage)
                }
            })
    }

    fun cardCheckOut(
        merchantId: String,
        name: String,
        email: String,
        amount: Double,
        currency: String,
        orderId: String,
        callback: TallyResponseCallback<CheckOutResponse>
    ) {
        tallyViewModel.cardCheckOut(
            merchantId,
            name,
            email,
            amount,
            currency,
            orderId,
            object : ApiResponseHandler.Callback<CheckOutResponse> {
                override fun onSuccess(data: CheckOutResponse?) {
                    callback.success(data)
                }

                override fun onError(errorMessage: String?, statusCode: Int?) {
                    callback.failed(errorMessage)
                }
            })
    }

    fun makePayment(payPayload: PayPayload, callback: TallyResponseCallback<PayResponse>) {
        tallyViewModel.makePayment(payPayload, object : ApiResponseHandler.Callback<PayResponse> {
            override fun onSuccess(data: PayResponse?) {
                callback.success(data)
            }

            override fun onError(errorMessage: String?, statusCode: Int?) {
                callback.failed(errorMessage)
            }
        })
    }

    fun makeVerveCardPayment(payload: PayPayload, callback: TallyResponseCallback<JsonObject>) {
        tallyViewModel.makeVerveCardPayment(payload, object  : ApiResponseHandler.Callback<JsonObject> {
            override fun onSuccess(data: JsonObject?) {
                callback.success(data)
            }

            override fun onError(errorMessage: String?, statusCode: Int?) {
                callback.failed(errorMessage)
            }
        })
    }

    fun sendOtpForVerveCard(verveOtpPayload: VerveOtpPayload, callback: TallyResponseCallback<JsonObject>) {
        tallyViewModel.sendOtpForVerveCard(verveOtpPayload, object : ApiResponseHandler.Callback<JsonObject> {
            override fun onSuccess(data: JsonObject?) {
                callback.success(data)
            }

            override fun onError(errorMessage: String?, statusCode: Int?) {
                callback.failed(errorMessage)
            }
        })
    }
}