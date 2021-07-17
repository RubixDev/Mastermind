import dev.kord.core.entity.User
import kotlinx.serialization.Serializable
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger()

data class BotAuthor(
    val user: User,
    val displayName: String = "${user.username}#${user.discriminator}",
    val avatar: String = user.avatar.url
)

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

        logger.info("Reset data of $username")
    }
}

@Serializable
data class Board(
    var isFinished: Boolean,
    val solution: List<Int>,
    val rows: MutableList<BoardRow>
)

@Serializable
data class BoardRow(
    val gamePins: List<Int>,
    val answerPins: AnswerPins
)

@Serializable
data class AnswerPins(
    val black: Int,
    val white: Int
)
