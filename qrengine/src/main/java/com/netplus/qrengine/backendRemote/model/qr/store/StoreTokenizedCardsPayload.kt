package com.netplus.qrengine.backendRemote.model.qr.store

data class StoreTokenizedCardsPayload(
    val card_scheme: String,
    val email: String,
    val issuing_bank: String,
    val qr_code_id: String,
    val qr_token: String
)