package com.netplus.qrengine.backendRemote.model.qr

import java.io.Serializable

data class GenerateQrcodeResponse(
    val qr_code_id: String?,
    val data: String?,
    val success: Boolean,
    val card_scheme: String?,
    val issuing_bank: String?,
    val date: String? = null
) : Serializable