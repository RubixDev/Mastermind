package de.rubixdev.mastermind.buttonHandlers

import de.rubixdev.mastermind.commands.updateGameScreen
import de.rubixdev.mastermind.userData.BotUser
import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.ButtonInteraction
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger()

@KordPreview
suspend fun handlePinButtonPress(interaction: ButtonInteraction, botUser: BotUser, pin: Int) {
    if (botUser.nextMove.size == botUser.pins
        || !botUser.allowMultiples && pin in botUser.nextMove
    ) {
        updateGameScreen(interaction, botUser)
        return
    }
    botUser.nextMove.add(pin)

    updateGameScreen(
        interaction.message ?: run {
            logger.error("Illegal state: Button interaction has no public message associated with it")
            return
        },
        botUser,
        interaction = interaction
    )
    // TODO: revert to interaction.user.asUser().username when next kord releases
    logger.info("Added a pin ($pin) to the next move of ${User(interaction.data.user.value!!, interaction.kord).username}")
}
