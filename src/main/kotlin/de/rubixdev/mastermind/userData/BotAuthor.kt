package de.rubixdev.mastermind.userData

import dev.kord.core.entity.User

data class BotAuthor(
    val user: User,
    val displayName: String = "${user.username}#${user.discriminator}",
    val avatar: String = user.avatar.url
)
