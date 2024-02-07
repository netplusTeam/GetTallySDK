package com.netplus.qrengine.backendRemote.model.merchants

data class AllMerchantResponse(
    val data: List<Merchant>,
    val status: Boolean
)