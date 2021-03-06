package de.rubixdev.mastermind.buttonHandlers

import de.rubixdev.mastermind.commands.showBoard
import de.rubixdev.mastermind.commands.updateGameScreen
import de.rubixdev.mastermind.countIndexed
import de.rubixdev.mastermind.eventHandlers.newGameButton
import de.rubixdev.mastermind.saveUserData
import de.rubixdev.mastermind.userData.AnswerPins
import de.rubixdev.mastermind.userData.BoardRow
import de.rubixdev.mastermind.userData.BotUser
import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.interaction.ButtonInteraction
import dev.kord.rest.builder.component.ActionRowBuilder
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

private val logger: Logger = LogManager.getLogger()

@KordPreview
suspend fun handleSubmitButtonPress(interaction: ButtonInteraction, botUser: BotUser) {
    if (botUser.nextMove.size != botUser.pins) {
        updateGameScreen(interaction, botUser)
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

    val username = interaction.user.username
    logger.info(
        "Processed move of ${username}: (${botUser.board.rows.last().gamePins.joinToString(")(")})"
    )

    val message = interaction.message ?: run {
        logger.error("Illegal state: Button interaction has no public message associated with it")
        return
    }

    if (blackPins == botUser.pins) {
        updateGameScreen(
            message = message,
            botUser = botUser,
            fullUpdate = true,
            interaction = interaction,
            overrideComponents = mutableListOf(ActionRowBuilder().apply {
                newGameButton(botUser.id)
            })
        )
        logger.info(
            "$username found the solution after ${botUser.board.rows.size} turns: " +
                    "(${botUser.board.solution.joinToString(")(")})"
        )
        botUser.board.isFinished = true
        botUser.activeMessageId = 0

        saveUserData()
    } else {
        showBoard(interaction.user, message.getGuildOrNull(), message.getChannel(), interaction = interaction)
    }
}
