package de.rubixdev.mastermind.reactionHandlers

import de.rubixdev.mastermind.Constants
import de.rubixdev.mastermind.updateMessage
import de.rubixdev.mastermind.userData.BotUser
import dev.kord.core.event.message.ReactionAddEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger()

suspend fun ReactionAddEvent.handlePinReaction(botUser: BotUser) {
    if (botUser.nextMove.size == botUser.pins) return
    val newPin = Constants.pinEmojis.indexOf(emoji)
    if (!botUser.allowMultiples && newPin in botUser.nextMove) return
    botUser.nextMove.add(newPin)

    updateMessage(getMessage(), botUser)
    logger.info("Added a pin ($newPin) to the next move of ${getUser().username}")
}