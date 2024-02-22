package com.netplus.qrengine.backendRemote.model.card

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WebResponse(
    val amount: String,
    val code: String,
    val currency_code: String,
    val customerName: String,
    val email: String,
    val message: String,
    val narration: String,
    val orderId: String,
    val result: String,
    val status: String,
    val transId: String
): Parcelable
