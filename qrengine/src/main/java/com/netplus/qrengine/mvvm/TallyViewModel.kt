package com.netplus.qrengine.mvvm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
import com.netplus.qrengine.backendRemote.responseManager.ApiResponseHandler

/**
 * ViewModel class for handling Tally-related operations, such as user login and QR code generation.
 *
 * @property tallyRepository An instance of [TallyRepository] for interacting with Tally API.
 */
class TallyViewModel(private val tallyRepository: TallyRepository) : ViewModel() {

    val recentGeneratedQrLiveData = MutableLiveData<GenerateQrcodeResponse>()

    /**
     * Initiates the login process for a user, triggering authentication with the provided credentials.
     *
     * This function acts as a bridge between the user interface and the authentication logic. It takes user credentials (email and password) as input parameters and utilizes the [tallyRepository] to execute the login operation. Upon completion, it invokes the provided callback to handle the login response, which includes success and failure scenarios.
     *
     * @param email The user's email address used for authentication.
     * @param password The user's password associated with the email address for authentication.
     * @param callback An instance of [ApiResponseHandler.Callback] to handle the login response, distinguishing between success and failure scenarios.
     *
     * Usage:
     * ```
     * login(
     *     email = "user@example.com",
     *     password = "password123",
     *     callback = object : ApiResponseHandler.Callback<LoginResponse> {
     *         override fun onSuccess(data: LoginResponse?) {
     *             // Handle successful login
     *             println("Login successful!")
     *         }
     *
     *         override fun onError(errorMessage: String?) {
     *             // Handle login error
     *             println("Login failed: $errorMessage")
     *         }
     *     }
     * )
     * ```
     *
     * Note:
     * - This function delegates the login operation to the [tallyRepository], which encapsulates the data access and manipulation logic.
     * - Ensure that the provided email and password are valid and conform to the authentication requirements.
     * - The callback allows for asynchronous handling of the login response, enabling the UI to remain responsive during the authentication process.
     *
     * By utilizing this function, applications can facilitate user authentication securely and seamlessly, integrating it with the overall login workflow.
     */
    fun login(
        email: String,
        password: String,
        callback: ApiResponseHandler.Callback<LoginResponse>
    ) = tallyRepository.login(email, password, callback)

