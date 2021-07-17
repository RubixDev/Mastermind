package de.rubixdev.mastermind.commands

import de.rubixdev.mastermind.showBoard
import dev.kord.common.annotation.KordPreview
import dev.kord.core.event.interaction.InteractionCreateEvent

@KordPreview
suspend fun InteractionCreateEvent.showCommand() {
    val responseBehavior = interaction.acknowledgePublic()

    showBoard(
        interaction.user.asUser(),
        interaction.data.guildId.value?.let { kord.getGuild(it) },
        interaction.getChannel(),
        responseBehavior
    )
}
