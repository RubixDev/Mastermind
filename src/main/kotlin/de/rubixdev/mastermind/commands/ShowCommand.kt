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
import dev.kord.core.entity.interaction.ButtonInteraction
import dev.kord.core.entity.interaction.CommandInteraction
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.MessageComponentBuilder
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

private fun boardDisplayDescription(botUser: BotUser): String {
    val board = botUser.board.rows.reversed().joinToString("") { row ->
        "${
            row.gamePins.joinToString("") {
                Constants.pinEmojis[it]
            }
        }   ${Emojis.black * row.answerPins.black}${Emojis.white * row.answerPins.white}\n"
    }

    return if (botUser.board.rows.lastOrNull()?.answerPins?.black == botUser.pins) {
        "**Congratulations**\n" +
                "You found the solution in ${botUser.board.rows.size} turns. Your board will stay visible below." +
                "To start a new game use `/show` or the button below, or to change the difficulty use `/config`.\n\n" +
                board
    } else {
        "_${botUser.nextMove.joinToString("") { Constants.pinEmojis[it] }} _\n" +
                "$board\n" +
                "Use the buttons below to input your move and submit when done."
    }
}

@KordPreview
private fun boardDisplayButtons(botUser: BotUser): MutableList<MessageComponentBuilder> {
    val components = mutableListOf<ActionRowBuilder.() -> Unit>()

    val pinButtons = Constants.pinEmojis.subList(0, (botUser.pins - 1) * 2).mapIndexed { i, it -> i to it }
    val chunkSize = when (pinButtons.size) {
        6 -> 3
        8 -> 4
        else -> 5
    }

    for (pinButtonsSubList in pinButtons.chunked(chunkSize)) {
        components.add {
            for ((pin, pinColor) in pinButtonsSubList) {
                interactionButton(ButtonStyle.Secondary, "${botUser.id}-game_event-$pin") {
                    emoji = DiscordPartialEmoji(name = pinColor)
                    disabled = !botUser.allowMultiples && pin in botUser.nextMove || botUser.nextMove.size == botUser.pins
                }
            }
        }
    }
    components.add {
        interactionButton(ButtonStyle.Danger, "${botUser.id}-game_event-delete") {
            label = "Remove last pin"
            disabled = botUser.nextMove.isEmpty()
        }
        interactionButton(ButtonStyle.Success, "${botUser.id}-game_event-submit") {
            label = "Submit"
            disabled = botUser.nextMove.size != botUser.pins
        }
    }

    return components.map { ActionRowBuilder().apply(it) }.toMutableList()
}

@KordPreview
private fun boardDisplayButtons(components: MutableList<MessageComponentBuilder>, botUser: BotUser) {
    components.clear()
    boardDisplayButtons(botUser).forEach { components.add(it) }
}

@KordPreview
suspend fun showBoard(
    author: User,
    guild: Guild?,
    channel: MessageChannel,
    responseBehavior: PublicInteractionResponseBehavior? = null,
    interaction: ButtonInteraction? = null
) {
    val authorId = author.id.value
    val botUser = getOrCreateUser(authorId)
    if (botUser.board.isFinished || botUser.board.solution.size != botUser.pins) {
        botUser.board = randomBoard(botUser.pins, botUser.allowMultiples)
    }

    val displayName = guild?.getMember(Snowflake(authorId))?.nickname ?: author.username
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
                    description = boardDisplayDescription(botUser)
                    color = Constants.themeColor
                    rubixFooter()
                }
                for (pinButtonsSubList in pinButtons.chunked(chunkSize)) {
                    actionRow {
                        for ((pin, pinColor) in pinButtonsSubList) {
                            interactionButton(ButtonStyle.Secondary, "$authorId-game_event-$pin") {
                                emoji = DiscordPartialEmoji(name = pinColor)
                            }
                        }
                    }
                }
                actionRow {
                    interactionButton(ButtonStyle.Danger, "$authorId-game_event-delete") { label = "Remove last pin" }
                    interactionButton(ButtonStyle.Success, "$authorId-game_event-submit") { label = "Submit" }
                }
                boardDisplayButtons(components, botUser)
            }.message

            botUser.activeMessageId = botMsg.id.value
            logger.info("Displayed board for ${author.username}")
        }
        interaction != null -> {
            val botMsg = channel.getMessage(Snowflake(botUser.activeMessageId))
            updateGameScreen(botMsg, botUser, true, interaction)
            logger.info("Updated Message for ${author.username}")
        }
        else -> logger.warn("Either a ResponseBehavior or a ComponentInteraction should be given. Neither is present")
    }

    saveUserData()
}

@KordPreview
suspend fun updateGameScreen(
    message: Message,
    botUser: BotUser,
    fullUpdate: Boolean = false,
    interaction: ButtonInteraction? = null,
    overrideComponents: MutableList<MessageComponentBuilder>? = null
) {
    val prevEmbed = message.embeds.getOrNull(0) ?: run {
        logger.warn("No embed found on specified message")
        return
    }

    val embed: EmbedBuilder.() -> Unit = {
        title = prevEmbed.title
        description = if (fullUpdate) {
            boardDisplayDescription(botUser)
        } else {
            prevEmbed.description?.replaceBefore(
                "\n",
                "_${botUser.nextMove.joinToString("") { Constants.pinEmojis[it] }} _"
            )
        }
        color = prevEmbed.color
        footer {
            text = prevEmbed.footer?.text ?: "Bot made by ${Constants.botAuthor.displayName}"
            icon = prevEmbed.footer?.iconUrl ?: Constants.botAuthor.avatar
        }
    }

    if (interaction != null) {
        interaction.acknowledgePublicUpdateMessage {
            embed(embed)
            components = overrideComponents ?: boardDisplayButtons(botUser)
        }
    } else {
        message.edit {
            embed(embed)
            components = overrideComponents ?: boardDisplayButtons(botUser)
        }
    }
}

@KordPreview
suspend fun updateGameScreen(interaction: ButtonInteraction, botUser: BotUser) {
    updateGameScreen(
        message = interaction.message ?: run {
            logger.error("Illegal state: Button interaction has no public message associated with it")
            return
        },
        botUser = botUser,
        fullUpdate = true,
        interaction = interaction
    )
}