    /**
     * Initiates the process to generate a QR code based on the user's payment card details.
     *
     * This function simplifies the interaction between the user interface and the underlying logic for QR code generation. It collects necessary information from the user, such as card details and personal information, and uses the [tallyRepository] to initiate the QR code generation process. The provided callback handles the response, which may indicate success or failure.
     *
     * @param userId The unique identifier of the user requesting the QR code.
     * @param cardCvv The Card Verification Value associated with the user's payment card.
     * @param cardExpiry The expiration date of the user's payment card, typically in MM/YY format.
     * @param cardNumber The number of the user's payment card.
     * @param cardScheme The payment card scheme (e.g., Visa, MasterCard).
     * @param email The email address of the user.
     * @param fullName The full name of the user as it appears on the payment card.
     * @param issuingBank The bank that issued the user's payment card.
     * @param mobilePhone The mobile phone number of the user.
     * @param appCode A specific code related to the application requesting the QR code generation.
     * @param cardPin The Personal Identification Number associated with the user's payment card.
     * @param callback An instance of [ApiResponseHandler.Callback] to handle the response of the QR code generation process.
     *
     * Usage:
     * ```
     * generateQrcode(
     *     userId = 12345,
     *     cardCvv = "123",
     *     cardExpiry = "12/34",
     *     cardNumber = "1234567890123456",
     *     cardScheme = "Visa",
     *     email = "user@example.com",
     *     fullName = "John Doe",
     *     issuingBank = "Bank of Example",
     *     mobilePhone = "1234567890",
     *     appCode = "APP123",
     *     cardPin = "1234",
     *     callback = object : ApiResponseHandler.Callback<GenerateQrcodeResponse> {
     *         override fun onSuccess(data: GenerateQrcodeResponse?) {
     *             // Handle successful QR code generation
     *             println("QR code generated successfully!")
     *         }
     *
     *         override fun onError(errorMessage: String?) {
     *             // Handle QR code generation error
     *             println("QR code generation failed: $errorMessage")
     *         }
     *     }
     * )
     * ```
     *
     * Note:
     * - This function delegates the QR code generation task to the [tallyRepository], which encapsulates the communication with the backend service.
     * - It's crucial to validate the provided information before attempting to generate a QR code to ensure the process's success.
     * - The callback facilitates asynchronous handling of the QR code generation response, allowing for a responsive and user-friendly application experience.
     *
     * By utilizing this function, applications can provide users with a secure and efficient way to generate QR codes for transactions, enhancing the overall payment experience.
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
    ) = tallyRepository.generateQrcode(
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
        callback
    )

    /**
     * Initiates the process to store tokenized card information associated with a specific QR code.
     *
     * This function streamlines the procedure for storing details of tokenized payment cards linked to QR codes generated for transactions. It gathers necessary card-related information and identifiers, leveraging the [tallyRepository] to securely store these details. The response to this operation, indicating success or failure, is managed through the provided callback.
     *
     * @param cardScheme The payment card scheme (e.g., Visa, MasterCard) of the stored card.
     * @param email The email address associated with the user who owns the card.
     * @param issuingBank The bank that issued the tokenized card.
     * @param qrCodeId The unique identifier of the QR code linked to the tokenized card.
     * @param qrToken The token representing the stored card information, typically used for transaction authorization.
     * @param callback An instance of [ApiResponseHandler.Callback] that handles the response from the card storage process.
     *
     * Usage:
     * ```
     * storeTokenizedCards(
     *     cardScheme = "Visa",
     *     email = "user@example.com",
     *     issuingBank = "Bank of Example",
     *     qrCodeId = "QR12345",
     *     qrToken = "Token12345",
     *     callback = object : ApiResponseHandler.Callback<StoreTokenizedCardsResponse> {
     *         override fun onSuccess(data: StoreTokenizedCardsResponse?) {
     *             // Handle successful storage of tokenized card information
     *             println("Tokenized card information stored successfully!")
     *         }
     *
     *         override fun onError(errorMessage: String?) {
     *             // Handle storage error
     *             println("Failed to store tokenized card information: $errorMessage")
     *         }
     *     }
     * )
     * ```
     *
     * Note:
     * - This function is crucial for maintaining a secure record of card details associated with specific transactions, enhancing both security and convenience for future transactions.
     * - It delegates the storage operation to the [tallyRepository], ensuring that the interaction with backend services is encapsulated and secure.
     * - Proper validation of the input parameters before initiating the storage process is essential to ensure the accuracy and security of the stored data.
     * - The asynchronous nature of the callback allows for a non-blocking UI experience, making the application more responsive and user-friendly.
     *
     * By using this function, applications can offer users a seamless way to store and manage their tokenized card information, further simplifying the payment process and enhancing user experience.
     */
    fun storeTokenizedCards(
        cardScheme: String,
        email: String,
        issuingBank: String,
        qrCodeId: String,
        qrToken: String,
        callback: ApiResponseHandler.Callback<StoreTokenizedCardsResponse>
    ) = tallyRepository.storeTokenizedCards(
        cardScheme,
        email,
        issuingBank,
        qrCodeId,
        qrToken,
        callback
    )

