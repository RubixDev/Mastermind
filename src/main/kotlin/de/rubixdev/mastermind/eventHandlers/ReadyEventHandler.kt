package de.rubixdev.mastermind.eventHandlers

import de.rubixdev.mastermind.displayGuilds
import de.rubixdev.mastermind.isReady
import de.rubixdev.mastermind.testCommandIds
import de.rubixdev.mastermind.updatePresence
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.createApplicationCommand
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.rest.builder.interaction.ApplicationCommandCreateBuilder
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger()

@KordPreview
suspend fun ReadyEvent.handleReadyEvent() {
    updatePresence()
    logger.info("Bot currently on guilds:${displayGuilds()}")

    addCommand("show", "Shows current board or starts a new game if previous one is finished")
    addCommand("newgame", "Cancels the current game and starts a new one")
    addCommand("rules", "Explains the game and commands")
    addCommand("invite", "Displays an invite link for this bot")
    addCommand("help", "Alias for /rules")
    addCommand("config", "Displays a config screen")
    addCommand("setpins", "Sets the amount of pins you want to play with") {
        int("amount", "Amount of pins. Value between 3 and 6") {
            required = true
        }
    }
    addCommand(
        "allowmultiples",
        "Allows having multiple pins of the same color in the solution. True by default"
    ) {
        boolean("allow", "True to enable, False to disable") {
            required = true
        }
    }

    isReady = true
}

@KordPreview
private suspend fun ReadyEvent.addCommand(
    name: String,
    description: String,
    builder: ApplicationCommandCreateBuilder.() -> Unit = {}
) {
    val cmd = kord.createGlobalApplicationCommand(
        name,
        description,
        builder
    )
    commandIds[name] = cmd.id
    addTestCommand(name, description, builder)
}

@Suppress("unused")
@KordPreview
private suspend fun ReadyEvent.addTestCommand(
    name: String,
    description: String,
    builder: ApplicationCommandCreateBuilder.() -> Unit = {}
) {
    val cmd = kord.getGuild(Snowflake(661936855167664148))!!.createApplicationCommand(
        name,
        description,
        builder
    )
    testCommandIds[name] = cmd.id
}
