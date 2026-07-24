package app.simplecloud.plugin.notify.shared

import app.simplecloud.api.CloudApi
import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.plugin.notify.shared.command.CloudSender
import app.simplecloud.plugin.notify.shared.command.NotifyCommand
import app.simplecloud.plugin.notify.shared.config.MessageConfig
import app.simplecloud.plugin.notify.shared.listener.NotificationListener
import app.simplecloud.plugin.notify.shared.notification.NotificationRenderer
import app.simplecloud.plugin.notify.shared.notification.NotificationSettings
import kotlinx.coroutines.*
import org.incendo.cloud.CommandManager
import java.nio.file.Path
import java.util.UUID

class NotifyPlugin(
    dataDirectory: Path,
    recipients: () -> Iterable<CloudSender>,
) {

    val config = ConfigurationFactory(dataDirectory.resolve("messages.yml").toFile(), MessageConfig::class.java)

    private val api = CloudApi.create()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val renderer = NotificationRenderer(config::get)
    private val settings = NotificationSettings(api.player())
    private val listener = NotificationListener(api, config::get, recipients, renderer, settings)

    fun start() {
        config.loadOrCreate(MessageConfig())
        listener.start()
    }

    fun close() {
        listener.stop()
        settings.clear()
        scope.cancel()
    }

    fun <C : CloudSender> registerCommands(manager: CommandManager<C>) {
        NotifyCommand(manager, this, renderer, settings, api.player()).register()
    }

    fun onPlayerJoin(id: UUID) {
        scope.launch {
            settings.load(id)
        }
    }

    fun onPlayerQuit(id: UUID) {
        settings.unload(id)
    }

    fun reload(): Boolean {
        return runCatching { config.reload() }.isSuccess
    }

}
