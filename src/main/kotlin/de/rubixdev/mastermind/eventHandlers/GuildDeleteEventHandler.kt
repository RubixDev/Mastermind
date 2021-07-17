package de.rubixdev.mastermind.eventHandlers

import de.rubixdev.mastermind.displayGuilds
import de.rubixdev.mastermind.isReady
import de.rubixdev.mastermind.updatePresence
import dev.kord.core.event.guild.GuildDeleteEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger()

suspend fun GuildDeleteEvent.handleGuildDeleteEvent() {
    if (isReady) {
        updatePresence(kord)
        logger.info("Left guild '${guild?.name}'. Now on guilds:${displayGuilds(kord)}")
    }
}
