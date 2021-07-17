package de.rubixdev.mastermind.userData

import kotlinx.serialization.Serializable

@Serializable
data class BoardRow(
    val gamePins: List<Int>,
    val answerPins: AnswerPins
)
