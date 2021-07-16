import dev.kord.common.Color
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.guild.GuildDeleteEvent
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

private val logger: Logger = LogManager.getLogger("Main")
val json = Json { prettyPrint = true }
val userData = json.decodeFromString<MutableList<BotUser>>(
    File("userData.json")
        .also { it.createNewFile() }
        .readText()
        .ifEmpty { "[]" }
)
val commandIds = mutableMapOf<String, Snowflake>()
val testCommandIds = mutableMapOf<String, Snowflake>()
var isReady = false

object Emojis {
    const val blue = "\ud83d\udd35"
    const val green = "\ud83d\udfe2"
    const val red = "\ud83d\udd34"
    const val yellow = "\ud83d\udfe1"
    const val black = "\u2b55"
    const val white = "\u26aa"
    const val orange = "\ud83d\udfe0"
    const val purple = "\ud83d\udfe3"
    const val brown = "\ud83d\udfe4"
    const val gray = "\u26ab"

    const val back = "\u2b05\ufe0f"
    const val check = "\u2705"
}

object Constants {
    val themeColor = Color(0xd76944)
    val errorColor = Color(0xff0000)
    val successColor = Color(0x00ff00)

    lateinit var botAuthor: BotAuthor

    val pinEmojis = listOf(
        Emojis.blue,
        Emojis.green,
        Emojis.red,
        Emojis.yellow,
        Emojis.black,
        Emojis.white,
        Emojis.orange,
        Emojis.purple,
        Emojis.brown,
        Emojis.gray
    ).map { it.asReactionEmoji() }
}

/**
 * [Invite bot](https://discord.com/api/oauth2/authorize?client_id=830490572765790220&permissions=10304&scope=bot%20applications.commands)
 */
@KordPreview
suspend fun main() {
    val client = Kord(object {}.javaClass.getResource("/devToken.txt")!!.readText()) {
        intents = Intents(
            Intent.Guilds,
            Intent.GuildMessageReactions
        )
    }

    Constants.botAuthor = BotAuthor(client.getUser(Snowflake(506069336247500811))!!)

    client.on<ReadyEvent> { handleReadyEvent() }
    client.on<ReactionAddEvent> { handleReactionAddEvent() }
    client.on<GuildCreateEvent> { handleGuildCreateEvent() }
    client.on<GuildDeleteEvent> { handleGuildDeleteEvent() }
    client.on<InteractionCreateEvent> { handleInteractionCreateEvent() }

    client.login()
}