    /**
     * Retrieves the stored tokenized card information.
     *
     * This function is designed to fetch the list of tokenized cards that have been previously stored, allowing users to access and manage their stored payment methods. The operation is executed through the [tallyRepository], ensuring secure and efficient access to the stored data. The result of this retrieval process is communicated back via the provided callback.
     *
     * @param callback An instance of [ApiResponseHandler.Callback] that handles the response, containing the list of stored tokenized cards or an error message.
     *
     * Usage:
     * ```
     * getStoredTokenizedCards(
     *     callback = object : ApiResponseHandler.Callback<GetTokenizedCardsResponse> {
     *         override fun onSuccess(data: GetTokenizedCardsResponse?) {
     *             // Handle successful retrieval of stored tokenized cards
     *             println("Successfully retrieved stored tokenized cards.")
     *         }
     *
     *         override fun onError(errorMessage: String?) {
     *             // Handle error in retrieving stored tokenized cards
     *             println("Failed to retrieve stored tokenized cards: $errorMessage")
     *         }
     *     }
     * )
     * ```
     *
     * Note:
     * - This function plays a critical role in enabling users to view and manage their payment options, contributing to a more convenient and user-friendly payment experience.
     * - It leverages the [tallyRepository] for data access, encapsulating the complexities of data retrieval and ensuring that the operation adheres to security best practices.
     * - The use of a callback for handling the asynchronous response allows the UI to remain responsive, enhancing the overall user experience by preventing blocking operations.
     * - Implementing proper error handling within the callback is crucial for providing meaningful feedback to the user and ensuring a smooth user interface flow.
     *
     * By utilizing this function, applications can offer users an efficient way to access and manage their stored payment information, streamlining the payment process and enhancing the overall user experience.
     */
    fun getStoredTokenizedCards(callback: ApiResponseHandler.Callback<GetTokenizedCardsResponse>) =
        tallyRepository.getStoredTokenizedCards(callback)

    /**
     * Fetches a list of transactions based on QR code identifiers.
     *
     * This function is responsible for retrieving transaction records associated with specific QR codes. It leverages the [tallyRepository] to access the transaction data, providing a paged list of transactions to support scalability and efficient data handling. The operation's outcome is communicated back to the caller via the provided callback, ensuring that the application can react accordingly to success or failure scenarios.
     *
     * @param qr_code_ids A list of QR code identifiers used to filter transactions.
     * @param page The page number of the transaction list to retrieve, supporting pagination.
     * @param pageSize The number of transactions to return per page, controlling the data volume.
     * @param callback An instance of [ApiResponseHandler.Callback] that handles the response, containing the paged list of transactions or an error message.
     *
     * Usage:
     * ```
     * getTransactions(
     *     qr_code_ids = listOf("qrCode1", "qrCode2"),
     *     page = 1,
     *     pageSize = 10,
     *     callback = object : ApiResponseHandler.Callback<UpdatedTransactionResponse> {
     *         override fun onSuccess(data: UpdatedTransactionResponse?) {
     *             // Handle successful retrieval of transactions
     *             println("Successfully retrieved transactions.")
     *         }
     *
     *         override fun onError(errorMessage: String?) {
     *             // Handle error in retrieving transactions
     *             println("Failed to retrieve transactions: $errorMessage")
     *         }
     *     }
     * )
     * ```
     *
     * Note:
     * - This function is crucial for applications that need to display transaction history to users, allowing for a detailed view of their activities based on QR code interactions.
     * - Pagination parameters (`page` and `pageSize`) are essential for handling large datasets efficiently, reducing the load on the network and improving the application's responsiveness.
     * - The asynchronous nature of the callback mechanism ensures that the UI remains interactive while the transaction data is being fetched, enhancing the user experience.
     * - Proper error handling within the callback allows the application to provide informative feedback to the user, ensuring clarity and maintaining trust.
     *
     * By utilizing this function, applications can offer users insight into their transaction history in a user-friendly manner, promoting transparency and engagement through detailed activity records.
     */
    fun getTransactions(
        qr_code_ids: List<String>,
        page: Int,
        pageSize: Int,
        callback: ApiResponseHandler.Callback<UpdatedTransactionResponse>
    ) = tallyRepository.getTransactions(qr_code_ids, page, pageSize, callback)


