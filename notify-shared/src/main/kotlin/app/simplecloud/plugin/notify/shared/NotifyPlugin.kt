package app.simplecloud.plugin.notify.shared

import app.simplecloud.droplet.api.auth.AuthCallCredentials
import app.simplecloud.droplet.api.time.ProtobufTimestamp
import app.simplecloud.plugin.notify.shared.config.Config
import app.simplecloud.plugin.notify.shared.config.ConfigFactory
import app.simplecloud.pubsub.PubSubClient
import build.buf.gen.simplecloud.controller.v1.*
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

    init {
        val pubSubClient = PubSubClient(
            System.getenv("CONTROLLER_PUBSUB_HOST"),
            System.getenv("CONTROLLER_PUBSUB_PORT").toInt(),
            AuthCallCredentials(System.getenv("CONTROLLER_SECRET"))
        )

        pubSubClient.subscribe("event", ServerUpdateEvent::class.java) { event ->
            val serverAfter = event.serverAfterOrNull ?: return@subscribe
            if (event.serverBeforeOrNull?.serverState == serverAfter.serverState) return@subscribe

            val serverState = serverAfter.serverState
            handleUpdate(serverState, serverAfter)
        }
    }

    private fun handleUpdate(serverState: ServerState, server: ServerDefinition) {
        val serverKey = server.uniqueId

        if (lastSentState[serverKey] == serverState) return
        lastSentState[serverKey] = serverState

        val filter = serverStateFilter.filter { it.serverState == serverState }
        if (filter.isEmpty()) return

        filter.forEach {
            val message = generateMessage(serverState, server, it.message)
            listeningFunction(message, it.permission)
        }
    }

    private fun generateMessage(
        serverState: ServerState,
        server: ServerDefinition,
        message: String
    ): Component {
        fun timeStampToLong(timeStamp: Timestamp): Long {
            return ProtobufTimestamp
                .toLocalDateTime(timeStamp)
                .toInstant(OffsetDateTime.now().offset)
                .toEpochMilli()
        }

        return miniMessage(
            message,
            Placeholder.parsed("server_ip", server.serverIp ?: "N/A"),
            Placeholder.parsed("server_port", server.serverPort.toString()),
            Placeholder.parsed("server_group", server.groupName ?: "N/A"),

            Placeholder.parsed("server_uuid", server.uniqueId),
            Placeholder.parsed("server_id", server.numericalId.toString()),

            Placeholder.parsed(
                "server_create_date",
                dateFormat.format(timeStampToLong(server.createdAt))
            ),

            Placeholder.parsed(
                "server_update_date",
                dateFormat.format(timeStampToLong(server.updatedAt))
            ),

            Placeholder.parsed("online_players", server.playerCount.toString()),
            Placeholder.parsed("max_players", server.maxPlayers.toString()),
            Placeholder.parsed("server_state", serverState.name)
        )
    }
}
