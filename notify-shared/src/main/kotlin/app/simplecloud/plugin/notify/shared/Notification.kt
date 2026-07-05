package app.simplecloud.plugin.notify.shared

import net.kyori.adventure.text.Component

class Notification(
    val message: Component,
    permissions: Iterable<String> = emptyList(),
) {
    constructor(message: Component, permission: String) : this(message, listOf(permission))

    val permissions: Set<String> = permissions
        .filter { it.isNotBlank() }
        .toSet()

    fun sendTo(sender: CloudSender) {
        if (canReceive(sender)) {
            sender.sendMessage(message)
        }
    }

    private fun canReceive(sender: CloudSender): Boolean {
        return permissions.isEmpty() || permissions.any { sender.hasPermission(it) }
    }
}
