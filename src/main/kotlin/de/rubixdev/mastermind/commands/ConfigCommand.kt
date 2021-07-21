package de.rubixdev.mastermind.commands

import de.rubixdev.mastermind.Constants
import de.rubixdev.mastermind.getOrCreateUser
import de.rubixdev.mastermind.rubixFooter
import de.rubixdev.mastermind.saveUserData
import de.rubixdev.mastermind.userData.BotUser
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.interaction.acknowledgePublicUpdateMessage
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.CommandInteraction
import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.interaction.actionRow
import dev.kord.rest.builder.interaction.embed
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger()

@KordPreview
suspend fun configCommand(interaction: CommandInteraction) {
    val authorId = interaction.user.id.value
    val botUser = getOrCreateUser(authorId)

    interaction.respondPublic {
        embed {
            title = "Settings"
            description = "Here you can change a few things about the game.\n" +
                    "The first dropdown controls the number of pins to generate the solution with and therefore also " +
                    "the amount of available colors.\n" +
                    "The second dropdown controls whether to allow multiple pins with the same color in the solution.\n" +
                    "As soon as you change any value your current game (if existent) will be cancelled."
            field {
                name = "Your current settings"
                value = "Pins: ${botUser.pins}\n" +
                        "Allow multiples: ${botUser.allowMultiples}"
            }
            color = Constants.themeColor
            rubixFooter()
        }
        actionRow(pinSelectMenu(botUser))
        actionRow(allowMultiplesSelectMenu(botUser))
    }
}

@KordPreview
private fun pinSelectMenu(botUser: BotUser): ActionRowBuilder.() -> Unit = {
    selectMenu("${botUser.id}-config-pins") {
        for (i in 3..6) {
            option("$i pins and ${(i - 1) * 2} colors", "$i") {
                default = i == botUser.pins
            }
        }
    }
}

@KordPreview
private fun allowMultiplesSelectMenu(botUser: BotUser): ActionRowBuilder.() -> Unit = {
    selectMenu("${botUser.id}-config-multiples") {
        option("With Multiples", "true") {
            default = botUser.allowMultiples
            description = "Allow multiple pins of the same color"
        }
        option("Without multiples", "false") {
            default = !botUser.allowMultiples
            description = "Do not allow multiple pins of the same color"
        }
    }
}

@KordPreview
suspend fun handleConfigInteraction(interaction: SelectMenuInteraction, botUser: BotUser, setting: String) {
    val username = interaction.user.username
    botUser.reset(username)

    when (setting) {
        "pins" -> {
            val pins = interaction.values[0].toInt()
            botUser.pins = pins
            logger.info("Set pins to $pins for $username")
        }
        "multiples" -> {
            val allow = interaction.values[0].toBoolean()
            botUser.allowMultiples = allow
            logger.info("User $username set allowMultiples to $allow")
        }
        else -> {
            logger.warn("Unknown setting: $setting")
            return
        }
    }

    saveUserData()

    val prevEmbed = (interaction.message ?: run {
        logger.error("Illegal state: SelectMenu interaction has no public message associated with it")
        return
    }).embeds.getOrNull(0) ?: run {
        logger.warn("No embed found on specified message")
        return
    }

    interaction.acknowledgePublicUpdateMessage {
        embed {
            title = prevEmbed.title
            description = prevEmbed.description
            field {
                name = "Your current settings"
                value = "Pins: ${botUser.pins}\n" +
                        "Allow multiples: ${botUser.allowMultiples}"
            }
            color = prevEmbed.color
            rubixFooter()
        }
        actionRow(pinSelectMenu(botUser))
        actionRow(allowMultiplesSelectMenu(botUser))
    }
}
