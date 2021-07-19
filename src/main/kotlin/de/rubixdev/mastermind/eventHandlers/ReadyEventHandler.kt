package de.rubixdev.mastermind.eventHandlers

import de.rubixdev.mastermind.*
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.createApplicationCommands
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.rest.builder.interaction.ApplicationCommandsCreateBuilder
import kotlinx.coroutines.flow.collect
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger()

@KordPreview
suspend fun ReadyEvent.handleReadyEvent() {
    updatePresence()
    logger.info("Bot currently on guilds:${displayGuilds()}")

    addCommands {
        command("show", "Shows current board or starts a new game if previous one is finished") {}
        command("newgame", "Cancels the current game and starts a new one") {}
        command("rules", "Explains the game and commands") {}
        command("invite", "Displays an invite link for this bot") {}
        command("help", "Alias for /rules") {}
        command("config", "Displays a config screen") {}
    }

    isReady = true
}

@KordPreview
private suspend fun ReadyEvent.addCommands(builder: ApplicationCommandsCreateBuilder.() -> Unit) {
    val commands = kord.createGlobalApplicationCommands(builder)
    commands.collect { commandIds[it.name] = it.id }
    addTestCommands(builder)
}

@Suppress("unused")
@KordPreview
private suspend fun ReadyEvent.addTestCommands(builder: ApplicationCommandsCreateBuilder.() -> Unit) {
    val commands = kord.getGuild(Snowflake(661936855167664148))!!.createApplicationCommands(builder)
    commands.collect { testCommandIds[it.name] = it.id }
}
