package com.netplus.qrengine.backendRemote.model.qr

data class EncryptedQrModel(
    val qrcodeId: String?,
    val image: String?,
    val success: Boolean,
    val cardScheme: String?,
    val issuingBank: String?,
    val date: String? = null
)
