package com.netplus.qrengine.internet.handler

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.MutableLiveData
import com.netplus.qrengine.internet.model.InternetConfigObject

/**
 * @author Anyanwu Nicholas(codeBaron)
 * @since 16-11-2023
 */

/**
 * Singleton class to handle and monitor internet connectivity in the app.
 */

class InternetConnectivityConfig {

    // Companion object for creating a singleton instance of InternetConnectivityConfig.
    companion object {
        lateinit var internetConnectivityConfig: InternetConnectivityConfig
    }

    /**
     * Function to get or create the singleton instance of InternetConnectivityConfig.
     * @return Singleton instance of InternetConnectivityConfig.
     */
    fun networkInstance(): InternetConnectivityConfig {
        internetConnectivityConfig = InternetConnectivityConfig()
        return internetConnectivityConfig
    }

    /**
     * Function to configure and monitor internet connectivity using LiveData.
     * @param context The application context.
     * @return MutableLiveData containing InternetConnectionHandler to observe connectivity changes.
     */
    fun internetConfig(context: Context): MutableLiveData<InternetConnectionHandler<InternetConfigObject>> {
        // LiveData to hold and observe InternetConnectionHandler events.
        val internetConnectionHandler: MutableLiveData<InternetConnectionHandler<InternetConfigObject>> =
            MutableLiveData()

        // Define a network request specifying the required capabilities and transport types.
        val networkRequest: NetworkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        // Define a network callback to handle events related to network availability and changes.
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            // Called when network becomes available.
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                internetConnectionHandler.postValue(
                    InternetConnectionHandler.IsInternetAvailable(
                        true
                    )
                )
            }

            // Called when network capabilities change (e.g., from mobile to Wi-Fi).
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val unMetered =
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                internetConnectionHandler.postValue(
                    InternetConnectionHandler.IsInternetConnectionTypeChanged(
                        true
                    )
                )
            }

            // Called when the network is lost.
            override fun onLost(network: Network) {
                super.onLost(network)
                internetConnectionHandler.postValue(
                    InternetConnectionHandler.IsInternetConnectionLost(
                        true
                    )
                )
            }
        }

        // Get the ConnectivityManager from the application context.
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // Request network updates using the defined network request and callback.
        connectivityManager.requestNetwork(networkRequest, networkCallback)

        // Return the LiveData to observe internet connectivity changes.
        return internetConnectionHandler
    }
}