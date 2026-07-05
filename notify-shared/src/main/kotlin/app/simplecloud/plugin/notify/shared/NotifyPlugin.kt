package app.simplecloud.plugin.notify.shared

import app.simplecloud.api.CloudApi
import app.simplecloud.api.server.Server
import app.simplecloud.api.server.ServerState
import app.simplecloud.plugin.api.shared.config.ConfigurationFactory
import app.simplecloud.plugin.notify.shared.config.MessageConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import java.nio.file.Path
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class NotifyPlugin(
    dataDirectory: Path,
) {
    val config = ConfigurationFactory(
        dataDirectory.resolve("messages.yml").toFile(),
        MessageConfig::class.java,
    )

    private val lastSentState: MutableMap<String, ServerState> = mutableMapOf()

    lateinit var listeningFunction: (message: Component, permission: String) -> Unit

    private val cloudApi: CloudApi = CloudApi.create()

    private val zone: ZoneId = ZoneId.systemDefault()
    private val formatter: DateTimeFormatter

    val scope = CoroutineScope(Dispatchers.IO)

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
                val serverKey = server.serverId

                if (lastSentState[serverKey] == serverState) return@launch
                lastSentState[serverKey] = serverState

                var message = config.get().notifications.server?.getByState(serverState) ?: return@launch

                message = replaceHover(message, group = false)

                val parsed = generateMessageForServer(serverState, server, message)
                val permission = "notify.state.server.${serverState.name.lowercase()}"

                listeningFunction(parsed, permission)
            }
        }

        cloudApi.event().server().onStopped { event ->
            scope.launch {
                var message = config.get().notifications.server?.stopped ?: return@launch

                message = replaceHover(message, group = false)

                val parsed = generateMessageForServerBase(
                    event.server,
                    message,
                    Placeholder.parsed("state", "STOPPED")
                )
                val permission = "notify.state.server.deleted"

                listeningFunction(parsed, permission)
            }
        }

        cloudApi.event().group().onCreated { event ->
            val group = event.group

            scope.launch {
                var message = config.get().notifications.group?.created ?: return@launch

                message = replaceHover(message, group = true)

                val parsed = parseMessage(
                    message,
                    Placeholder.parsed("group", group.name ?: "N/A"),
                    Placeholder.parsed("time", formatTimestamp(Instant.now()))
                )
                val permission = "notify.state.group.created"

                listeningFunction(parsed, permission)
            }
        }

        cloudApi.event().group().onDeleted { event ->
            scope.launch {
                var message = config.get().notifications.group?.deleted ?: return@launch

                message = replaceHover(message, group = true)

                val parsed = parseMessage(
                    message,
                    Placeholder.parsed("group", event.name ?: "N/A"),
                    Placeholder.parsed("time", formatTimestamp(Instant.now()))
                )
                val permission = "notify.state.group.deleted"

                listeningFunction(parsed, permission)
            }
        }
    }

    private fun replaceHover(message: String, group: Boolean): String {
        val placeholderName = if (group) "notifications.hover.group" else "notifications.hover.server"
        val hoverMessage = if (group) config.get().notifications.hover.group else config.get().notifications.hover.server

        return message.replace("<hover:show_text:'<$placeholderName>'>", "<hover:show_text:'$hoverMessage'>")
    }

    private fun generateMessageForServer(serverState: ServerState, server: Server, message: String): Component {
        return generateMessageForServerBase(
            server,
            message,
            Placeholder.parsed("state", serverState.name)
        )
    }

    private fun generateMessageForServerBase(server: Server, message: String, vararg tagResolver: TagResolver): Component {
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

}
