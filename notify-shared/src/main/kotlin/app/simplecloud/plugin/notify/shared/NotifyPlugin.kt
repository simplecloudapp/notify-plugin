package app.simplecloud.plugin.notify.shared

import app.simplecloud.api.CloudApi
import app.simplecloud.api.server.Server
import app.simplecloud.api.server.ServerState
import app.simplecloud.plugin.notify.shared.config.Config
import app.simplecloud.plugin.notify.shared.config.ConfigFactory
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class NotifyPlugin(
    dataDirectory: Path,
) {

    private var config: Config = ConfigFactory.loadOrCreate(dataDirectory) { newConfig ->
        dateFormat.applyPattern(newConfig.dateFormat)
        serverStateFilter = newConfig.serverStateFilter
    }
    private val dateFormat = SimpleDateFormat(config.dateFormat)
    private var serverStateFilter = config.serverStateFilter

    private val lastSentState: MutableMap<String, ServerState> = mutableMapOf()

    lateinit var listeningFunction: (message: Component, permission: String) -> Unit

    private val cloudApi: CloudApi = CloudApi.create()

    private val zone: ZoneId = ZoneId.systemDefault()
    private val formatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern(config.dateFormat, Locale.getDefault())
            .withZone(zone)

    init {
        cloudApi.event().server().onStateChanged { event ->
            if (event.oldState == event.newState) {
                return@onStateChanged
            }

            val serverState = event.newState
            handleUpdate(serverState, event.server)
        }
    }

    private fun handleUpdate(serverState: ServerState, server: Server) {
        val serverKey = server.serverId

        if (lastSentState[serverKey] == serverState) return
        lastSentState[serverKey] = serverState

        val filter = serverStateFilter.filter { it.serverState == serverState }
        if (filter.isEmpty()) return

        filter.forEach {
            val message = generateMessage(serverState, server, it.message)
            listeningFunction(message, it.permission)
        }
    }

    private fun generateMessage(serverState: ServerState, server: Server, message: String): Component {
        val createdAt = runCatching { server.createdAt }.getOrNull()
        val updatedAt = runCatching { server.updatedAt }.getOrNull()

        return miniMessage(
            message,
            Placeholder.parsed("server_ip", server.ip ?: "N/A"),
            Placeholder.parsed("server_port", server.port.toString()),
            Placeholder.parsed("server_group", server.serverBase.name ?: "N/A"),
            Placeholder.parsed("server_name", server.serverBase.name ?: "N/A"),
            Placeholder.parsed("server_uuid", server.serverId),
            Placeholder.parsed("server_id", server.numericalId.takeIf { it != -1 }?.let { " $it" } ?: ""),
            Placeholder.parsed("server_create_date", formatTimestamp(runCatching { server.createdAt }.getOrNull())),
            Placeholder.parsed("server_update_date", formatTimestamp(runCatching { server.updatedAt }.getOrNull())),
            Placeholder.parsed("online_players", server.playerCount.toString()),
            Placeholder.parsed("max_players", server.maxPlayers.toString()),
            Placeholder.parsed("server_state", serverState.name)
        )
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
