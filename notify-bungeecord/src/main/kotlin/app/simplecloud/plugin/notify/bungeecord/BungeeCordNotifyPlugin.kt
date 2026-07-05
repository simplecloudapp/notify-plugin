package app.simplecloud.plugin.notify.bungeecord

import app.simplecloud.plugin.notify.shared.CloudSender
import app.simplecloud.plugin.notify.shared.NotifyPlugin
import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import net.md_5.bungee.api.plugin.Plugin

class BungeeCordNotifyPlugin : Plugin() {

    private val adventure = BungeeAudiences.create(this)

    private var notify: NotifyPlugin? = null

    override fun onEnable() {
        notify = NotifyPlugin(dataFolder.toPath(), ::onlinePlayers)
    }

    override fun onDisable() {
        notify?.close()
        notify = null
        adventure.close()
    }

    private fun onlinePlayers(): Iterable<CloudSender> {
        return proxy.players.map { BungeeCloudSender(it, adventure) }
    }

}