    /**
     * Searches for merchants based on a query string.
     *
     * This function interacts with the repository layer to search for merchants using various criteria such as name or identifier. The search operation is paginated, allowing for efficient data retrieval and handling. The outcome, whether it be a list of merchants matching the search criteria or an error, is communicated back to the caller through a callback mechanism.
     *
     * @param token Authentication token required for accessing the merchant search API.
     * @param search The query string used for searching merchants. This could include partial or full merchant names or other relevant identifiers.
     * @param limit The maximum number of merchant records to return per page. This helps in managing the volume of data returned by the search operation.
     * @param page The page number in the pagination sequence, allowing the caller to retrieve different segments of the search results.
     * @param callback An instance of [ApiResponseHandler.Callback] that processes the search result, encapsulating either the merchant data or an error message.
     *
     * Usage:
     * ```
     * getMerchant(
     *     token = "your_auth_token",
     *     search = "merchant_query",
     *     limit = 10,
     *     page = 1,
     *     callback = object : ApiResponseHandler.Callback<MerchantResponse> {
     *         override fun onSuccess(data: MerchantResponse?) {
     *             // Process the successful retrieval of merchant data
     *             println("Merchants retrieved successfully.")
     *         }
     *
     *         override fun onError(errorMessage: String?) {
     *             // Handle errors encountered during the merchant search
     *             println("Failed to retrieve merchants: $errorMessage")
     *         }
     *     }
     * )
     * ```
     *
     * Note:
     * - This function is crucial for applications requiring dynamic merchant search capabilities, such as payment platforms, e-commerce solutions, and business directories.
     * - Pagination parameters (`limit` and `page`) ensure scalable and efficient data handling, especially important when dealing with potentially large sets of merchant information.
     * - The use of an authentication token (`token`) emphasizes the operation's security, ensuring that only authorized users can access sensitive merchant data.
     * - By employing a callback mechanism, the function supports asynchronous data retrieval, thus maintaining a responsive user interface and enhancing overall user experience.
     * - Proper error handling within the callback allows the application to provide clear and informative feedback to users, fostering a trustworthy user environment.
     *
     * This function is an essential component of any application requiring access to merchant data, offering flexibility, efficiency, and security in data retrieval operations.
     */
    fun getMerchant(
        token: String,
        search: String,
        limit: Int,
        page: Int,
        callback: ApiResponseHandler.Callback<MerchantResponse>
    ) = tallyRepository.getMerchant(token, search, limit, page, callback)


    /**
     * Retrieves all merchants from a specified URL endpoint with pagination support.
     *
     * This function interfaces with the repository layer to fetch a comprehensive list of merchants from a given URL. It supports pagination to efficiently manage and navigate through large datasets. The function ensures that data retrieval is secure by requiring an authentication token. The results are delivered asynchronously, providing a smooth user experience by not blocking the main thread.
     *
     * @param url The URL endpoint from which to retrieve the merchant data. This URL points to the specific API responsible for returning the merchant list.
     * @param token An authentication token to ensure that the request is authorized. This adds a layer of security by restricting access to authenticated users.
     * @param limit The number of merchant records to return in a single response. This parameter is essential for pagination and helps in managing data load.
     * @param page The current page number in the pagination sequence. It allows the user to navigate through different sets of merchant data.
     * @param callback An instance of [ApiResponseHandler.Callback] that handles the response. It processes both successful data retrieval and error scenarios.
     *
     * Usage:
     * ```
     * getAllMerchant(
     *     url = "https://api.example.com/merchants",
     *     token = "your_auth_token",
     *     limit = 20,
     *     page = 1,
     *     callback = object : ApiResponseHandler.Callback<AllMerchantResponse> {
     *         override fun onSuccess(data: AllMerchantResponse?) {
     *             // Handle successful data retrieval here
     *             println("Successfully retrieved all merchants.")
     *         }
     *
     *         override fun onError(errorMessage: String?) {
     *             // Handle any errors encountered during the request
     *             println("Error fetching merchants: $errorMessage")
     *         }
     *     }
     * )
     * ```
     *
     * Note:
     * - This function is vital for applications that need to display a comprehensive list of merchants, such as marketplaces, financial applications, and business analytics tools.
     * - The use of pagination through `limit` and `page` parameters ensures that the application can scale by handling large volumes of data efficiently.
     * - Securing the request with an authentication token (`token`) protects sensitive data and ensures that only authorized users can access the merchant list.
     * - Asynchronous handling of responses through the callback mechanism ensures that the application remains responsive, enhancing the user experience.
     * - Proper error handling within the callback allows for graceful degradation of the application's functionality in case of errors, providing clear feedback to the user.
     *
     * This function highlights the importance of efficient data management, security, and user experience in modern application development, especially when dealing with extensive datasets and sensitive information.
     */
    fun getAllMerchant(
        url: String,
        token: String,
        limit: Int,
        page: Int,
        callback: ApiResponseHandler.Callback<AllMerchantResponse>
    ) = tallyRepository.getAllMerchant(url, token, limit, page, callback)

