package de.rubixdev.mastermind.commands

import de.rubixdev.mastermind.getOrCreateUser
import de.rubixdev.mastermind.showBoard
import dev.kord.common.annotation.KordPreview
import dev.kord.core.event.interaction.InteractionCreateEvent

@KordPreview
suspend fun InteractionCreateEvent.newGameCommand() {
    val responseBehavior = interaction.acknowledgePublic()

    val botUser = getOrCreateUser(interaction.user.id.value)
    botUser.reset(interaction.user.asUser().username)
    showBoard(
        interaction.user.asUser(),
        interaction.data.guildId.value?.let { kord.getGuild(it) },
        interaction.getChannel(),
        responseBehavior
    )
}
