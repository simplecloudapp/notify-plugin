package app.simplecloud.plugin.notify.velocity

import app.simplecloud.plugin.notify.shared.CloudSender
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component

class VelocityCloudSender(
    private val player: Player,
) : CloudSender {

    override fun sendMessage(message: Component) {
        player.sendMessage(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }
}
