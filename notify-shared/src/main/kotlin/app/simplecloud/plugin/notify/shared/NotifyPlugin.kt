package app.simplecloud.plugin.notify.shared

import app.simplecloud.api.CloudApi
import app.simplecloud.api.server.Server
import app.simplecloud.api.server.ServerState
import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.plugin.notify.shared.config.MessageConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import java.nio.file.Path
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class NotifyPlugin(
    dataDirectory: Path,
    private val recipients: () -> Iterable<CloudSender>,
) {
    private val serverHoverPlaceholder = "notifications.hover.server"
    private val groupHoverPlaceholder = "notifications.hover.group"

    private val serverStatePermissionPrefix = "notify.state.server"
    private val serverStoppedPermission = "$serverStatePermissionPrefix.stopped"
    private val legacyServerStoppedPermission = "$serverStatePermissionPrefix.deleted"
    private val serverStoppedPermissions = setOf(serverStoppedPermission, legacyServerStoppedPermission)

    private val groupCreatedPermission = "notify.state.group.created"
    private val groupDeletedPermission = "notify.state.group.deleted"

    val config = ConfigurationFactory(
        dataDirectory.resolve("messages.yml").toFile(),
        MessageConfig::class.java,
    )

    private val lastSentState: MutableMap<String, ServerState> = ConcurrentHashMap()

    private val cloudApi: CloudApi = CloudApi.create()

    private val zone: ZoneId = ZoneId.systemDefault()
    private val formatter: DateTimeFormatter

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        val newConf = config.loadOrCreate(MessageConfig())
        formatter = DateTimeFormatter.ofPattern(newConf.format.date, Locale.getDefault())
            .withZone(zone)

        cloudApi.event().server().onStateChanged { event ->
            if (event.oldState == event.newState) {
                return@onStateChanged
            }

            val serverState = event.newState
            val server = event.server

            scope.launch {
                val message = config.get().notifications.server?.getByState(serverState) ?: return@launch

                if (!markStateSent(server, serverState)) return@launch

                sendServerNotification(
                    server,
                    message,
                    setOf(serverStatePermission(serverState)),
                    Placeholder.parsed("state", serverState.name)
                )
            }
        }

        cloudApi.event().server().onStopped { event ->
            scope.launch {
                val message = config.get().notifications.server?.stopped ?: return@launch

                sendServerNotification(
                    event.server,
                    message,
                    serverStoppedPermissions,
                    Placeholder.parsed("state", "STOPPED")
                )
            }
        }

        cloudApi.event().group().onCreated { event ->
            val group = event.group

            scope.launch {
                val message = config.get().notifications.group?.created ?: return@launch

                sendGroupNotification(
                    message,
                    group.name,
                    groupCreatedPermission
                )
            }
        }

        cloudApi.event().group().onDeleted { event ->
            scope.launch {
                val message = config.get().notifications.group?.deleted ?: return@launch

                sendGroupNotification(
                    message,
                    event.name,
                    groupDeletedPermission
                )
            }
        }
    }

    fun close() {
        scope.cancel()
    }

    private fun markStateSent(server: Server, serverState: ServerState): Boolean {
        return lastSentState.put(server.serverId, serverState) != serverState
    }

    private fun sendServerNotification(
        server: Server,
        message: String,
        permissions: Set<String>,
        vararg tagResolver: TagResolver,
    ) {
        val parsed = parseServerMessage(
            server,
            replaceServerHover(message),
            *tagResolver
        )

        broadcast(Notification(parsed, permissions))
    }

    private fun sendGroupNotification(message: String, groupName: String?, permission: String) {
        val parsed = parseMessage(
            replaceGroupHover(message),
            Placeholder.parsed("group", groupName ?: "N/A"),
            Placeholder.parsed("time", formatTimestamp(Instant.now()))
        )

        broadcast(Notification(parsed, permission))
    }

    private fun broadcast(notification: Notification) {
        recipients().forEach(notification::sendTo)
    }

    private fun replaceServerHover(message: String): String {
        return replaceHover(
            message,
            serverHoverPlaceholder,
            config.get().notifications.hover.server
        )
    }

    private fun replaceGroupHover(message: String): String {
        return replaceHover(
            message,
            groupHoverPlaceholder,
            config.get().notifications.hover.group
        )
    }

    private fun replaceHover(message: String, placeholder: String, hoverMessage: String): String {
        return message.replace(
            "<hover:show_text:'<$placeholder>'>",
            "<hover:show_text:'$hoverMessage'>"
        )
    }

    private fun parseServerMessage(server: Server, message: String, vararg tagResolver: TagResolver): Component {
        return parseMessage(
            message,
            Placeholder.parsed("ip", server.ip ?: "N/A"),
            Placeholder.parsed("port", server.port.toString()),
            Placeholder.parsed("group", server.serverBase.name ?: "N/A"),
            Placeholder.parsed("server", server.serverBase.name ?: "N/A"),
            Placeholder.parsed("uuid", server.serverId),
            Placeholder.parsed("id", server.numericalId.takeIf { it != -1 }?.let { " $it" } ?: ""),
            Placeholder.parsed("create_date", formatTimestamp(runCatching { server.createdAt }.getOrNull())),
            Placeholder.parsed("time", formatTimestamp(Instant.now())),
            Placeholder.parsed("updated", formatTimestamp(runCatching { server.updatedAt }.getOrNull())),
            Placeholder.parsed("players", server.playerCount.toString()),
            Placeholder.parsed("max", server.maxPlayers.toString()),
            *tagResolver
        )
    }

    private fun parseMessage(message: String, vararg tagResolver: TagResolver): Component {
        return config.get().msg(message, *tagResolver)
    }

    private fun formatTimestamp(value: Any?): String {
        if (value == null) return "N/A"

        val instant = when (value) {
            is Instant -> value
            is Date -> value.toInstant()
            is Long -> Instant.ofEpochMilli(value)
            is String -> runCatching { Instant.parse(value) }.getOrNull()
            else -> null
        } ?: return "N/A"

        return formatter.format(instant)
    }

    private fun serverStatePermission(state: ServerState): String {
        return "$serverStatePermissionPrefix.${state.name.lowercase()}"
    }

}
