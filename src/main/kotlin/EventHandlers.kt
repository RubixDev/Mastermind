import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ChannelType
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.CommandInteraction
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.guild.GuildDeleteEvent
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.rest.builder.interaction.embed
import kotlinx.datetime.toJavaInstant
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.time.Instant

private val logger: Logger = LogManager.getLogger("EventHandlers")

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

suspend fun GuildCreateEvent.handleGuildCreateEvent() {
    if (isReady) {
        updatePresence(kord)
        logger.info("Joined guild '${guild.name}'. Now on guilds:${displayGuilds(kord)}")
    }
}

suspend fun GuildDeleteEvent.handleGuildDeleteEvent() {
    if (isReady) {
        updatePresence(kord)
        logger.info("Left guild '${guild?.name}'. Now on guilds:${displayGuilds(kord)}")
    }
}

@KordPreview
suspend fun InteractionCreateEvent.handleInteractionCreateEvent() {
    if (interaction.getChannel().type != ChannelType.GuildText) {
        interaction.respondPublic {
            embed {
                title = "Forbidden"
                description = "This bot does not work in this channel. " +
                        "Please use it in a normal server text channel.\n" +
                        "Use this link to invite me to your server:\n" +
                        "https://discord.com/api/oauth2/authorize?client_id=830490572765790220&permissions=10304" +
                        "&scope=bot%20applications.commands"
                color = Constants.errorColor
                rubixFooter()
            }
        }
        return
    }

    when ((interaction as CommandInteraction).command.rootId) {
        testCommandIds["show"],
        commandIds["show"] -> showCommand()
        testCommandIds["newgame"],
        commandIds["newgame"] -> newGameCommand()
        testCommandIds["setpins"],
        commandIds["setpins"] -> setPinsCommand()
        testCommandIds["rules"],
        commandIds["rules"],
        testCommandIds["help"],
        commandIds["help"] -> rulesCommand()
        testCommandIds["allowmultiples"],
        commandIds["allowmultiples"] -> allowMultiplesCommand()
        testCommandIds["invite"],
        commandIds["invite"] -> inviteCommand()
    }
}

@KordPreview
suspend fun ReactionAddEvent.handleReactionAddEvent() {
    if (user.id == kord.selfId) return
    if (messageId.value !in userData.map { it.activeMessageId }) return
    if (getMessage().timestamp.toJavaInstant().isBefore(Instant.now().minusSeconds(30 * 60L))) return

    val botUser = userData.find { it.activeMessageId == messageId.value } ?: return
    message.deleteReaction(userId, emoji)
    if (botUser.id != userId.value) return

    when (emoji) {
        in Constants.pinEmojis -> handlePinReaction(botUser)
        Emojis.back.asReactionEmoji() -> handleBackReaction(botUser)
        Emojis.check.asReactionEmoji() -> handleCheckReaction(botUser)
        else -> return
    }
}
