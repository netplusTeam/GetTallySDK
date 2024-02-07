package com.netplus.qrengine.utils

import androidx.appcompat.app.AppCompatActivity
import com.netplus.qrengine.backendRemote.model.login.LoginResponse
import com.netplus.qrengine.backendRemote.model.merchants.AllMerchantResponse
import com.netplus.qrengine.backendRemote.model.merchants.MerchantResponse
import com.netplus.qrengine.backendRemote.model.qr.GenerateQrcodeResponse
import com.netplus.qrengine.backendRemote.model.qr.retreive.GetTokenizedCardsResponse
import com.netplus.qrengine.backendRemote.model.qr.store.StoreTokenizedCardsResponse
import com.netplus.qrengine.backendRemote.model.transactions.updatedTransaction.UpdatedTransactionResponse
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
     * Authenticates the bank with the provided email and password.
     *
     * @param email The user's email for authentication.
     * @param password The user's password for authentication.
     * @param context The context used for operations like displaying Toast messages.
     * @param callback The callback for handling the authentication response.
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

                override fun onError(errorMessage: String?) {
                    // Handle login error
                    callback.failed(errorMessage)
                }
            })
    }

    /**
     * Generates a QR code with the provided information.
     *
     * @param context The context used for operations like displaying Toast messages.
     * @param userId The user's ID.
     * @param cardCvv The CVV of the card.
     * @param cardExpiry The expiry date of the card.
     * @param cardNumber The card number.
     * @param cardScheme The card scheme.
     * @param email The user's email.
     * @param fullName The user's full name.
     * @param issuingBank The issuing bank of the card.
     * @param mobilePhone The user's mobile phone number.
     * @param appCode The application code.
     * @param callback The callback for handling the QR code generation response.
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
                override fun onError(errorMessage: String?) {
                    callback.failed(errorMessage)
                }
            }
        )
    }

    /**
     * Stores tokenized qrcode with the provided information
     *
     * @param cardScheme The card type.
     * @param email The user email.
     * @param issuingBank The financial institution.
     * @param qrCodeId The tokenized qr ID.
     * @param qrToken The tokenized token.
     * @param callback The callback for handling the API response.
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

                override fun onError(errorMessage: String?) {
                    callback.failed(message = errorMessage)
                }
            })
    }

    /**
     * Retrieves the stored tokenized qr
     * @param callback
     */
    fun getStoredTokenizedCards(callback: TallyResponseCallback<GetTokenizedCardsResponse>) {
        tallyViewModel.getStoredTokenizedCards(object :
            ApiResponseHandler.Callback<GetTokenizedCardsResponse> {
            override fun onSuccess(data: GetTokenizedCardsResponse?) {
                callback.success(data = data)
            }

            override fun onError(errorMessage: String?) {
                callback.failed(message = errorMessage)
            }
        })
    }

    /**
     * Gets all transactions with the provided information
     *
     * @param qrcodeId The QrcodeId from which transactions will be queried
     * @param page The number of pages to return
     * @param pageSize The size of transactions to be returned in a page
     * @param callback The callback for handling API response
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
                override fun onError(errorMessage: String?) {
                    callback.failed(errorMessage)
                }
            }
        )
    }

    /**
     * Get selected merchant with the provided information
     *
     * @param token The authorization token
     * @param search The name of merchant to search
     * @param limit The limit
     * @param page The number of pages to return
     * @param callback The callback for handling API response
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

                override fun onError(errorMessage: String?) {
                    callback.failed(errorMessage)
                }
            }
        )
    }

    /**
     * Get all merchants
     *
     * @param token The authorization token
     * @param limit The page limit
     * @param page The page number
     * @param callback The callback for handling API response
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

                override fun onError(errorMessage: String?) {
                    callback.failed(errorMessage)
                }
            }
        )
    }
}