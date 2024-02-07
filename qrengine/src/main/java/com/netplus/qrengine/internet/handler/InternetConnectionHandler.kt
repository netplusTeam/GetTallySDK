package com.netplus.qrengine.internet.handler

/**
 * @author Anyanwu Nicholas(codeBaron)
 * @since 15-11-2023
 */

/**
 * Generic sealed class for handling different types of internet connection events.
 * @param R The type of data associated with the internet connection event.
 */
open class InternetConnectionHandler<out R> {

    /**
     * Data class representing an event indicating internet availability.
     * @param T The type of data associated with the availability event.
     * @property isAvailable Boolean indicating whether the internet is available.
     */
    data class IsInternetAvailable<out T>(val isAvailable: Boolean) : InternetConnectionHandler<T>()

    /**
     * Data class representing an event indicating a change in internet connection type.
     * @param T The type of data associated with the connection type change event.
     * @property isChanged Boolean indicating whether the internet connection type has changed.
     */
    data class IsInternetConnectionTypeChanged<out T>(val isChanged: Boolean) :
        InternetConnectionHandler<T>()

    /**
     * Data class representing an event indicating the loss of internet connection.
     * @param T The type of data associated with the connection loss event.
     * @property isLost Boolean indicating whether the internet connection is lost.
     */
    data class IsInternetConnectionLost<out T>(val isLost: Boolean) :
        InternetConnectionHandler<T>()
}
