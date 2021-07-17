package de.rubixdev.mastermind

import de.rubixdev.mastermind.userData.Board
import de.rubixdev.mastermind.userData.BotUser
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.createApplicationCommand
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import dev.kord.core.behavior.interaction.followUp
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.interaction.ApplicationCommandCreateBuilder
import dev.kord.rest.builder.interaction.embed
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

private val logger: Logger = LogManager.getLogger()

private operator fun String.times(n: Int): String {
    var out = ""
    for (i in 0 until n) {
        out += this
    }
    return out
}

fun String.asReactionEmoji(): ReactionEmoji.Unicode {
    return ReactionEmoji.Unicode(this)
}

fun <T> Iterable<T>.countIndexed(predicate: (index: Int, T) -> Boolean): Int {
    return this.filterIndexed(predicate).size
}

@KordPreview
suspend fun ReadyEvent.addCommand(
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
//    addTestCommand(name, description, builder)
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

@KordPreview
suspend fun InteractionCreateEvent.testPermissions(responseBehavior: PublicInteractionResponseBehavior): Boolean {
    val channel = interaction.getChannel() as GuildMessageChannel
    if (!channel.getEffectivePermissions(kord.selfId).contains(Permission.AddReactions)) {
        responseBehavior.followUp {
            embed {
                title = "I can't play with you here"
                description = "I am not allowed to add new reactions to messages in this channel, " +
                        "which is required for me to work. Please allow me to do so."
                color = Constants.errorColor
            }
        }
        return false
    }
    if (!channel.getEffectivePermissions(kord.selfId).contains(Permission.ManageMessages)) {
        responseBehavior.followUp {
            embed {
                title = "That is not supposed to happen"
                color = Constants.errorColor
                description = "I don't seem to have the permission to remove your reactions, so you will " +
                        "have to do that yourself. " +
                        "For the best experience please allow me to manage messages in this channel."
            }
        }
    }
    return true
}

fun randomBoard(pins: Int = 4, allowMultiples: Boolean = true): Board {
    if (allowMultiples) {
        return Board(
            false,
            (1..pins).map { (0 until ((pins - 1) * 2)).random() },
            mutableListOf()
        )
    } else {
        val solution = mutableListOf<Int>()
        for (pin in 1..pins) {
            var randomPin = (0 until ((pins - 1) * 2)).random()
            while (randomPin in solution) {
                randomPin = (0 until ((pins - 1) * 2)).random()
            }
            solution.add(randomPin)
        }
        return Board(
            false,
            solution,
            mutableListOf()
        )
    }
}

fun getOrCreateUser(id: Long): BotUser {
    return userData.find { it.id == id }
        ?: BotUser(id, 0, 4, true, mutableListOf(), randomBoard())
            .also { userData.add(it) }
}

fun saveUserData() {
    File("userData.json")
        .also { it.createNewFile() }
        .writeText(json.encodeToString(userData))
}

fun EmbedBuilder.rubixFooter() = footer {
    text = "Bot made by ${Constants.botAuthor.displayName}"
    icon = Constants.botAuthor.avatar
}

suspend fun updatePresence(kord: Kord) {
    kord.editPresence {
        playing("Mastermind on ${kord.guilds.count()} servers")
    }
}

suspend fun updateMessage(message: Message, botUser: BotUser, overrideDesc: String? = null) {
    message.edit {
        embed {
            val prevEmbed = message.embeds[0]
            title = prevEmbed.title
            description = overrideDesc
                ?: prevEmbed.description?.replaceBefore(
                    "\n",
                    "_${botUser.nextMove.joinToString("") { Constants.pinEmojis[it].name }} _"
                )
            color = prevEmbed.color
            footer {
                text = prevEmbed.footer?.text ?: "Bot made by ${Constants.botAuthor.displayName}"
                icon = prevEmbed.footer?.iconUrl ?: Constants.botAuthor.avatar
            }
        }
    }
}

suspend fun displayGuilds(kord: Kord): String {
    return kord.guilds.toList().joinToString("\n        ", "\n        ") {
        "${it.name} --> ${it.id.value}"
    }
}

@KordPreview
suspend fun showBoard(
    author: User,
    guild: Guild?,
    channel: MessageChannel,
    responseBehavior: PublicInteractionResponseBehavior?
) {
    val authorId = author.id.value
    val botUser = getOrCreateUser(authorId)
    if (botUser.board.isFinished || botUser.board.solution.size != botUser.pins) {
        botUser.board = randomBoard(botUser.pins, botUser.allowMultiples)
    }

    var boardDisplay = ""
    for (row in botUser.board.rows.reversed()) {
        boardDisplay += "${
            row.gamePins.joinToString("") { Constants.pinEmojis[it].name }
        }   ${
            Emojis.black * row.answerPins.black
        }${
            Emojis.white * row.answerPins.white
        }\n"
    }

    val displayName = guild?.getMember(Snowflake(authorId))?.nickname ?: author.username
    val desc = "_${botUser.nextMove.joinToString("") { Constants.pinEmojis[it].name }} _\n$boardDisplay\n" +
            "Input your move using the reactions below, delete the last pin " +
            "using ${Emojis.back} and submit with ${Emojis.check}"

    if (responseBehavior != null) {
        val botMsg = responseBehavior.followUp {
            embed {
                title = "Current board of $displayName"
                description = desc
                color = Constants.themeColor
                rubixFooter()
            }
        }.message

        for (pinColor in Constants.pinEmojis.subList(0, (botUser.pins - 1) * 2)) {
            botMsg.addReaction(pinColor)
        }
        botMsg.addReaction(Emojis.back.asReactionEmoji())
        botMsg.addReaction(Emojis.check.asReactionEmoji())

        botUser.activeMessageId = botMsg.id.value
        logger.info("Displayed board for ${author.username}")
    } else {
        val botMsg = channel.getMessage(Snowflake(botUser.activeMessageId))
        updateMessage(botMsg, botUser, desc)
        logger.info("Updated Message for ${author.username}")
    }

    saveUserData()
}
