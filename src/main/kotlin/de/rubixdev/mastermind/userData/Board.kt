package de.rubixdev.mastermind.userData

import kotlinx.serialization.Serializable

@Serializable
data class Board(
    var isFinished: Boolean,
    val solution: List<Int>,
    val rows: MutableList<BoardRow>
)
