package com.netplus.qrengine.backendRemote.model.verve

data class VerveOtpPayload(
    val OTPData: String,
    val type: String = "OTP"
)
