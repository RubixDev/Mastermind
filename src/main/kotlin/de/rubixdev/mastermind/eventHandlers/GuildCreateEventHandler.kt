package de.rubixdev.mastermind.eventHandlers

import de.rubixdev.mastermind.displayGuilds
import de.rubixdev.mastermind.isReady
import de.rubixdev.mastermind.updatePresence
import dev.kord.core.event.guild.GuildCreateEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger()

suspend fun GuildCreateEvent.handleGuildCreateEvent() {
    if (isReady) {
        updatePresence()
        logger.info("Joined guild '${guild.name}'. Now on guilds:${displayGuilds()}")
    }
}
