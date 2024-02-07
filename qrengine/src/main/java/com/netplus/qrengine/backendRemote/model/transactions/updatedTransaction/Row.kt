package com.netplus.qrengine.backendRemote.model.transactions.updatedTransaction

data class Row(
    val AID: Any,
    val STAN: Any,
    val TSI: Any,
    val TVR: Any,
    val accountType: Any,
    val acquiringInstCode: Any,
    val additionalAmount: Any,
    val agentName: String,
    val amount: Int,
    val appCryptogram: Any,
    val authCode: Any,
    val cardExpiry: Any,
    val cardHolder: Any,
    val cardLabel: Any,
    val dateCreated: String,
    val localDate: String,
    val localTime_: String,
    val maskedPan: String,
    val merchantId: String,
    val merchantName: String,
    val operatorId: Any,
    val originalForwardingInstCode: Any,
    val otherAmount: Any,
    val otherId: String,
    val partnerId: String,
    val provider: String,
    val remark: String,
    val responseCode: Any,
    val responseDE55: Any,
    val responseMessage: String,
    val rrn: String,
    val source: String,
    val terminalId: String,
    val transactionTime: String,
    val transactionTimeInMillis: Any,
    val transactionType: Any,
    val transmissionDateTime: String,
    val webHookResponseMessage: Any
)