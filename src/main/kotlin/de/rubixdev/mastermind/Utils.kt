package de.rubixdev.mastermind

import de.rubixdev.mastermind.userData.Board
import de.rubixdev.mastermind.userData.BotUser
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.createApplicationCommand
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import dev.kord.core.behavior.interaction.acknowledgePublicUpdateMessage
import dev.kord.core.behavior.interaction.followUp
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.rest.builder.interaction.ApplicationCommandCreateBuilder
import dev.kord.rest.builder.interaction.actionRow
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

suspend fun getOrCreateUser(id: Long): BotUser {
    return userData.find { it.id == id }
        ?: BotUser(id, 0, 4, true, mutableListOf(), randomBoard())
            .also { userData.add(it); updatePresence() }
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

suspend fun updatePresence() {
    client.editPresence {
        playing("Mastermind on ${client.guilds.count()} servers with ${userData.size} unique users")
    }
}

@KordPreview
suspend fun updateMessage(
    message: Message,
    botUser: BotUser,
    overrideDesc: String? = null,
    interaction: ComponentInteraction? = null
) {
    val prevEmbed = message.embeds.getOrNull(0) ?: run {
        logger.warn("Embed removed")
        message.edit {
            embed {
                title = "Error"
                description = "The embed on the active message seems to be gone. " +
                        "To continue playing use `/show` again."
                color = Constants.errorColor
            }
        }
        return
    }

    val embed: EmbedBuilder.() -> Unit = {
        title = prevEmbed.title
        description = overrideDesc ?: prevEmbed.description?.replaceBefore(
            "\n",
            "_${botUser.nextMove.joinToString("") { Constants.pinEmojis[it] }} _"
        )
        color = prevEmbed.color
        footer {
            text = prevEmbed.footer?.text ?: "Bot made by ${Constants.botAuthor.displayName}"
            icon = prevEmbed.footer?.iconUrl ?: Constants.botAuthor.avatar
        }
    }

    if (interaction != null) {
        interaction.acknowledgePublicUpdateMessage {
            embed(embed)
        }
    } else {
        message.edit {
            embed(embed)
        }
    }
}

suspend fun displayGuilds(): String {
    return client.guilds.toList().joinToString("\n        ", "\n        ") {
        "${it.name} --> ${it.id.value}"
    }
}

@KordPreview
suspend fun showBoard(
    author: User,
    guild: Guild?,
    channel: MessageChannel,
    responseBehavior: PublicInteractionResponseBehavior? = null,
    interaction: ComponentInteraction? = null
) {
    val authorId = author.id.value
    val botUser = getOrCreateUser(authorId)
    if (botUser.board.isFinished || botUser.board.solution.size != botUser.pins) {
        botUser.board = randomBoard(botUser.pins, botUser.allowMultiples)
    }

    var boardDisplay = ""
    for (row in botUser.board.rows.reversed()) {
        boardDisplay += "${
            row.gamePins.joinToString("") { Constants.pinEmojis[it] }
        }   ${
            Emojis.black * row.answerPins.black
        }${
            Emojis.white * row.answerPins.white
        }\n"
    }

    val displayName = guild?.getMember(Snowflake(authorId))?.nickname ?: author.username
    val desc = "_${botUser.nextMove.joinToString("") { Constants.pinEmojis[it] }} _\n$boardDisplay\n" +
            "Use the buttons below to input your move and submit when done."
    val pinButtons = Constants.pinEmojis.subList(0, (botUser.pins - 1) * 2).mapIndexed { i, it -> i to it }
    val chunkSize = when (pinButtons.size) {
        6 -> 3
        8 -> 4
        else -> 5
    }

    when {
        responseBehavior != null -> {
            val botMsg = responseBehavior.followUp {
                embed {
                    title = "Current board of $displayName"
                    description = desc
                    color = Constants.themeColor
                    rubixFooter()
                }
                for (pinButtonsSubList in pinButtons.chunked(chunkSize)) {
                    actionRow {
                        for ((pin, pinColor) in pinButtonsSubList) {
                            interactionButton(ButtonStyle.Secondary, "$authorId-$pin") {
                                emoji = DiscordPartialEmoji(name = pinColor)
                            }
                        }
                    }
                }
                actionRow {
                    interactionButton(ButtonStyle.Danger, "$authorId-delete") { label = "Remove last pin" }
                    interactionButton(ButtonStyle.Success, "$authorId-submit") { label = "Submit" }
                }
            }.message

            botUser.activeMessageId = botMsg.id.value
            logger.info("Displayed board for ${author.username}")
        }
        interaction != null -> {
            val botMsg = channel.getMessage(Snowflake(botUser.activeMessageId))
            updateMessage(botMsg, botUser, desc, interaction)
            logger.info("Updated Message for ${author.username}")
        }
        else -> logger.warn("Either a ResponseBehaviour or a ComponentInteraction should be given. Neither is present")
    }

    saveUserData()
}
