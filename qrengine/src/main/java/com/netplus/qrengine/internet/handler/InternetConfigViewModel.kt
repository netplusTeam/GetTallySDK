package com.netplus.qrengine.internet.handler

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.netplus.qrengine.internet.model.InternetConfigObject

/**
 * @author Anyanwu Nicholas(codeBaron)
 * @since @since 15-11-2023
 * @see InternetConnectivityConfig
 * This function handles getting network state result and updating the UI
 */
class InternetConfigViewModel : ViewModel() {

    companion object {
        var internetConnectivityConfig: InternetConnectivityConfig? = null
        var internetConfigObject: MutableLiveData<InternetConnectionHandler<InternetConfigObject>>? =
            null
    }

    fun networkState(context: Context): MutableLiveData<InternetConnectionHandler<InternetConfigObject>>? {
        /*viewModelScope.launch {
            internetConnectivityConfig = InternetConnectivityConfig().networkInstance()
            internetConfigObject = internetConnectivityConfig?.internetConfig(context)
        }*/
        return internetConfigObject
    }
}