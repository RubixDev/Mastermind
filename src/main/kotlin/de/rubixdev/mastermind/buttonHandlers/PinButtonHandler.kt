package de.rubixdev.mastermind.buttonHandlers

import de.rubixdev.mastermind.updateMessage
import de.rubixdev.mastermind.userData.BotUser
import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.interaction.ComponentInteraction
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger()

@KordPreview
suspend fun handlePinButtonPress(interaction: ComponentInteraction, botUser: BotUser, pin: Int) {
    if (botUser.nextMove.size == botUser.pins
        || !botUser.allowMultiples && pin in botUser.nextMove
    ) {
        interaction.acknowledgePublicDeferredMessageUpdate()
        return
    }
    botUser.nextMove.add(pin)

    updateMessage(
        interaction.message ?: run {
            logger.error("Illegal state: Button interaction has no public message associated with it")
            return
        },
        botUser,
        interaction = interaction
    )
    logger.info("Added a pin ($pin) to the next move of ${interaction.user.asUser().username}")
}