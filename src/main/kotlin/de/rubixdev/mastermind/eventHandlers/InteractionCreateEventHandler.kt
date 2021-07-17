package de.rubixdev.mastermind.eventHandlers

import de.rubixdev.mastermind.*
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ChannelType
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.CommandInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.interaction.embed

@KordPreview
suspend fun InteractionCreateEvent.handleInteractionCreateEvent() {
    if (interaction.getChannel().type != ChannelType.GuildText) {
        interaction.respondPublic {
            embed {
                title = "Forbidden"
                description = "This bot does not work in this channel. " +
                        "Please use it in a normal server text channel.\n" +
                        "Use this link to invite me to your server:\n" +
                        Constants.inviteLink
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
