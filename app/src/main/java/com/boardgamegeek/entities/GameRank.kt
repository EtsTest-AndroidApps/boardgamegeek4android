package com.boardgamegeek.entities

import com.boardgamegeek.io.BggService

data class GameRank(
        val id: Int = 0,
        val type: String = "",
        val name: String = "",
        val friendlyName: String = "",
        val value: Int = INVALID_RANK,
        val bayesAverage: Double = 0.0
) {
    val isFamilyType: Boolean
        get() = BggService.RANK_TYPE_FAMILY == type

    companion object {
        const val INVALID_RANK = 0
    }
}