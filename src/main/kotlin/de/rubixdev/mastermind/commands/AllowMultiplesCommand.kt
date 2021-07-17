package de.rubixdev.mastermind.commands

import de.rubixdev.mastermind.Constants
import de.rubixdev.mastermind.getOrCreateUser
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.interaction.followUp
import dev.kord.core.entity.interaction.CommandInteraction
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.embed
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger()

@KordPreview
suspend fun allowMultiplesCommand(interaction: CommandInteraction) {
    val responseBehavior = interaction.acknowledgePublic()
    val botUser = getOrCreateUser(interaction.user.id.value)
    val allow = (interaction.command.options["allow"] as OptionValue.BooleanOptionValue).value
    botUser.allowMultiples = allow
    botUser.reset(interaction.user.asUser().username)
    responseBehavior.followUp {
        embed {
            title = "Success"
            description = "Your solutions will now be generated with${if (allow) "" else "out"} multiples"
            color = Constants.successColor
        }
    }
    logger.info("User ${interaction.user.asUser().username} set allowMultiples to $allow")
}
