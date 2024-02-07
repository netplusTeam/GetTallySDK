package com.netplus.qrengine.koin

import android.app.Application
import com.netplus.coremechanism.mvvm.TallyRepository
import com.netplus.qrengine.backendRemote.TallyEndpoints
import com.netplus.qrengine.internet.handler.InternetConfigViewModel
import com.netplus.qrengine.mvvm.TallyViewModel
import com.netplus.qrengine.utils.TallyAppPreferences
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @author Anyanwu Nicholas(codeBaron)
 * @since 16-11-2023
 */

/**
 * Custom Application class for initializing Koin dependency injection.
 */
class KoinApp : Application() {

    /**
     * Called when the application is starting. Responsible for initializing Koin.
     */
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@KoinApp)
            modules(allModules())
        }
    }

    /**
     * Provides a list of Koin modules used for dependency injection.
     *
     * @return List of Koin modules.
     */
    private fun allModules(): List<Module> {
        val token = TallyAppPreferences.getInstance(this).getStringValue(TallyAppPreferences.TOKEN)
        //Network module providing Retrofit instance for API calls.
        val networkModule = module {
            single {
                val okHttpClient = OkHttpClient.Builder().apply {
                    readTimeout(30, TimeUnit.SECONDS)
                    connectTimeout(30, TimeUnit.SECONDS)
                    addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            /*.addHeader("token", TOKEN)*/.build()
                        chain.proceed(request)
                    }
                }.build()

                // App module providing repositories and view models.
                Retrofit.Builder()
                    .baseUrl("https://getqr.netpluspay.com/")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(TallyEndpoints::class.java)
            }
        }

        val appModule = module {
            single { TallyRepository(get()) }
            viewModel { TallyViewModel(get()) }
            viewModel { InternetConfigViewModel() }
        }

        return listOf(networkModule, appModule)
    }

}