package com.example.stars

import kotlinx.coroutines.channels.Channel

/**
 * Barebones implementation of a condition variable using Kotlin Channels
 */
// hip hip hurrah for cmput379
open class Signaller<T> {
    private val channel = Channel<T>(Channel.UNLIMITED)
    protected var listeners = 0

    /**
     * Suspends the calling coroutine until the broadcast occurs
     *
     * @return value of the broadcast
     */
    suspend fun wait(): T {
        listeners++
        val result = channel.receive()
        listeners--
        return result
    }

    /**
     * Broadcast a value to all listeners
     *
     * @param msg broadcast value
     */
    suspend fun broadcast(msg: T) {
        for (listener in 1..listeners) {
            channel.send(msg)
        }
    }
}