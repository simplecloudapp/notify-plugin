package app.simplecloud.plugin.notify.shared

import app.simplecloud.api.CloudApi
import app.simplecloud.api.server.Server
import app.simplecloud.api.server.ServerState
import app.simplecloud.plugin.notify.shared.config.Config
import app.simplecloud.plugin.notify.shared.config.ConfigFactory
import com.google.protobuf.Timestamp
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.time.OffsetDateTime

class NotifyPlugin(
    dataDirectory: Path
) {

    private val config: Config = ConfigFactory.loadOrCreate(dataDirectory)
    private val dateFormat = SimpleDateFormat(config.dateFormat)
    private val serverStateFilter = config.serverStateFilter

    private val lastSentState: MutableMap<String, ServerState> = mutableMapOf()

    lateinit var listeningFunction: (message: Component, permission: String) -> Unit

    private val cloudApi: CloudApi = CloudApi.create()

    init {
        cloudApi.event().server().onStateChanged { event ->
            val serverAfter = event.server ?: return@onStateChanged

            if (event.server?.state == serverAfter.state) {
                return@onStateChanged
            }

            val serverState = serverAfter.state
            handleUpdate(serverState, serverAfter)
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
        return miniMessage(
            message,
            Placeholder.parsed("server_ip", server.ip ?: "N/A"),
            Placeholder.parsed("server_port", server.port.toString()),
            Placeholder.parsed("server_group", server.group.name ?: "N/A"),

            Placeholder.parsed("server_uuid", server.serverId),
            Placeholder.parsed("server_id", server.numericalId.toString()),

            Placeholder.parsed(
                "server_create_date",
                dateFormat.format(server.createdAt)
            ),

            Placeholder.parsed(
                "server_update_date",
                dateFormat.format(server.updatedAt)
            ),

            Placeholder.parsed("online_players", server.playerCount.toString()),
            Placeholder.parsed("max_players", server.maxPlayers.toString()),
            Placeholder.parsed("server_state", serverState.name)
        )
    }
}
