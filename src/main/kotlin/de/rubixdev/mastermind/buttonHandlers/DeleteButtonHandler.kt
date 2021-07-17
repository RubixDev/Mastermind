package de.rubixdev.mastermind.buttonHandlers

import de.rubixdev.mastermind.updateMessage
import de.rubixdev.mastermind.userData.BotUser
import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.interaction.ComponentInteraction
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger()

@KordPreview
suspend fun handleDeleteButtonPress(interaction: ComponentInteraction, botUser: BotUser) {
    if (botUser.nextMove.isEmpty()) {
        interaction.acknowledgePublicDeferredMessageUpdate()
        return
    }
    botUser.nextMove.removeLast()

    updateMessage(
        interaction.message ?: run {
            logger.error("Illegal state: Button interaction has no public message associated with it")
            return
        },
        botUser,
        interaction = interaction
    )
    val username = interaction.user.asUser().username
    logger.info("Removed the last pin of $username'${if (username.endsWith('s')) "" else "s"} next move")
}
