package de.rubixdev.mastermind.commands

import de.rubixdev.mastermind.Constants
import de.rubixdev.mastermind.rubixFooter
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.interaction.embed
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger()

@KordPreview
suspend fun InteractionCreateEvent.inviteCommand() {
    interaction.respondPublic {
        embed {
            title = "Invite me using this link:"
            description = Constants.inviteLink
            color = Constants.successColor
            rubixFooter()
        }
    }
    logger.info("Sent invitation link as response to ${interaction.user.asUser().username}")
}
