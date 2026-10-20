package com.example.stars

/**
 * A range of timestamps in epoch milliseconds
 *
 * Jetpack Compose datetime pickers output epoch ms, which is why we use this format
 *
 * @param start epoch timestamp of the start of the range, inclusive
 * @param end epoch timestamp of the end of the range, inclusive
 */
data class EpochRange (
    val start: Long,
    val end: Long
)
