package com.netplus.qrengine.backendRemote.model.transactions

data class TransactionsData(
    val count: Int,
    val rows: List<Transaction>
)