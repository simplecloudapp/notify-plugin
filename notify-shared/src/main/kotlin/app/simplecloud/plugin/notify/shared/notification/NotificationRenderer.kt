package app.simplecloud.plugin.notify.shared.notification

import app.simplecloud.api.server.Server
import app.simplecloud.plugin.notify.shared.config.MessageConfig
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class NotificationRenderer(
    private val config: () -> MessageConfig,
) {

    private val serverHoverPlaceholder = "notifications.hover.server"
    private val groupHoverPlaceholder = "notifications.hover.group"

    private val zone: ZoneId = ZoneId.systemDefault()

    private val formatter: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern(config().format.date, Locale.getDefault()).withZone(zone)

    fun server(server: Server, message: String, vararg tagResolver: TagResolver): Component {
        return parse(
            replaceHover(message, serverHoverPlaceholder, config().notifications.hover.server),
            Placeholder.unparsed("ip", server.ip ?: "N/A"),
            Placeholder.unparsed("port", server.port.toString()),
            Placeholder.unparsed("group", server.serverBase.name ?: "N/A"),
            Placeholder.unparsed("server", server.serverBase.name ?: "N/A"),
            Placeholder.unparsed("uuid", server.serverId),
            Placeholder.unparsed("id", server.numericalId.takeIf { it != -1 }?.let { " $it" } ?: ""),
            Placeholder.unparsed("create_date", formatTimestamp(runCatching { server.createdAt }.getOrNull())),
            Placeholder.unparsed("time", formatTimestamp(Instant.now())),
            Placeholder.unparsed("updated", formatTimestamp(runCatching { server.updatedAt }.getOrNull())),
            Placeholder.unparsed("players", server.playerCount.toString()),
            Placeholder.unparsed("max", server.maxPlayers.toString()),
            *tagResolver
        )
    }

    fun group(message: String, groupName: String?): Component {
        return parse(
            replaceHover(message, groupHoverPlaceholder, config().notifications.hover.group),
            Placeholder.unparsed("group", groupName ?: "N/A"),
            Placeholder.unparsed("time", formatTimestamp(Instant.now()))
        )
    }

    fun message(message: String, vararg tagResolver: TagResolver): Component {
        return parse(message, *tagResolver)
    }

    private fun parse(message: String, vararg tagResolver: TagResolver): Component {
        return config().msg(message, *tagResolver)
    }

    private fun replaceHover(message: String, placeholder: String, hoverMessage: String): String {
        return message.replace(
            "<hover:show_text:'<$placeholder>'>",
            "<hover:show_text:'$hoverMessage'>"
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