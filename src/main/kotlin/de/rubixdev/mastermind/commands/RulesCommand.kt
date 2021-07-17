package de.rubixdev.mastermind.commands

import de.rubixdev.mastermind.Constants
import de.rubixdev.mastermind.Emojis
import de.rubixdev.mastermind.rubixFooter
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.CommandInteraction
import dev.kord.rest.builder.interaction.embed
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger()

@KordPreview
suspend fun rulesCommand(interaction: CommandInteraction) {
    interaction.respondPublic {
        embed {
            title = "Rules for Mastermind"
            description = "A video explaining the game can be found here: https://youtu.be/5X9_5cavUPw\n\n" +
                    "Mastermind originally is a bord game from 1972. One player (in this case the bot) " +
                    "thinks of a random combination of four pins. The other player then has to find that " +
                    "solution by placing their guess in the first row of the open field. When he is done the " +
                    "bot places black pins ${Emojis.black} and white pins " +
                    "${Emojis.white} next to the row. Each black pin stands for one pin with the correct " +
                    "color and the correct spot. The player just isn't told which ones. White pins stand " +
                    "for pins that have the correct color, but are in the wrong spot."
            field {
                name = "Example"
                value = "${Emojis.red}${Emojis.black}${Emojis.blue}${Emojis.red}\n\n" +
                        "${Emojis.black}${Emojis.blue}${Emojis.blue}${Emojis.red}   " +
                        "${Emojis.black}${Emojis.black}${Emojis.white}\n" +
                        "${Emojis.yellow}${Emojis.green}${Emojis.red}${Emojis.white}   " +
                        "${Emojis.white}\n\n" +
                        "The row at the very top displays the solution the player tries to find. " +
                        "In a normal game this is obviously not shown.\n" +
                        "The player is currently two rounds in and would now have to input the third one. " +
                        "In his first guess (the bottom most row) he only got one white pin as feedback. In this " +
                        "case it stands for the red pin, which is a pin with the correct color, but at the wrong " +
                        "place (it should be one more right). The player can't know for sure which pin is meant " +
                        "yet, but he does know that only one is meant. The other pins are all colors that are " +
                        "not in the answer, so they don't give any pins.\n" +
                        "In his second guess, the player got two fully correct pins (the blue and red one at the " +
                        "right) and one with only the correct color (the black one). The second blue one in his " +
                        "guess doesn't give a white pin, because in the answer there is only one blue one, which " +
                        "is already matched with the other blue pin, giving a black pin."
            }
            field {
                name = "Commands"
                value = "- `/show` The main command of this bot. Shows your current board or " +
                        "starts a new game if the previous one finished.\n" +
                        "- `/newgame` Starts a new game, cancelling the current one, " +
                        "if there is one.\n" +
                        "- `/rules` Shows this message.\n" +
                        "- `/help` Alias for `/rules`\n" +
                        "- `/setpins <amount>` Sets the number of pins the game is played with. " +
                        "Is 4 by default and must be between 3 and 6.\n" +
                        "- `/allowmultiples <allow>` Allows or prevents multiple pins of the same color to exist in " +
                        "the solutions. Is on by default.\n" +
                        "- `/invite` Displays an invite link for this bot"
            }
            color = Constants.themeColor
            rubixFooter()
        }
    }
    logger.info("Displayed rules for ${interaction.user.asUser().username}")
}
