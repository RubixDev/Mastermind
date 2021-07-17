package de.rubixdev.mastermind.commands

import de.rubixdev.mastermind.client
import de.rubixdev.mastermind.showBoard
import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.interaction.CommandInteraction

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
