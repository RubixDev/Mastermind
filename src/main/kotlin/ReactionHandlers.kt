import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.message.ReactionAddEvent

suspend fun ReactionAddEvent.handlePinReaction(botUser: BotUser, pinReactionEmojis: List<ReactionEmoji.Unicode>) {
    if (botUser.nextMove.size == botUser.pins) return
    val newPin = pinReactionEmojis.indexOf(emoji)
    if (!botUser.allowMultiples && newPin in botUser.nextMove) return
    botUser.nextMove.add(newPin)

    updateMessage(getMessage(), botUser)
    logger.info("Added a pin to the next move of ${getUser().username}")
}

suspend fun ReactionAddEvent.handleBackReaction(botUser: BotUser) {
    if (botUser.nextMove.isEmpty()) return
    botUser.nextMove.removeLast()

    updateMessage(getMessage(), botUser)
    logger.info("Removed last pin of the next move of ${getUser().username}")
}

@KordPreview
suspend fun ReactionAddEvent.handleCheckReaction(botUser: BotUser) {
    if (botUser.nextMove.size != botUser.pins) return

    var totalPins = 0
    val matchedPins = mutableListOf<Int>()
    for (pin in botUser.nextMove) {
        if (pin in botUser.board.solution.filterIndexed { i, _ -> i !in matchedPins }) {
            var index = -1
            for ((i, p) in botUser.board.solution.withIndex()) {
                if (p == pin && i !in matchedPins) {
                    index = i
                }
            }
            matchedPins.add(index)
            totalPins++
        }
    }
    val blackPins = botUser.nextMove.countIndexed { i, pin -> pin == botUser.board.solution[i] }
    val whitePins = totalPins - blackPins

    botUser.board.rows.add(
        BoardRow(
            botUser.nextMove.toList(),
            AnswerPins(
                blackPins,
                whitePins
            )
        )
    )
    botUser.nextMove.clear()

    logger.info("Processed move of ${getUser().username}")

    if (blackPins == botUser.pins) {
        channel.createEmbed {
            title = "Congratulations!"
            description = "You found the answer after ${botUser.board.rows.size} turns"
            field {
                name = "The solution was"
                value = botUser.board.solution.map { Constants.pinEmojis[it] }.joinToString("")
            }
            color = Constants.themeColor
            rubixFooter()
        }
        botUser.board.isFinished = true
        botUser.activeMessageId = 0

        saveUserData()
    } else {
        showBoard(getUser(), getGuild(), getChannel(), null)
    }
}
