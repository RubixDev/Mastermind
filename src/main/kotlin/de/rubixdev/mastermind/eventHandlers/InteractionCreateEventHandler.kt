package de.rubixdev.mastermind.eventHandlers

import de.rubixdev.mastermind.buttonHandlers.handleDeleteButtonPress
import de.rubixdev.mastermind.buttonHandlers.handlePinButtonPress
import de.rubixdev.mastermind.buttonHandlers.handleSubmitButtonPress
import de.rubixdev.mastermind.commandIds
import de.rubixdev.mastermind.commands.*
import de.rubixdev.mastermind.getOrCreateUser
import de.rubixdev.mastermind.testCommandIds
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.InteractionType
import dev.kord.core.entity.interaction.CommandInteraction
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent

@KordPreview
suspend fun InteractionCreateEvent.handleInteractionCreateEvent() {
    if (interaction.type == InteractionType.ApplicationCommand) {
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
    } else {
        val componentInteraction = interaction as ComponentInteraction

        val buttonId = componentInteraction.componentId.split('-')
        val authorId = buttonId[0].toLong()
        val botUser = getOrCreateUser(authorId)
        if (authorId != componentInteraction.user.id.value
            || botUser.activeMessageId != componentInteraction.message?.id?.value
        ) {
            componentInteraction.acknowledgePublicDeferredMessageUpdate()
            return
        }

        when (buttonId[1]) {
            "delete" -> handleDeleteButtonPress(componentInteraction, botUser)
            "submit" -> handleSubmitButtonPress(componentInteraction, botUser)
            else -> handlePinButtonPress(componentInteraction, botUser, buttonId[1].toInt())
        }
    }
}
