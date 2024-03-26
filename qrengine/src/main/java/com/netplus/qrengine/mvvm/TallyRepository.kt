package com.netplus.qrengine.mvvm

import com.google.gson.JsonObject
import com.netplus.qrengine.backendRemote.TallyEndpoints
import com.netplus.qrengine.backendRemote.model.card.CheckOutResponse
import com.netplus.qrengine.backendRemote.model.card.PayPayload
import com.netplus.qrengine.backendRemote.model.card.PayResponse
import com.netplus.qrengine.backendRemote.model.keys.FinancialInstitutionKeyResponse
import com.netplus.qrengine.backendRemote.model.keys.FinancialInstitutionPayload
import com.netplus.qrengine.backendRemote.model.keys.get.GetFinancialInstitutionKeyResponse
import com.netplus.qrengine.backendRemote.model.login.LoginPayload
import com.netplus.qrengine.backendRemote.model.login.LoginResponse
import com.netplus.qrengine.backendRemote.model.merchants.AllMerchantResponse
import com.netplus.qrengine.backendRemote.model.merchants.MerchantResponse
import com.netplus.qrengine.backendRemote.model.qr.GenerateQrPayload
import com.netplus.qrengine.backendRemote.model.qr.GenerateQrcodeResponse
import com.netplus.qrengine.backendRemote.model.qr.QrcodeIds
import com.netplus.qrengine.backendRemote.model.qr.retreive.GetTokenizedCardsResponse
import com.netplus.qrengine.backendRemote.model.qr.store.StoreTokenizedCardsPayload
import com.netplus.qrengine.backendRemote.model.qr.store.StoreTokenizedCardsResponse
import com.netplus.qrengine.backendRemote.model.transactions.updatedTransaction.UpdatedTransactionResponse
import com.netplus.qrengine.backendRemote.model.verve.VerveOtpPayload
import com.netplus.qrengine.backendRemote.responseManager.ApiResponseHandler
import com.netplus.qrengine.backendRemote.responseManager.DynamicJsonErrorParser
import com.netplus.qrengine.manualDi.ManualDIHandler
import com.netplus.qrengine.utils.API_KEY
import com.netplus.qrengine.utils.CHECK_OUT_PAY_URL
import com.netplus.qrengine.utils.CHECK_OUT_URL
import com.netplus.qrengine.utils.QR_AUTH_BASE_URL
import com.netplus.qrengine.utils.QR_ENGINE_BASE_URL
import com.netplus.qrengine.utils.TOKEN
import com.netplus.qrengine.utils.TRANSACTIONS_BASE_URL
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Repository class responsible for handling Tally API requests related to authentication and QR code generation.
 *
 * @property tallyEndpoints An instance of the [TallyEndpoints] interface for making API requests.
 */
class TallyRepository(val tallyEndpoints: TallyEndpoints = ManualDIHandler.tallyEndpoints) {

    // Error mapper instance to handle error responses.
    var errorMapper: DynamicJsonErrorParser = DynamicJsonErrorParser()

