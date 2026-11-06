package com.example.stars

data class Lottery (
    val id: String = "",
    val ownerID: String = "",
    val campsiteIDs: List<String> = emptyList(),
    val timePeriod: EpochRange = EpochRange(),
    val deadline: Long = (-1).toLong(),
    val numWinners: Int = 0,
    var entrantIDs: List<String> = emptyList(),
    val waitlistCapacity: Int = 0,
    val pendingWinnerIDs: List<String> = emptyList(),
    val confirmedIDs: List<String> = emptyList()
)