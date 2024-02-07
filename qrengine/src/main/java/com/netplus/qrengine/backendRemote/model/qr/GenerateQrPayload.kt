package com.netplus.qrengine.backendRemote.model.qr

data class GenerateQrPayload(
    val user_id: Int? = null,
    val card_cvv: String,
    val card_expiry: String,
    val card_number: String,
    val card_scheme: String,
    val email: String?,
    val fullname: String?,
    val issuing_bank: String,
    val mobile_phone: String?,
    val app_code: String? = "Tally",
    val card_pin: String? = null
)
