package app.simplecloud.plugin.notify.velocity

import app.simplecloud.plugin.notify.shared.NotifyPlugin
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import java.nio.file.Path

@Plugin(
    id = "notify-velocity",
    name = "notify-velocity",
    version = "1.0-SNAPSHOT",
    authors = ["rlqu"],
    dependencies = [
        Dependency(
            id = "simplecloud-api"
        )
    ],
    url = "https://github.com/simplecloudapp/notify-plugin"
)
class VelocityNotifyPlugin @Inject constructor(@DataDirectory val dataDirectory: Path, private val server: ProxyServer) {

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        val notifyPlugin = NotifyPlugin(dataDirectory)

        notifyPlugin.listeningFunction = { message, permission ->
            server.sendMessage(message)
            server.allPlayers.forEach { onlinePlayer ->
                if (permission.isEmpty() || onlinePlayer.hasPermission(permission)) {
                    onlinePlayer.sendMessage(message)
                }
            }
        }
    }

}