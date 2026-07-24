package app.simplecloud.plugin.notify.bungeecord

import app.simplecloud.plugin.notify.bungeecord.command.BungeeCloudSender
import app.simplecloud.plugin.notify.bungeecord.listener.NotifyConnectionListener
import app.simplecloud.plugin.notify.shared.NotifyPlugin
import app.simplecloud.plugin.notify.shared.command.CloudSender
import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import net.md_5.bungee.api.plugin.Plugin
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.bungee.BungeeCommandManager
import org.incendo.cloud.execution.ExecutionCoordinator

class BungeeCordNotifyPlugin : Plugin() {

    private val adventure = BungeeAudiences.create(this)

    private var notifyPlugin: NotifyPlugin? = null

    override fun onEnable() {
        val notify = NotifyPlugin(dataFolder.toPath(), ::onlinePlayers)
        notify.start()
        notify.registerCommands(createCommandManager())

        notifyPlugin = notify
        proxy.pluginManager.registerListener(this, NotifyConnectionListener(notify))
    }

    override fun onDisable() {
        notifyPlugin?.close()
        notifyPlugin = null
        adventure.close()
    }

    private fun createCommandManager(): BungeeCommandManager<BungeeCloudSender> {
        return BungeeCommandManager(
            this,
            ExecutionCoordinator.simpleCoordinator(),
            SenderMapper.create(
                { source -> BungeeCloudSender(source, adventure) },
                BungeeCloudSender::source
            )
        )
    }

    private fun onlinePlayers(): Iterable<CloudSender> {
        return proxy.players.map { BungeeCloudSender(it, adventure) }
    }

}
