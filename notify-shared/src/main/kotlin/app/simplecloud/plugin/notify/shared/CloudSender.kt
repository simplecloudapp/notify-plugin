package app.simplecloud.plugin.notify.shared

import net.kyori.adventure.text.Component

interface CloudSender {

    fun sendMessage(message: Component)

    fun hasPermission(permission: String): Boolean
}
