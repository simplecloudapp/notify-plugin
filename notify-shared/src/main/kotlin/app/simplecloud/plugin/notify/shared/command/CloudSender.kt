package app.simplecloud.plugin.notify.shared.command

import net.kyori.adventure.text.Component
import java.util.UUID

interface CloudSender {

    val uniqueId: UUID?

    val name: String

    fun sendMessage(message: Component)

    fun hasPermission(permission: String): Boolean
}
