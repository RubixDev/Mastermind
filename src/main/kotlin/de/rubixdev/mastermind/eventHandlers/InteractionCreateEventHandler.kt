package de.rubixdev.mastermind.eventHandlers

import de.rubixdev.mastermind.*
import de.rubixdev.mastermind.buttonHandlers.handleDeleteButtonPress
import de.rubixdev.mastermind.buttonHandlers.handlePinButtonPress
import de.rubixdev.mastermind.buttonHandlers.handleSubmitButtonPress
import de.rubixdev.mastermind.commands.*
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.InteractionType
import dev.kord.core.behavior.interaction.acknowledgePublicUpdateMessage
import dev.kord.core.behavior.interaction.followUp
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.ButtonInteraction
import dev.kord.core.entity.interaction.CommandInteraction
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.interaction.actionRow
import dev.kord.rest.builder.interaction.embed
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger()

@KordPreview
suspend fun InteractionCreateEvent.handleInteractionCreateEvent() {
    if (interaction.type == InteractionType.ApplicationCommand) {
        val commandInteraction = interaction as CommandInteraction
        when (commandInteraction.command.rootId) {
            testCommandIds["show"],
            commandIds["show"] -> showCommand(commandInteraction)
            testCommandIds["newgame"],
            commandIds["newgame"] -> newGameCommand(commandInteraction)
            testCommandIds["rules"],
            commandIds["rules"],
            testCommandIds["help"],
            commandIds["help"] -> rulesCommand(commandInteraction)
            testCommandIds["invite"],
            commandIds["invite"] -> inviteCommand(commandInteraction)
            testCommandIds["config"],
            commandIds["config"] -> configCommand(commandInteraction)
        }
    } else {
        val componentInteraction = interaction as ComponentInteraction
        val componentId = componentInteraction.componentId.split('-')

        val authorId = componentId[0].toLong()
        val botUser = getOrCreateUser(authorId)
        // TODO: revert to interaction.user.asUser().id.value when next kord releases
        if (authorId != User(componentInteraction.data.user.value!!, componentInteraction.kord).id.value) {
            componentInteraction.acknowledgePublicDeferredMessageUpdate()
            return
        }

        when (componentId[1]) {
            "config" -> {
                val selectMenuInteraction = componentInteraction as SelectMenuInteraction
                handleConfigInteraction(selectMenuInteraction, botUser, componentId[2])
            }
            "game_event" -> {
                if (componentId[2] != "new_game" && botUser.activeMessageId != componentInteraction.message?.id?.value) {
                    componentInteraction.acknowledgePublicUpdateMessage {
                        components = mutableListOf()
                    }.followUp {
                        embed {
                            title = "Sorry"
                            description = "This is not your active game screen anymore. " +
                                    "If you can't find your active message, or you reset your data by changing a " +
                                    "setting, you can request a new one with `/show` or by clicking the button below."
                            color = Constants.errorColor
                        }
                        actionRow {
                            newGameButton(authorId)
                        }
                    }
                    return
                }

                val buttonInteraction = componentInteraction as ButtonInteraction

                when (componentId[2]) {
                    "delete" -> handleDeleteButtonPress(buttonInteraction, botUser)
                    "submit" -> handleSubmitButtonPress(buttonInteraction, botUser)
                    "new_game" -> {
                        val responseBehavior = buttonInteraction.acknowledgePublicUpdateMessage {
                            actionRow {
                                newGameButton(authorId, true)
                            }
                        }

                        // TODO: revert to interaction.user.asUser().username when next kord releases
                        botUser.reset(User(componentInteraction.data.user.value!!, componentInteraction.kord).username)
                        showBoard(
                            // TODO: revert to interaction.user.asUser() when next kord releases
                            author = User(componentInteraction.data.user.value!!, componentInteraction.kord),
                            guild = interaction.data.guildId.value?.let { client.getGuild(it) },
                            channel = interaction.getChannel(),
                            responseBehavior = responseBehavior
                        )
                    }
                    else -> handlePinButtonPress(buttonInteraction, botUser, componentId[2].toInt())
                }
            }
            else -> logger.error("Invalid component id: $componentId")
        }
    }
}

@KordPreview
fun ActionRowBuilder.newGameButton(
    userId: Long,
    disabled: Boolean = false
) = interactionButton(ButtonStyle.Primary, "$userId-game_event-new_game") {
    label = "Start new game"
    this.disabled = disabled
}
