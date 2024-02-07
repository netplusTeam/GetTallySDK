package com.netplus.qrengine.backendRemote.model.login

data class LoginResponse(
    val message: String,
    val refreshToken: String,
    val success: Boolean,
    val token: String
)