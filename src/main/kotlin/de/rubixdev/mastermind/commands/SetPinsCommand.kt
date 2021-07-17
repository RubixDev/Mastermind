package de.rubixdev.mastermind.commands

import de.rubixdev.mastermind.Constants
import de.rubixdev.mastermind.getOrCreateUser
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.interaction.followUp
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.CommandInteraction
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.embed
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger()

@KordPreview
suspend fun setPinsCommand(interaction: CommandInteraction) {
    val pins = (interaction.command.options["amount"] as OptionValue.IntOptionValue).value
    if (pins !in 3..6) {
        interaction.respondPublic {
            embed {
                title = "Forbidden"
                description = "The amount of pins has to be between 3 and 6!"
                color = Constants.errorColor
            }
        }
    }
    val responseBehavior = interaction.acknowledgePublic()

    val author = interaction.user.asUser()
    val botUser = getOrCreateUser(author.id.value)
    botUser.pins = pins
    botUser.reset(author.username)

    responseBehavior.followUp {
        embed {
            title = "Success"
            description = "Your pin count was set to $pins"
            color = Constants.successColor
        }
    }
    logger.info("Set pins to $pins for ${author.username}")
}
