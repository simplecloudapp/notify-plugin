package app.simplecloud.plugin.notify.velocity

import app.simplecloud.plugin.notify.shared.NotifyPlugin
import app.simplecloud.plugin.notify.shared.command.CloudSender
import app.simplecloud.plugin.notify.velocity.command.VelocityCloudSender
import app.simplecloud.plugin.notify.velocity.listener.NotifyConnectionListener
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.velocity.VelocityCommandManager
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
class VelocityNotifyPlugin @Inject constructor(
    @param:DataDirectory private val dataDirectory: Path,
    private val server: ProxyServer,
) {

    private var notifyPlugin: NotifyPlugin? = null

    @Subscribe
    fun onProxyInitialize(event: ProxyInitializeEvent) {
        val notify = NotifyPlugin(dataDirectory, ::onlinePlayers)
        notify.start()
        notify.registerCommands(createCommandManager())

        notifyPlugin = notify
        server.eventManager.register(this, NotifyConnectionListener(notify))
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        notifyPlugin?.close()
        notifyPlugin = null
    }

    private fun createCommandManager(): VelocityCommandManager<VelocityCloudSender> {
        return VelocityCommandManager(
            server.pluginManager.ensurePluginContainer(this),
            server,
            ExecutionCoordinator.simpleCoordinator(),
            SenderMapper.create(::VelocityCloudSender, VelocityCloudSender::source)
        )
    }

    private fun onlinePlayers(): Iterable<CloudSender> {
        return server.allPlayers.map(::VelocityCloudSender)
    }

}
