package com.netplus.qrengine.manualDi

import android.content.Context
import com.netplus.qrengine.backendRemote.TallyEndpoints
import com.netplus.qrengine.internet.handler.InternetConfigViewModel
import com.netplus.qrengine.mvvm.TallyRepository
import com.netplus.qrengine.mvvm.TallyViewModel
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ManualDIHandler {

    private lateinit var application: Context

    fun init(context: Context) {
        application = context
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().apply {
            readTimeout(30, TimeUnit.SECONDS)
            connectTimeout(30, TimeUnit.SECONDS)
            addInterceptor { chain ->
                val request = chain.request().newBuilder().build()
                chain.proceed(request)
            }
        }.build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://getqr.netpluspay.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val tallyEndpoints: TallyEndpoints by lazy {
        retrofit.create(TallyEndpoints::class.java)
    }

    fun provideTallyRepository(): TallyRepository {
        return TallyRepository(tallyEndpoints)
    }

    fun provideTallyViewModel(): TallyViewModel {
        return TallyViewModel(provideTallyRepository())
    }

    fun provideInternetConfigViewModel(): InternetConfigViewModel {
        return InternetConfigViewModel()
    }
}