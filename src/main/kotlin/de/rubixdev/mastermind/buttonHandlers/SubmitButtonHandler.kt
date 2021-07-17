package de.rubixdev.mastermind.buttonHandlers

import de.rubixdev.mastermind.Constants
import de.rubixdev.mastermind.commands.showBoard
import de.rubixdev.mastermind.countIndexed
import de.rubixdev.mastermind.rubixFooter
import de.rubixdev.mastermind.saveUserData
import de.rubixdev.mastermind.userData.AnswerPins
import de.rubixdev.mastermind.userData.BoardRow
import de.rubixdev.mastermind.userData.BotUser
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.interaction.acknowledgePublicUpdateMessage
import dev.kord.core.behavior.interaction.followUp
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.rest.builder.interaction.embed
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger()

@KordPreview
suspend fun handleSubmitButtonPress(interaction: ComponentInteraction, botUser: BotUser) {
    if (botUser.nextMove.size != botUser.pins) {
        interaction.acknowledgePublicDeferredMessageUpdate()
        return
    }

    var totalPins = 0
    val matchedPins = mutableListOf<Int>()
    for (pin in botUser.nextMove) {
        if (pin in botUser.board.solution.filterIndexed { i, _ -> i !in matchedPins }) {
            var index = -1
            for ((i, p) in botUser.board.solution.withIndex()) {
                if (p == pin && i !in matchedPins) {
                    index = i
                }
            }
            matchedPins.add(index)
            totalPins++
        }
    }
    val blackPins = botUser.nextMove.countIndexed { i, pin -> pin == botUser.board.solution[i] }
    val whitePins = totalPins - blackPins

    botUser.board.rows.add(
        BoardRow(
            botUser.nextMove.toList(),
            AnswerPins(
                blackPins,
                whitePins
            )
        )
    )
    botUser.nextMove.clear()

    // TODO: revert to interaction.user.asUser().username when next kord releases
    val username = User(interaction.data.user.value!!, interaction.kord).username
    logger.info(
        "Processed move of ${username}: (${botUser.board.rows.last().gamePins.joinToString(")(")})"
    )

    val message = interaction.message ?: run {
        logger.error("Illegal state: Button interaction has no public message associated with it")
        return
    }

    if (blackPins == botUser.pins) {
        val responseBehaviour = interaction.acknowledgePublicUpdateMessage {
            components = mutableListOf()
        }
        logger.info(
            "$username found the solution after ${botUser.board.rows.size} turns: " +
                    "(${botUser.board.solution.joinToString(")(")})"
        )
        responseBehaviour.followUp {
            embed {
                title = "Congratulations!"
                description = "You found the answer after ${botUser.board.rows.size} turns"
                field {
                    name = "The solution was"
                    value = botUser.board.solution.joinToString("") { Constants.pinEmojis[it] }
                }
                color = Constants.themeColor
                rubixFooter()
            }
        }
        botUser.board.isFinished = true
        botUser.activeMessageId = 0

        saveUserData()
    } else {
        // TODO: revert to interaction.user.asUser().username when next kord releases
        showBoard(User(interaction.data.user.value!!, interaction.kord), message.getGuildOrNull(), message.getChannel(), interaction = interaction)
    }
}
