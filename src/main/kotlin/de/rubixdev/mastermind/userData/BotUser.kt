package de.rubixdev.mastermind.userData

import de.rubixdev.mastermind.randomBoard
import kotlinx.serialization.Serializable
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Serializable
data class BotUser(
    val id: Long,
    var activeMessageId: Long,
    var pins: Int,
    var allowMultiples: Boolean,
    val nextMove: MutableList<Int>,
    var board: Board,
) {
    fun reset(username: String) {
        activeMessageId = 0
        nextMove.clear()
        board = randomBoard(pins, allowMultiples)

        val logger: Logger = LogManager.getLogger()
        logger.info("Reset data of $username")
    }
}
