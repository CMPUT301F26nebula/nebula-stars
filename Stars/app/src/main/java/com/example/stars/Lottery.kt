package com.example.stars

data class Lottery (
    val id: String = "",
    val campsiteIDs: List<String> = emptyList(),
    val timePeriod: EpochRange = EpochRange(),
    val deadline: Long = (-1).toLong(),
    val numWinners: Int = 0,
    val entrantIDs: List<String> = emptyList(),
    val confirmedIDs: List<String> = emptyList()
)