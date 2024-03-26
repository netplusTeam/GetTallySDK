package com.netplus.qrengine.manualDi

import android.app.Application

class TallyApp: Application() {

    override fun onCreate() {
        super.onCreate()
        ManualDIHandler.init(this)
    }
}