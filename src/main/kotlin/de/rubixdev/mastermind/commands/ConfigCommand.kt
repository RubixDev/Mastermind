package de.rubixdev.mastermind.commands

import de.rubixdev.mastermind.Constants
import de.rubixdev.mastermind.getOrCreateUser
import de.rubixdev.mastermind.rubixFooter
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.CommandInteraction
import dev.kord.rest.builder.interaction.actionRow
import dev.kord.rest.builder.interaction.embed

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
                    "The second dropdown controls whether to allow multiple pins with the same color in the solution."
            color = Constants.themeColor
            rubixFooter()
        }
        actionRow {
            selectMenu("$authorId-config-pins") {
                for (i in 3..6) {
                    option("$i pins and ${(i - 1) * 2} colors", "$i") {
                        default = i == botUser.pins
                    }
                }
            }
        }
        actionRow {
            selectMenu("$authorId-config-multiples") {
                option("Multiples", "true") {
                    default = botUser.allowMultiples
                    description = "Allow multiple pins of the same color"
                }
                option("No multiples", "false") {
                    default = !botUser.allowMultiples
                    description = "Do not allow multiple pins of the same color"
                }
            }
        }
    }
}
