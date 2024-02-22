package com.netplus.qrengine.backendRemote.model.card

data class PayResponse(
    val ACSUrl: String,
    val MD: String,
    val PaReq: String,
    val TermUrl: String,
    val amount: String,
    val code: String,
    val eciFlag: String,
    val orderId: String,
    val provider: String,
    val redirectHtml: String,
    val result: String,
    val status: String,
    val transId: String
)