    /**
     * Initiates the generation of keys for a financial institution.
     *
     * This function triggers the generation of unique keys necessary for data encryption and decryption in communication with financial institutions. It delegates the task to the repository layer, which handles the interaction with external APIs or data sources. The provided callback receives notifications upon successful key generation or in case of any errors encountered during the process.
     *
     * @param url The URL endpoint for the key generation service. This endpoint is responsible for creating the encryption and decryption keys specific to a financial institution.
     * @param token An authentication token to secure the request and ensure it is processed by authorized personnel or systems only.
     * @param financialInstitutionPayload A payload containing necessary details about the financial institution, such as its name, ID, and other relevant information required for key generation.
     * @param callback An instance of [ApiResponseHandler.Callback] that handles the response. It processes both the successful generation of keys and any errors encountered during the request.
     *
     * Note:
     * - This function facilitates secure communication with financial institutions by ensuring the proper generation of encryption keys.
     * - It abstracts away the complexities of the key generation process, providing a simple interface for initiating the operation.
     * - The provided callback allows for asynchronous handling of the response, ensuring that the application remains responsive during key generation.
     * - Proper error handling within the callback enables the application to gracefully manage failures and provide appropriate feedback to the user.
     */
    fun generateFinancialInstitutionKeys(
        url: String,
        token: String,
        financialInstitutionPayload: FinancialInstitutionPayload,
        callback: ApiResponseHandler.Callback<FinancialInstitutionKeyResponse>
    ) = tallyRepository.generateFinancialInstitutionKeys(url, token, financialInstitutionPayload, callback)

    /**
     * Retrieves the generated keys for a financial institution.
     *
     * This function delegates the task of fetching the encryption and decryption keys for a financial institution to the repository layer. It takes in the URL endpoint and the name of the partner or financial institution as parameters to identify and fetch the correct set of keys. The operation is performed asynchronously, and the result is delivered via the provided callback.
     *
     * @param url The URL endpoint where the generated keys can be retrieved. This endpoint is specific to the service that manages encryption keys for financial institutions.
     * @param partnerName The name of the partner or financial institution for which the keys were generated. This parameter is used to identify and fetch the correct set of keys.
     * @param callback An instance of [ApiResponseHandler.Callback] that handles the response from the repository layer. It provides mechanisms to process the successfully retrieved keys or handle any errors encountered during the request.
     *
     * Note:
     * - This function abstracts the details of network communication and response handling, providing a simple interface for fetching the necessary keys.
     * - Proper error handling ensures that the application can respond appropriately to any failures encountered during the key retrieval process.
     * - The use of a callback allows for asynchronous processing of the network response, ensuring that the application remains responsive and can properly manage the flow of execution based on the outcome of the request.
     */
    fun getGenerateFinancialInstitutionKeys(
        url: String,
        partnerName: String,
        callback: ApiResponseHandler.Callback<GetFinancialInstitutionKeyResponse>
    ) = tallyRepository.getGenerateFinancialInstitutionKeys(url, partnerName, callback)

    fun transferGeneratedQrData(generateQrcodeResponse: GenerateQrcodeResponse?) {
        recentGeneratedQrLiveData.value = generateQrcodeResponse
    }

    fun receiveTransferredGeneratedQrData() = recentGeneratedQrLiveData
}