package de.rubixdev.mastermind.commands

import de.rubixdev.mastermind.showBoard
import de.rubixdev.mastermind.testPermissions
import dev.kord.common.annotation.KordPreview
import dev.kord.core.event.interaction.InteractionCreateEvent

@KordPreview
suspend fun InteractionCreateEvent.showCommand() {
    val responseBehavior = interaction.acknowledgePublic()
    if (!testPermissions(responseBehavior)) return

    showBoard(
        interaction.user.asUser(),
        interaction.data.guildId.value?.let { kord.getGuild(it) },
        interaction.getChannel(),
        responseBehavior
    )
}
