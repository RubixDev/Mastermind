package de.rubixdev.mastermind.eventHandlers

import de.rubixdev.mastermind.buttonHandlers.handleDeleteButtonPress
import de.rubixdev.mastermind.buttonHandlers.handlePinButtonPress
import de.rubixdev.mastermind.buttonHandlers.handleSubmitButtonPress
import de.rubixdev.mastermind.commandIds
import de.rubixdev.mastermind.commands.*
import de.rubixdev.mastermind.getOrCreateUser
import de.rubixdev.mastermind.testCommandIds
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ComponentType
import dev.kord.common.entity.InteractionType
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.ButtonInteraction
import dev.kord.core.entity.interaction.CommandInteraction
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
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
            testCommandIds["setpins"],
            commandIds["setpins"] -> setPinsCommand(commandInteraction)
            testCommandIds["rules"],
            commandIds["rules"],
            testCommandIds["help"],
            commandIds["help"] -> rulesCommand(commandInteraction)
            testCommandIds["allowmultiples"],
            commandIds["allowmultiples"] -> allowMultiplesCommand(commandInteraction)
            testCommandIds["invite"],
            commandIds["invite"] -> inviteCommand(commandInteraction)
            testCommandIds["config"],
            commandIds["config"] -> configCommand(commandInteraction)
        }
    } else {
        val componentInteraction = interaction as ComponentInteraction
        val component = componentInteraction.component ?: kotlin.run {
            logger.error("Illegal state: ComponentInteraction without public message")
            return
        }
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
                if (botUser.activeMessageId != componentInteraction.message?.id?.value) {
                    componentInteraction.acknowledgePublicDeferredMessageUpdate()
                    return
                }

                val buttonInteraction = componentInteraction as ButtonInteraction

                when (componentId[2]) {
                    "delete" -> handleDeleteButtonPress(buttonInteraction, botUser)
                    "submit" -> handleSubmitButtonPress(buttonInteraction, botUser)
                    else -> handlePinButtonPress(buttonInteraction, botUser, componentId[2].toInt())
                }
            }
            else -> logger.error("Invalid component id: $componentId")
        }
    }
}
