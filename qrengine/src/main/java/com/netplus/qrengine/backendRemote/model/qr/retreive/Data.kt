package com.netplus.qrengine.backendRemote.model.qr.retreive

data class Data(
    val card_scheme: String,
    val date: String,
    val issuing_bank: String,
    val qr_code_id: String,
    val user_id: String
)