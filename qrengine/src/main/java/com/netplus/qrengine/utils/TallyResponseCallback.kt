package com.netplus.qrengine.utils

interface TallyResponseCallback<T> {
    fun success(data: T?)
    fun failed(message: String?)
}