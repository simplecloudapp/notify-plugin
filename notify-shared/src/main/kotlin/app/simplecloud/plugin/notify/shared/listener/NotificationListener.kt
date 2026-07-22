package app.simplecloud.plugin.notify.shared.listener

import app.simplecloud.api.CloudApi
import app.simplecloud.api.event.Subscription
import app.simplecloud.api.server.ServerState
import app.simplecloud.plugin.notify.shared.command.CloudSender
import app.simplecloud.plugin.notify.shared.notification.Notification
import app.simplecloud.plugin.notify.shared.config.MessageConfig
import app.simplecloud.plugin.notify.shared.notification.NotificationRenderer
import app.simplecloud.plugin.notify.shared.notification.NotificationSettings
import app.simplecloud.plugin.notify.shared.utilities.NotifyPermissions
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import java.util.concurrent.ConcurrentHashMap

class NotificationListener(
    private val api: CloudApi,
    private val config: () -> MessageConfig,
    private val recipients: () -> Iterable<CloudSender>,
    private val renderer: NotificationRenderer,
    private val settings: NotificationSettings
) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val subscriptions: MutableList<Subscription> = mutableListOf()
    private val lastSentState: MutableMap<String, ServerState> = ConcurrentHashMap()

    fun start() {
        subscriptions.add(
            api.event().server().onStateChanged { event ->
                if (event.oldState == event.newState) {
                    return@onStateChanged
                }

                val server = event.server
                val state = event.newState

                scope.launch {
                    val message = config().notifications.server?.getByState(state) ?: return@launch
                    if (lastSentState.put(server.serverId, state) == state) return@launch

                    broadcast(
                        Notification(
                            renderer.server(server, message, Placeholder.unparsed("state", state.name)),
                            setOf(NotifyPermissions.serverStatePermission(state))
                        )
                    )
                }
            }
        )

        subscriptions.add(
            api.event().server().onStopped { event ->
                val server = event.server
                lastSentState.remove(server.serverId)

                scope.launch {
                    val message = config().notifications.server?.stopped ?: return@launch
                    broadcast(
                        Notification(
                            renderer.server(server, message, Placeholder.unparsed("state", "STOPPED")),
                            NotifyPermissions.SERVER_STOPPED_ALIASES
                        )
                    )
                }
            }
        )

        subscriptions.add(
            api.event().group().onCreated { event ->
                val serverGroupId = event.serverGroupId

                scope.launch {
                    val message = config().notifications.group?.created ?: return@launch
                    val name = runCatching { api.group().getGroupById(serverGroupId).await() }
                        .getOrNull()?.name ?: serverGroupId

                    broadcast(
                        Notification(
                            renderer.group(message, name),
                            NotifyPermissions.GROUP_CREATED
                        )
                    )
                }
            }
        )

        subscriptions.add(
            api.event().group().onDeleted { event ->
                val name = event.name

                scope.launch {
                    val message = config().notifications.group?.deleted ?: return@launch
                    broadcast(
                        Notification(
                            renderer.group(message, name),
                            NotifyPermissions.GROUP_DELETED
                        )
                    )
                }
            }
        )
    }

    fun stop() {
        subscriptions.forEach { it.close() }
        subscriptions.clear()
        lastSentState.clear()
        scope.cancel()
    }

    private fun broadcast(notification: Notification) {
        recipients()
            .filter { sender -> sender.uniqueId?.let(settings::isEnabled) ?: true }
            .forEach(notification::sendTo)
    }

}
