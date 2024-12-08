package app.simplecloud.plugin.notify.shared

import app.simplecloud.controller.shared.time.ProtoBufTimestamp
import app.simplecloud.plugin.notify.shared.config.Config
import app.simplecloud.plugin.notify.shared.config.ConfigFactory
import app.simplecloud.pubsub.PubSubClient
import build.buf.gen.simplecloud.controller.v1.ServerUpdateEvent
import build.buf.gen.simplecloud.controller.v1.serverAfterOrNull
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

    fun onMessageAnnounce(function: (message: Component, permission: String?) -> Unit) {
        val pubSubClient = PubSubClient("127.0.0.1", 8080) //TODO: Replace IP and Port to let that work properly, not done yet because Philipp hasn't

        pubSubClient.subscribe("event", ServerUpdateEvent::class.java) { event ->

            val serverState = event.serverAfter.serverState

            val filter = config.serverStateFilter.filter { it.serverState == serverState }
            if (filter.isEmpty()) return@subscribe

            val serverAfter = event.serverAfterOrNull ?: return@subscribe

            fun timeStampToLong(timeStamp: Timestamp): Long {
                return ProtoBufTimestamp.toLocalDateTime(timeStamp).toInstant(OffsetDateTime.now().offset)
                    .toEpochMilli()
            }

            filter.forEach {
                val message = miniMessage(
                    it.message,
                    Placeholder.parsed("<server_ip>", serverAfter.serverIp ?: "N/A"),
                    Placeholder.parsed("<server_port>", serverAfter.serverPort.toString()),
                    Placeholder.parsed("<server_group>", serverAfter.groupName ?: "N/A"),

                    Placeholder.parsed("<server_uuid>", serverAfter.uniqueId),
                    Placeholder.parsed("<server_id>", serverAfter.numericalId.toString()),

                    Placeholder.parsed(
                        "<server_create_date>",
                        dateFormat.format(timeStampToLong(serverAfter.createdAt))
                    ),

                    Placeholder.parsed(
                        "<server_update_date>",
                        dateFormat.format(timeStampToLong(serverAfter.updatedAt))
                    ),

                    Placeholder.parsed("<online_players>", serverAfter.playerCount.toString()),
                    Placeholder.parsed("<max_players>", serverAfter.maxPlayers.toString()),
                    Placeholder.parsed("<server_state>", serverState.name)
                )

                function(message, it.permission)
            }

        }
    }
}