    /**
     * Initiates a login request to authenticate a user.
     *
     * This function sends a login request to a specified endpoint, using the provided email and password as credentials. It employs a callback mechanism to handle the response asynchronously, ensuring that the consumer of the function can react to successful authentication or handle errors as necessary.
     *
     * @param email The email address of the user attempting to log in.
     * @param password The password associated with the user's account.
     * @param callback An instance of [ApiResponseHandler.Callback] that processes the login response. The callback provides a generic way to handle success or failure of the login operation.
     *
     * Usage Example:
     * ```
     * login("user@example.com", "password123", object : ApiResponseHandler.Callback<LoginResponse> {
     *     override fun onSuccess(data: LoginResponse?) {
     *         // Handle successful login here.
     *         // 'data' contains the login response, typically including user details and authentication token.
     *     }
     *
     *     override fun onError(errorMessage: String?) {
     *         // Handle login failure here.
     *         // 'errorMessage' provides the reason for login failure.
     *     }
     * })
     * ```
     *
     * Note:
     * - It's crucial to validate the email and password before sending the login request to prevent unnecessary network calls and to ensure data integrity.
     * - The actual endpoint (e.g., "login") and payload structure (LoginPayload) might vary depending on the backend API specifications. Ensure these align with your backend implementation.
     *
     * Important:
     * - This function involves transmitting sensitive user information. Ensure that the connection is secure (e.g., HTTPS) to protect the data from interception.
     * - Consider implementing additional security measures such as rate limiting and account lockout policies to mitigate brute force attacks.
     *
     * The function demonstrates how to structure asynchronous network calls within Kotlin, utilizing Retrofit for HTTP requests. The use of an ApiResponseHandler facilitates a cleaner separation of network logic and UI handling, promoting code reusability and maintainability.
     */
    fun login(
        email: String,
        password: String,
        callback: ApiResponseHandler.Callback<LoginResponse>
    ) {
        val apiResponseHandler = ApiResponseHandler<LoginResponse>()
        val loginPayload = LoginPayload(email, password)
        tallyEndpoints.login("$QR_AUTH_BASE_URL/auth/login", loginPayload)
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful) {
                        apiResponseHandler.handleResponse(response.body(), null, response.code(), callback)
                    } else {
                        val error = DynamicJsonErrorParser.parseError(response.errorBody())
                        apiResponseHandler.handleResponse(null, error?.first, response.code(), callback)
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    apiResponseHandler.handleResponse(null, t.message, 500, callback)
                }
            })
    }

    /**
     * Generates a QR code for a user based on provided payment card details and personal information.
     *
     * This function assembles a payload with the user's card and personal details, then sends a request to a designated endpoint for QR code generation. The QR code, upon successful creation, can be used for transactions or other relevant operations. The function employs a callback mechanism to asynchronously handle the response, allowing the caller to react to the outcome of the QR code generation process.
     *
     * @param userId The unique identifier of the user for whom the QR code is being generated.
     * @param cardCvv The Card Verification Value associated with the user's payment card.
     * @param cardExpiry The expiry date of the payment card in a MM/YY format.
     * @param cardNumber The number of the payment card.
     * @param cardScheme The scheme of the payment card (e.g., Visa, MasterCard).
     * @param email The email address of the user.
     * @param fullName The full name of the user.
     * @param issuingBank The bank issuing the user's payment card.
     * @param mobilePhone The mobile phone number of the user.
     * @param appCode A unique code identifying the application requesting the QR code generation.
     * @param cardPin The Personal Identification Number associated with the payment card.
     * @param callback An instance of [ApiResponseHandler.Callback] that processes the QR code generation response, handling success or failure.
     *
     * Usage Example:
     * ```
     * generateQrcode(
     *     userId = 12345,
     *     cardCvv = "123",
     *     cardExpiry = "12/34",
     *     cardNumber = "1234567890123456",
     *     cardScheme = "Visa",
     *     email = "user@example.com",
     *     fullName = "John Doe",
     *     issuingBank = "Bank Name",
     *     mobilePhone = "1234567890",
     *     appCode = "APP123",
     *     cardPin = "1234",
     *     callback = object : ApiResponseHandler.Callback<GenerateQrcodeResponse> {
     *         override fun onSuccess(data: GenerateQrcodeResponse?) {
     *             // Handle successful QR code generation here.
     *         }
     *
     *         override fun onError(errorMessage: String?) {
     *             // Handle QR code generation failure here.
     *         }
     *     }
     * )
     * ```
     *
     * Note:
     * - Ensure that all personal and card details are validated before sending the request to minimize errors and ensure data integrity.
     * - The actual endpoint and payload structure might vary depending on backend API specifications. Make sure these align with your backend implementation.
     *
     * Important:
     * - Given the sensitive nature of the information involved (e.g., card details, personal information), ensure the transmission is secure (e.g., HTTPS) to protect against data breaches.
     * - Adhere to relevant data protection regulations (e.g., GDPR, PCI DSS) when handling payment card information to maintain compliance and user trust.
     *
     * The function showcases a practical example of how to perform secure and effective asynchronous network calls within Kotlin, particularly in scenarios requiring the handling of sensitive user information.
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
        callback: ApiResponseHandler.Callback<GenerateQrcodeResponse>
    ) {
        val apiResponseHandler = ApiResponseHandler<GenerateQrcodeResponse>()
        val generateQrPayload = GenerateQrPayload(
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
            cardPin
        )
        tallyEndpoints.generateQrcode("$QR_AUTH_BASE_URL/qr", TOKEN, generateQrPayload)
            .enqueue(object : Callback<GenerateQrcodeResponse> {
                override fun onResponse(
                    call: Call<GenerateQrcodeResponse>,
                    response: Response<GenerateQrcodeResponse>
                ) {
                    if (response.isSuccessful) {
                        apiResponseHandler.handleResponse(response.body(), null, response.code(), callback)
                    } else {
                        val error = DynamicJsonErrorParser.parseError(response.errorBody())
                        apiResponseHandler.handleResponse(null, error?.first, response.code(), callback)
                    }
                }

                override fun onFailure(call: Call<GenerateQrcodeResponse>, t: Throwable) {
                    apiResponseHandler.handleResponse(null, t.message, 500, callback)
                }
            })
    }

    /**
     * Stores tokenized card information associated with a generated QR code.
     *
     * This function sends a request to store details of a tokenized card including the card scheme, user's email, issuing bank, QR code ID, and QR token. The purpose is to securely record the association between tokenized card data and the corresponding QR code for future transactions or inquiries. Upon completion of the request, the function utilizes a callback mechanism to asynchronously communicate the outcome.
     *
     * @param cardScheme The scheme of the payment card (e.g., Visa, MasterCard).
     * @param email The email address of the user associated with the tokenized card.
     * @param issuingBank The bank issuing the user's tokenized payment card.
     * @param qrCodeId A unique identifier for the QR code associated with the tokenized card.
     * @param qrToken The token representing the stored card details, typically used for transaction authorization.
     * @param callback An instance of [ApiResponseHandler.Callback] that processes the response to the storage operation, handling success or failure scenarios.
     *
     * Usage Example:
     * ```
     * storeTokenizedCards(
     *     cardScheme = "Visa",
     *     email = "user@example.com",
     *     issuingBank = "Bank Name",
     *     qrCodeId = "QR123456789",
     *     qrToken = "TokenValue",
     *     callback = object : ApiResponseHandler.Callback<StoreTokenizedCardsResponse> {
     *         override fun onSuccess(data: StoreTokenizedCardsResponse?) {
     *             // Handle successful storage of tokenized card info here.
     *         }
     *
     *         override fun onError(errorMessage: String?) {
     *             // Handle storage operation failure here.
     *         }
     *     }
     * )
     * ```
     *
     * Note:
     * - Ensure the security and confidentiality of the QR token and card information during transmission and storage.
     * - Validate all inputs to ensure data integrity and adherence to expected formats, particularly for card scheme, email, and bank names.
     *
     * Important:
     * - The implementation should comply with relevant data protection and privacy regulations to safeguard user information.
     * - The API endpoint and payload may need to be adjusted based on the specific backend service architecture and API design.
     *
     * This function illustrates a secure and efficient method to handle sensitive payment information within Kotlin, emphasizing the importance of data protection and error handling in financial applications.
     */
    fun storeTokenizedCards(
        cardScheme: String,
        email: String,
        issuingBank: String,
        qrCodeId: String,
        qrToken: String,
        callback: ApiResponseHandler.Callback<StoreTokenizedCardsResponse>
    ) {
        val storeTokenizedCardsPayload = StoreTokenizedCardsPayload(
            cardScheme, email, issuingBank, qrCodeId, qrToken
        )
        val apiResponseHandler = ApiResponseHandler<StoreTokenizedCardsResponse>()
        tallyEndpoints.storeTokenizedCards(
            "$QR_ENGINE_BASE_URL/storeQrInfo",
            TOKEN, storeTokenizedCardsPayload
        )
            .enqueue(object : Callback<StoreTokenizedCardsResponse> {
                override fun onResponse(
                    call: Call<StoreTokenizedCardsResponse>,
                    response: Response<StoreTokenizedCardsResponse>
                ) {
                    if (response.isSuccessful) {
                        apiResponseHandler.handleResponse(response.body(), null, response.code(), callback)
                    } else {
                        val error = DynamicJsonErrorParser.parseError(response.errorBody())
                        apiResponseHandler.handleResponse(null, error?.first, response.code(), callback)
                    }
                }

                override fun onFailure(call: Call<StoreTokenizedCardsResponse>, t: Throwable) {
                    apiResponseHandler.handleResponse(null, t.message, 500, callback)
                }
            })
    }

    /**
     * Retrieves stored tokenized card information.
     *
     * This function sends a request to retrieve details of all tokenized cards stored for the user. It's designed to fetch a list of tokenized card details which may include card scheme, issuing bank, and associated QR tokens. Upon completion of the request, the function uses a callback mechanism to asynchronously convey the outcome of the retrieval operation.
     *
     * @param callback An instance of [ApiResponseHandler.Callback] that processes the response, handling success or failure scenarios.
     *
     * Usage Example:
     * ```
     * getStoredTokenizedCards(object : ApiResponseHandler.Callback<GetTokenizedCardsResponse> {
     *     override fun onSuccess(data: GetTokenizedCardsResponse?) {
     *         // Handle successful retrieval of tokenized card info here.
     *         data?.tokenizedCards?.forEach { card ->
     *             println("Card Scheme: ${card.cardScheme}, Issuing Bank: ${card.issuingBank}")
     *         }
     *     }
     *
     *     override fun onError(errorMessage: String?) {
     *         // Handle retrieval operation failure here.
     *     }
     * })
     * ```
     *
     * Note:
     * - This function assumes the user is already authenticated and has the necessary permissions to access the stored tokenized card information.
     * - The API endpoint used in `tallyEndpoints.getTokenizedCards()` should be appropriately secured to protect sensitive user data.
     *
     * Important:
     * - Ensure compliance with relevant financial data protection and privacy regulations when storing and retrieving tokenized card information.
     * - Consider implementing caching mechanisms or conditional fetching to optimize network usage and improve the user experience, especially if the data does not change frequently.
     *
     * This function exemplifies the retrieval of sensitive financial information within Kotlin applications, emphasizing the importance of secure data handling, user authentication, and error management in the context of financial technology solutions.
     */
    fun getStoredTokenizedCards(callback: ApiResponseHandler.Callback<GetTokenizedCardsResponse>) {
        val apiResponseHandler = ApiResponseHandler<GetTokenizedCardsResponse>()
        tallyEndpoints.getTokenizedCards().enqueue(object : Callback<GetTokenizedCardsResponse> {
            override fun onResponse(
                call: Call<GetTokenizedCardsResponse>,
                response: Response<GetTokenizedCardsResponse>
            ) {
                if (response.isSuccessful) {
                    apiResponseHandler.handleResponse(response.body(), null, response.code(), callback)
                } else {
                    val error = DynamicJsonErrorParser.parseError(response.errorBody())
                    apiResponseHandler.handleResponse(null, error?.first, response.code(), callback)
                }
            }

            override fun onFailure(call: Call<GetTokenizedCardsResponse>, t: Throwable) {
                apiResponseHandler.handleResponse(null, t.message, 500, callback)
            }
        })
    }

    /**
     * Retrieves transactions based on a list of QR code IDs.
     *
     * This function sends a network request to retrieve transaction details associated with specified QR code IDs. It's intended for scenarios where detailed transaction history for one or more QR codes is required. The function utilizes pagination through 'page' and 'pageSize' parameters to manage the volume of data returned.
     *
     * @param qr_code_id A list of QR code IDs for which transactions are being requested.
     * @param page The page number in the pagination sequence.
     * @param pageSize The number of records to return per page.
     * @param callback An instance of [ApiResponseHandler.Callback] that processes the response, handling success or failure scenarios.
     *
     * Usage Example:
     * ```
     * getTransactions(
     *     qr_code_id = listOf("qrCode1", "qrCode2"),
     *     page = 1,
     *     pageSize = 10,
     *     callback = object : ApiResponseHandler.Callback<UpdatedTransactionResponse> {
     *         override fun onSuccess(data: UpdatedTransactionResponse?) {
     *             // Process the successful retrieval of transactions here.
     *             data?.transactions?.forEach { transaction ->
     *                 println("Transaction ID: ${transaction.id}, Amount: ${transaction.amount}")
     *             }
     *         }
     *
     *         override fun onError(errorMessage: String?) {
     *             // Handle failure in retrieving transactions here.
     *         }
     *     }
     * )
     * ```
     *
     * Note:
     * - Ensure that the QR code IDs provided are valid and that the user has the necessary permissions to view the associated transactions.
     * - The pagination parameters ('page' and 'pageSize') should be used to efficiently manage the volume of transactions and to improve the user experience by loading data as needed.
     *
     * Important:
     * - The security of transaction data is paramount. Ensure that the API endpoints used are secure and that data transmission is encrypted.
     * - Be mindful of the regulatory requirements regarding the handling and display of transaction data to protect user privacy and financial information.
     *
     * This function demonstrates how to fetch and handle transaction data linked to specific QR codes in a Kotlin application, emphasizing pagination, security, and regulatory compliance in financial technology implementations.
     */
    fun getTransactions(
        qr_code_id: List<String>,
        page: Int,
        pageSize: Int,
        callback: ApiResponseHandler.Callback<UpdatedTransactionResponse>
    ) {
        val qr_code_ids = QrcodeIds(qr_code_id)
        val apiResponseHandler = ApiResponseHandler<UpdatedTransactionResponse>()
        tallyEndpoints.getTransactions(
            "$TRANSACTIONS_BASE_URL/multiple-qrcode-transactions/",
            qr_code_ids,
            page,
            pageSize
        )
            .enqueue(object : Callback<UpdatedTransactionResponse> {
                override fun onResponse(
                    call: Call<UpdatedTransactionResponse>,
                    response: Response<UpdatedTransactionResponse>
                ) {
                    if (response.isSuccessful) {
                        apiResponseHandler.handleResponse(response.body(), null, response.code(), callback)
                    } else {
                        val error = DynamicJsonErrorParser.parseError(response.errorBody())
                        apiResponseHandler.handleResponse(null, error?.first, response.code(), callback)
                    }
                }

                override fun onFailure(call: Call<UpdatedTransactionResponse>, t: Throwable) {
                    apiResponseHandler.handleResponse(null, t.message, 500, callback)
                }
            })
    }

    /**
     * Retrieves merchant information based on search criteria.
     *
     * This function sends a network request to retrieve information about merchants based on search criteria such as name, location, or category. It allows users to search for merchants within a specified geographical area or with specific attributes. Pagination is implemented through 'limit' and 'page' parameters to control the amount of data returned.
     *
     * @param token The authentication token for accessing the API.
     * @param search The search query used to filter merchants.
     * @param limit The maximum number of merchant records to return per page.
     * @param page The page number in the pagination sequence.
     * @param callback An instance of [ApiResponseHandler.Callback] that processes the response, handling success or failure scenarios.
     *
     * Usage Example:
     * ```
     * getMerchant(
     *     token = "yourAuthToken",
     *     search = "coffee",
     *     limit = 10,
     *     page = 1,
     *     callback = object : ApiResponseHandler.Callback<MerchantResponse> {
     *         override fun onSuccess(data: MerchantResponse?) {
     *             // Process the successful retrieval of merchant information here.
     *             data?.merchants?.forEach { merchant ->
     *                 println("Merchant Name: ${merchant.name}, Location: ${merchant.location}")
     *             }
     *         }
     *
     *         override fun onError(errorMessage: String?) {
     *             // Handle failure in retrieving merchant information here.
     *         }
     *     }
     * )
     * ```
     *
     * Note:
     * - The 'search' parameter allows for flexible searching based on various criteria, depending on the API's capabilities.
     * - Utilize pagination to manage large sets of merchant data efficiently and to improve application performance.
     *
     * Important:
     * - Ensure that the authentication token ('token') provided is valid and has the necessary permissions to access merchant information.
     * - Consider implementing client-side caching mechanisms to reduce unnecessary API requests and improve responsiveness, especially for frequently accessed merchant data.
     *
     * This function demonstrates how to fetch and handle merchant information based on search criteria in a Kotlin application, emphasizing the importance of search flexibility, pagination, and performance optimization in API interactions.
     */
    fun getMerchant(
        token: String,
        search: String,
        limit: Int,
        page: Int,
        callback: ApiResponseHandler.Callback<MerchantResponse>
    ) {
        val apiResponseHandler = ApiResponseHandler<MerchantResponse>()
        tallyEndpoints.getMerchant(token, search, limit, page)
            .enqueue(object : Callback<MerchantResponse> {
                override fun onResponse(
                    call: Call<MerchantResponse>,
                    response: Response<MerchantResponse>
                ) {
                    if (response.isSuccessful) {
                        apiResponseHandler.handleResponse(response.body(), null, response.code(), callback)
                    } else {
                        val error = DynamicJsonErrorParser.parseError(response.errorBody())
                        apiResponseHandler.handleResponse(null, error?.first, response.code(), callback)
                    }
                }

                override fun onFailure(call: Call<MerchantResponse>, t: Throwable) {
                    apiResponseHandler.handleResponse(null, t.message, 500, callback)
                }
            })
    }

    /**
     * Retrieves a list of all merchants from the specified URL endpoint, incorporating pagination and authentication.
     *
     * This function communicates with a REST API to fetch a comprehensive list of merchants. It leverages HTTP GET requests to obtain merchant data, which is then parsed and returned to the caller through a callback mechanism. The function supports pagination through 'limit' and 'page' parameters and requires an authentication token.
     *
     * @param url The URL endpoint from which to retrieve the merchant data. This URL should point to the API's relevant endpoint for fetching merchant listings.
     * @param token The authentication token required for API access. This token ensures that the request is authorized.
     * @param limit The number of merchant records to retrieve per request/page. This parameter helps in managing large datasets and controlling the payload size.
     * @param page The current page number of the pagination sequence. It allows clients to navigate through pages of merchant data.
     * @param callback An instance of [ApiResponseHandler.Callback] designed to process the API response. It distinguishes between successful data retrieval and error scenarios.
     *
     * Usage:
     * ```
     * getAllMerchant(
     *     url = "https://api.example.com/merchants",
     *     token = "Bearer your_access_token_here",
     *     limit = 20,
     *     page = 1,
     *     callback = object : ApiResponseHandler.Callback<AllMerchantResponse> {
     *         override fun onSuccess(data: AllMerchantResponse?) {
     *             // Handle successful response
     *             data?.merchants?.forEach { merchant ->
     *                 println("Merchant Name: ${merchant.name}")
     *             }
     *         }
     *
     *         override fun onError(errorMessage: String?) {
     *             // Handle error scenario
     *             println("Error fetching merchants: $errorMessage")
     *         }
     *     }
     * )
     * ```
     *
     * Important Considerations:
     * - Ensure the provided 'token' is valid and has sufficient privileges to access the merchant data.
     * - The 'limit' and 'page' parameters are crucial for effective pagination, especially when dealing with large datasets.
     * - Implement error handling to manage potential issues, such as network errors or unauthorized access attempts.
     *
     * By employing this function, applications can dynamically fetch and display merchant information, supporting features like merchant discovery and analysis. Additionally, it demonstrates effective API consumption practices, including authentication, pagination, and asynchronous callbacks.
     */
    fun getAllMerchant(
        url: String,
        token: String,
        limit: Int,
        page: Int,
        callback: ApiResponseHandler.Callback<AllMerchantResponse>
    ) {
        val apiResponseHandler = ApiResponseHandler<AllMerchantResponse>()
        tallyEndpoints.getAllMerchant(url, token, limit, page)
            .enqueue(object : Callback<AllMerchantResponse> {
                override fun onResponse(
                    call: Call<AllMerchantResponse>,
                    response: Response<AllMerchantResponse>
                ) {
                    if (response.isSuccessful) {
                        apiResponseHandler.handleResponse(response.body(), null, response.code(), callback)
                    } else {
                        val error = DynamicJsonErrorParser.parseError(response.errorBody())
                        apiResponseHandler.handleResponse(null, error?.first, response.code(), callback)
                    }
                }

                override fun onFailure(call: Call<AllMerchantResponse>, t: Throwable) {
                    apiResponseHandler.handleResponse(null, t.message, 500, callback)
                }
            })
    }

    /**
     * Initiates the generation of keys for a financial institution.
     *
     * This function facilitates secure communication with financial institutions by generating unique keys necessary for data encryption and decryption. It makes an HTTP request to a specified URL, passing along a payload containing information about the financial institution for which the keys are to be generated. The process is secured through the use of an authentication token, ensuring that only authorized requests are processed.
     *
     * @param url The URL endpoint for the key generation service. This endpoint is responsible for creating the encryption and decryption keys specific to a financial institution.
     * @param token An authentication token to secure the request and ensure it is processed by authorized personnel or systems only.
     * @param financialInstitutionPayload A payload containing necessary details about the financial institution, such as its name, ID, and other relevant information required for key generation.
     * @param callback An instance of [ApiResponseHandler.Callback] that handles the response. It processes both the successful generation of keys and any errors encountered during the request.
     *
     * Usage:
     * ```
     * val financialInstitutionPayload = FinancialInstitutionPayload(
     *     institutionId = "12345",
     *     institutionName = "Example Bank"
     *     // Other necessary details
     * )
     * generateFinancialInstitutionKeys(
     *     url = "https://api.example.com/generateKeys",
     *     token = "your_auth_token",
     *     financialInstitutionPayload = financialInstitutionPayload,
     *     callback = object : ApiResponseHandler.Callback<FinancialInstitutionKeyResponse> {
     *         override fun onSuccess(data: FinancialInstitutionKeyResponse?) {
     *             // Handle successful key generation here
     *             println("Keys generated successfully.")
     *         }
     *
     *         override fun onError(errorMessage: String?) {
     *             // Handle any errors encountered during the request
     *             println("Error generating keys: $errorMessage")
     *         }
     *     }
     * )
     * ```
     *
     * Note:
     * - This function plays a crucial role in ensuring the security and integrity of data exchanged between an application and financial institutions.
     * - The authentication token (`token`) adds an additional layer of security, safeguarding the key generation process against unauthorized access.
     * - The asynchronous handling of the request and response ensures that the application remains responsive, enhancing user experience.
     * - Proper error handling within the callback allows the application to gracefully handle failures, providing clear feedback to the user and maintaining trust.
     * - This approach aligns with best practices in secure application development, particularly in financial services, where data protection is paramount.
     *
     * Through its careful implementation, this function exemplifies the importance of security measures in modern financial applications, facilitating secure and efficient communication with financial institutions.
     */
    fun storeFinancialInstitutionKeys(
        url: String,
        token: String,
        financialInstitutionPayload: FinancialInstitutionPayload,
        callback: ApiResponseHandler.Callback<FinancialInstitutionKeyResponse>
    ) {
        val apiResponseHandler = ApiResponseHandler<FinancialInstitutionKeyResponse>()
        tallyEndpoints.storeFinancialInstitutionKeys(url, token, financialInstitutionPayload)
            .enqueue(object : Callback<FinancialInstitutionKeyResponse> {
                override fun onResponse(
                    call: Call<FinancialInstitutionKeyResponse>,
                    response: Response<FinancialInstitutionKeyResponse>
                ) {
                    if (response.isSuccessful) {
                        apiResponseHandler.handleResponse(response.body(), null, response.code(), callback)
                    } else {
                        val error = DynamicJsonErrorParser.parseError(response.errorBody())
                        apiResponseHandler.handleResponse(null, error?.first, response.code(), callback)
                    }
                }

                override fun onFailure(call: Call<FinancialInstitutionKeyResponse>, t: Throwable) {
                    apiResponseHandler.handleResponse(null, t.message, 500, callback)
                }
            })
    }

    /**
     * Retrieves the generated keys for a financial institution.
     *
     * This function is responsible for fetching the encryption and decryption keys that were previously generated for a financial institution. These keys are essential for secure communication and data exchange with the financial institution. The operation is performed by making a network request to a specified URL endpoint, using the name of the partner or financial institution as a parameter to identify the correct keys to retrieve.
     *
     * @param url The URL endpoint where the generated keys can be retrieved. This endpoint is specific to the service that manages encryption keys for financial institutions.
     * @param partnerName The name of the partner or financial institution for which the keys were generated. This parameter is used to identify and fetch the correct set of keys.
     * @param callback An instance of [ApiResponseHandler.Callback] that handles the response from the network request. It provides mechanisms to process the successfully retrieved keys or handle any errors encountered during the request.
     *
     * Note:
     * - This function is crucial for maintaining the security and integrity of data exchanged with financial institutions by ensuring that the correct encryption keys are always used.
     * - It abstracts the details of the network request and response handling, providing a simple interface for retrieving the necessary keys.
     * - The callback mechanism allows for asynchronous processing of the network response, ensuring that the application remains responsive and can properly manage the flow of execution based on the outcome of the request.
     * - Proper error handling is essential for diagnosing issues with key retrieval and ensuring that the application can respond appropriately to any failures.
     */
    fun getGenerateFinancialInstitutionKeys(
        url: String,
        partnerName: String,
        callback: ApiResponseHandler.Callback<GetFinancialInstitutionKeyResponse>
    ) {
        val apiResponseHandler = ApiResponseHandler<GetFinancialInstitutionKeyResponse>()
        tallyEndpoints.getGenerateFinancialInstitutionKeys(url, API_KEY, partnerName)
            .enqueue(object : Callback<GetFinancialInstitutionKeyResponse> {
                override fun onResponse(
                    call: Call<GetFinancialInstitutionKeyResponse>,
                    response: Response<GetFinancialInstitutionKeyResponse>
                ) {
                    if (response.isSuccessful) {
                        apiResponseHandler.handleResponse(response.body(), null, response.code(), callback)
                    } else {
                        val error = DynamicJsonErrorParser.parseError(response.errorBody())
                        apiResponseHandler.handleResponse(null, error?.first, response.code(), callback)
                    }
                }

                override fun onFailure(
                    call: Call<GetFinancialInstitutionKeyResponse>,
                    t: Throwable
                ) {
                    apiResponseHandler.handleResponse(null, t.message, 500, callback)
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
        callback: ApiResponseHandler.Callback<CheckOutResponse>
    ) {
        val apiResponseHandler = ApiResponseHandler<CheckOutResponse>()
        tallyEndpoints.cardCheckOut(
            CHECK_OUT_URL,
            merchantId,
            name,
            email,
            amount,
            currency,
            orderId
        )
            .enqueue(object : Callback<CheckOutResponse> {
                override fun onResponse(
                    call: Call<CheckOutResponse>,
                    response: Response<CheckOutResponse>
                ) {
                    if (response.isSuccessful) {
                        apiResponseHandler.handleResponse(response.body(), null, response.code(), callback)
                    } else {
                        val error = DynamicJsonErrorParser.parseError(response.errorBody())
                        apiResponseHandler.handleResponse(null, error?.first, response.code(), callback)
                    }
                }

                override fun onFailure(call: Call<CheckOutResponse>, t: Throwable) {
                    apiResponseHandler.handleResponse(null, t.message, 500, callback)
                }
            })
    }

    fun makePayment(payPayload: PayPayload, callback: ApiResponseHandler.Callback<PayResponse>) {
        val apiResponseHandler = ApiResponseHandler<PayResponse>()
        tallyEndpoints.makePayment(CHECK_OUT_PAY_URL, payPayload).enqueue(object : Callback<PayResponse> {
            override fun onResponse(call: Call<PayResponse>, response: Response<PayResponse>) {
                if (response.isSuccessful) {
                    apiResponseHandler.handleResponse(response.body(), null, response.code(), callback)
                } else {
                    val error = DynamicJsonErrorParser.parseError(response.errorBody())
                    apiResponseHandler.handleResponse(null, error?.first, response.code(), callback)
                }
            }

            override fun onFailure(call: Call<PayResponse>, t: Throwable) {
                apiResponseHandler.handleResponse(null, t.message, 500, callback)
            }
        })
    }

    fun makeVerveCardPayment(payload: PayPayload, callback: ApiResponseHandler.Callback<JsonObject>) {
        val apiResponseHandler = ApiResponseHandler<JsonObject>()
        tallyEndpoints.makePaymentForVerveCard(CHECK_OUT_PAY_URL, payload).enqueue(object : Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    apiResponseHandler.handleResponse(response.body(), null, response.code(), callback)
                } else {
                    val error = DynamicJsonErrorParser.parseError(response.errorBody())
                    apiResponseHandler.handleResponse(null, error?.first, response.code(), callback)
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                apiResponseHandler.handleResponse(null, t.message, 500, callback)
            }
        })
    }

    fun sendOtpForVerveCard(verveOtpPayload: VerveOtpPayload, callback: ApiResponseHandler.Callback<JsonObject>) {
        val apiResponseHandler = ApiResponseHandler<JsonObject>()
        tallyEndpoints.sendOTPForVerveCard(CHECK_OUT_PAY_URL, verveOtpPayload).enqueue(object : Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    apiResponseHandler.handleResponse(response.body(), null, response.code(), callback)
                } else {
                    val error = DynamicJsonErrorParser.parseError(response.errorBody())
                    apiResponseHandler.handleResponse(null, error?.first, response.code(), callback)
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                apiResponseHandler.handleResponse(null, t.message, 500, callback)
            }
        })
    }
}