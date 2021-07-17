package de.rubixdev.mastermind.reactionHandlers

import de.rubixdev.mastermind.updateMessage
import de.rubixdev.mastermind.userData.BotUser
import dev.kord.core.event.message.ReactionAddEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger()

suspend fun ReactionAddEvent.handleBackReaction(botUser: BotUser) {
    if (botUser.nextMove.isEmpty()) return
    botUser.nextMove.removeLast()

    updateMessage(getMessage(), botUser)
    logger.info("Removed the last pin of the next move of ${getUser().username}")
}