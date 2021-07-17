package de.rubixdev.mastermind

import de.rubixdev.mastermind.userData.Board
import de.rubixdev.mastermind.userData.BotUser
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.encodeToString
import java.io.File

operator fun String.times(n: Int): String {
    var out = ""
    for (i in 0 until n) {
        out += this
    }
    return out
}

fun <T> Iterable<T>.countIndexed(predicate: (index: Int, T) -> Boolean): Int {
    return this.filterIndexed(predicate).size
}

fun EmbedBuilder.rubixFooter() = footer {
    text = "Bot made by ${Constants.botAuthor.displayName}"
    icon = Constants.botAuthor.avatar
}

fun saveUserData() {
    File("userData.json")
        .also { it.createNewFile() }
        .writeText(json.encodeToString(userData))
}

suspend fun updatePresence() {
    client.editPresence {
        playing("Mastermind on ${client.guilds.count()} servers with ${userData.size} unique users")
    }
}

suspend fun displayGuilds(): String {
    return client.guilds.toList().joinToString("\n        ", "\n        ") {
        "${it.name} --> ${it.id.value}"
    }
}

suspend fun getOrCreateUser(id: Long): BotUser {
    return userData.find { it.id == id }
        ?: BotUser(id, 0, 4, true, mutableListOf(), randomBoard())
            .also { userData.add(it); updatePresence() }
}

fun randomBoard(pins: Int = 4, allowMultiples: Boolean = true): Board {
    if (allowMultiples) {
        return Board(
            false,
            (1..pins).map { (0 until ((pins - 1) * 2)).random() },
            mutableListOf()
        )
    } else {
        val solution = mutableListOf<Int>()
        for (pin in 1..pins) {
            var randomPin = (0 until ((pins - 1) * 2)).random()
            while (randomPin in solution) {
                randomPin = (0 until ((pins - 1) * 2)).random()
            }
            solution.add(randomPin)
        }
        return Board(
            false,
            solution,
            mutableListOf()
        )
    }
}
