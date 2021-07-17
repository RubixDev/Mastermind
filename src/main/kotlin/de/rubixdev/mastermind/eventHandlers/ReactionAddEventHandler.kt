package de.rubixdev.mastermind.eventHandlers

import de.rubixdev.mastermind.Constants
import de.rubixdev.mastermind.Emojis
import de.rubixdev.mastermind.asReactionEmoji
import de.rubixdev.mastermind.reactionHandlers.handleBackReaction
import de.rubixdev.mastermind.reactionHandlers.handleCheckReaction
import de.rubixdev.mastermind.reactionHandlers.handlePinReaction
import de.rubixdev.mastermind.userData
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Permission
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.message.ReactionAddEvent
import kotlinx.datetime.toJavaInstant
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.time.Instant

private val logger: Logger = LogManager.getLogger()

@KordPreview
suspend fun ReactionAddEvent.handleReactionAddEvent() {
    if (user.id == kord.selfId) return
    if (messageId.value !in userData.map { it.activeMessageId }) return
    if (getMessage().timestamp.toJavaInstant().isBefore(Instant.now().minusSeconds(30 * 60L))) return

    val botUser = userData.find { it.activeMessageId == messageId.value } ?: return
    val channel = message.getChannel() as GuildMessageChannel
    if (channel.getEffectivePermissions(kord.selfId).contains(Permission.ManageMessages)) {
        message.deleteReaction(userId, emoji)
    } else {
        logger.warn("Missing permission to remove reactions")
    }
    if (botUser.id != userId.value) return

    when (emoji) {
        in Constants.pinEmojis -> handlePinReaction(botUser)
        Emojis.back.asReactionEmoji() -> handleBackReaction(botUser)
        Emojis.check.asReactionEmoji() -> handleCheckReaction(botUser)
        else -> return
    }
}
