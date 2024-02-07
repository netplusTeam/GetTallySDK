package com.netplus.qrengine.backendRemote.responseManager

import java.io.Serializable

data class ErrorModel(
    val message: String? = null,
    val errors: String? = null
) : Serializable