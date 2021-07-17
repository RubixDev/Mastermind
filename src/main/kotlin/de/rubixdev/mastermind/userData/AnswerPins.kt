package de.rubixdev.mastermind.userData

import kotlinx.serialization.Serializable

@Serializable
data class AnswerPins(
    val black: Int,
    val white: Int
)
