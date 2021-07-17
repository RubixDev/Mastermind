package de.rubixdev.mastermind.commands

import de.rubixdev.mastermind.*
import de.rubixdev.mastermind.userData.BotUser
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import dev.kord.core.behavior.interaction.acknowledgePublicUpdateMessage
import dev.kord.core.behavior.interaction.followUp
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.interaction.CommandInteraction
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.rest.builder.interaction.actionRow
import dev.kord.rest.builder.interaction.embed
import dev.kord.rest.builder.message.EmbedBuilder
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger()

@KordPreview
suspend fun showCommand(interaction: CommandInteraction) {
    val responseBehavior = interaction.acknowledgePublic()

    showBoard(
        interaction.user.asUser(),
        interaction.data.guildId.value?.let { client.getGuild(it) },
        interaction.getChannel(),
        responseBehavior
    )
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

@KordPreview
suspend fun updateMessage(
    message: Message,
    botUser: BotUser,
    overrideDesc: String? = null,
    interaction: ComponentInteraction? = null
) {
    val prevEmbed = message.embeds.getOrNull(0) ?: run {
        logger.warn("No embed found on specified message")
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
