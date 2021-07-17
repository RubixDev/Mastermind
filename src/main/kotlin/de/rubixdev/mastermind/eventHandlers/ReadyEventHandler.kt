package de.rubixdev.mastermind.eventHandlers

import de.rubixdev.mastermind.addCommand
import de.rubixdev.mastermind.displayGuilds
import de.rubixdev.mastermind.isReady
import de.rubixdev.mastermind.updatePresence
import dev.kord.common.annotation.KordPreview
import dev.kord.core.event.gateway.ReadyEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger()

@KordPreview
suspend fun ReadyEvent.handleReadyEvent() {
    updatePresence(kord)
    logger.info("Bot currently on guilds:${displayGuilds(kord)}")

    addCommand(
        "show",
        "Shows current board or starts a new game if previous one is finished"
    )
    addCommand(
        "newgame",
        "Cancels the current game and starts a new one"
    )
    addCommand(
        "setpins",
        "Sets the amount of pins you want to play with"
    ) {
        int("amount", "Amount of pins. Value between 3 and 6") {
            required = true
        }
    }
    addCommand(
        "rules",
        "Explains the game and commands"
    )
    addCommand(
        "allowmultiples",
        "Allows having multiple pins of the same color in the solution. True by default"
    ) {
        boolean("allow", "True to enable, False to disable") {
            required = true
        }
    }
    addCommand(
        "invite",
        "Displays an invite link for this bot"
    )
    addCommand(
        "help",
        "Alias for /rules"
    )

    isReady = true
}
