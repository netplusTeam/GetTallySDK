package com.netplus.qrengine.backendRemote

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
import com.netplus.qrengine.backendRemote.model.transactions.TransactionResponse
import com.netplus.qrengine.backendRemote.model.transactions.updatedTransaction.UpdatedTransactionResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Interface defining API endpoints for Tally authentication and QR code generation.
 */
interface TallyEndpoints {

    /**
     * Sends a [POST] request to authenticate the user.
     *
     * @param loginPayload The payload containing user credentials.
     * @return A [Call] object wrapping the [LoginResponse].
     */
    @POST
    fun login(@Url url: String, @Body loginPayload: LoginPayload): Call<LoginResponse>

    /**
     * Sends a [POST] request to generate a QR code.
     *
     * @param generateQrPayload The payload containing data for QR code generation.
     * @return A [Call] object wrapping the [GenerateQrcodeResponse].
     */
    @POST
    fun generateQrcode(
        @Url url: String,
        @Header("token") token: String,
        @Body generateQrPayload: GenerateQrPayload
    ): Call<GenerateQrcodeResponse>

    /**
     * Sends a [POST] request to store user tokenized card info
     * @param storeTokenizedCardsPayload
     * @return A [Call] object wrapping the [StoreTokenizedCardsResponse]
     */
    @POST
    fun storeTokenizedCards(
        @Url url: String,
        @Header("token") token: String,
        @Body storeTokenizedCardsPayload: StoreTokenizedCardsPayload
    ): Call<StoreTokenizedCardsResponse>

    /**
     * Sends a [GET] request to retrieve all store qr tokens
     * @return A [Call] object wrapping the [GetTokenizedCardsResponse]
     */
    @GET("fetchQrInfo")
    fun getTokenizedCards(): Call<GetTokenizedCardsResponse>

    /**
     * Sends a [GET] request to get all transactions performed from the tokenized/generated Qrcode
     *
     * @param qr_code_id
     * @param page
     * @param pageSize
     * @return A [Call] object wrapping the [TransactionResponse].
     */
    @POST
    fun getTransactions(
        @Url url: String,
        @Body qr_code_ids: QrcodeIds,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): Call<UpdatedTransactionResponse>

    @GET("qrcode_transactions/{qr_code_id}")
    fun getAllTransaction(
        @Path("qr_code_id") qr_code_id: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): Call<TransactionResponse>

    /**
     * Sends a [GET] request to user type searched merchant
     *
     * @param token
     * @param search
     * @param limit
     * @param page
     */
    @GET("user/search-partner-user")
    fun getMerchant(
        @Header("token") token: String,
        @Query("search") search: String,
        @Query("limit") limit: Int,
        @Query("page") page: Int
    ): Call<MerchantResponse>

    /**
     * Sends [GET] request to get all merchants
     *
     * @param token
     * @param limit
     * @param page
     */
    @GET
    fun getAllMerchant(
        @Url url: String,
        @Header("token") token: String,
        @Query("limit") limit: Int,
        @Query("page") page: Int
    ): Call<AllMerchantResponse>

    /**
     * Send [POST] to create key for specific financial institution
     *
     * @param url
     * @param token
     * @param financialInstitutionPayload
     */
    @POST
    fun storeFinancialInstitutionKeys(
        @Url url: String,
        @Header("token") token: String,
        @Body financialInstitutionPayload: FinancialInstitutionPayload
    ): Call<FinancialInstitutionKeyResponse>

    @GET
    fun getGenerateFinancialInstitutionKeys(
        @Url url: String,
        @Header("token") token: String,
        @Query("partner_name") partnerName: String
    ): Call<GetFinancialInstitutionKeyResponse>
}