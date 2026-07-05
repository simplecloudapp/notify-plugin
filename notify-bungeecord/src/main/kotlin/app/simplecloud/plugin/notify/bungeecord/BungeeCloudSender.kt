package app.simplecloud.plugin.notify.bungeecord

import app.simplecloud.plugin.notify.shared.CloudSender
import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.connection.ProxiedPlayer

class BungeeCloudSender(
    private val player: ProxiedPlayer,
    private val adventure: BungeeAudiences,
) : CloudSender {

    override fun sendMessage(message: Component) {
        adventure.player(player).sendMessage(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }
}
