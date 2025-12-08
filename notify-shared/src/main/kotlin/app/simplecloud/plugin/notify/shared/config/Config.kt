package app.simplecloud.plugin.notify.shared.config

import app.simplecloud.api.server.ServerState
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@ConfigSerializable
data class Config(
    @Comment("The date format pattern the notify plugin uses.")
    val dateFormat: String = "dd.MM.yyyy HH:mm:ss",

    @Comment(
        "Here you can define which server state changes should be announced.\nThe following placeholders can be used:\n" +
                "<server_ip> - The IP of the server\n" +
                "<server_port> - The port of the server\n" +
                "<server_group> - The group the server belongs to\n" +
                "<server_uuid> - The UUID of the server\n" +
                "<server_id> - The numerical ID of the server\n" +
                "<server_create_date> - The initial creation date of the server\n" +
                "<server_update_date> - The last update date of the server\n" +
                "<server_state> - The new state of the server"
    )
    val serverStateFilter: List<ServerStateChangedFilterEntry> = listOf(
        ServerStateChangedFilterEntry(
            ServerState.STARTING,
            "<color:#38bdf8><bold>⚡</bold></color> <hover:show_text:'<color:#38bdf8><bold>⚡</bold></color> Information of <server_group> <server_id>\n" +
                    "   <color:#a3a3a3>Timestamp:</color> <color:#38bdf8><server_update_date></color>\n" +
                    "   <color:#a3a3a3>State:</color> <color:#38bdf8><server_state></color>\n" +
                    "   <color:#a3a3a3>Server-IP:</color> <color:#38bdf8><server_ip></color>\n" +
                    "   <color:#a3a3a3>Port:</color> <color:#38bdf8><server_port></color>\n" +
                    "   <color:#a3a3a3>Players:</color> <color:#38bdf8><online_players>/<max_players></color>'>" +
                    "<color:white>Server <server_group> <server_id></hover> is now <color:#fbbf24>starting</color>."
        ),

        ServerStateChangedFilterEntry(
            ServerState.AVAILABLE,
            "<color:#38bdf8><bold>⚡</bold></color> <hover:show_text:'<color:#38bdf8><bold>⚡</bold></color> Information of <server_group> <server_id>\n" +
                    "   <color:#a3a3a3>Timestamp:</color> <color:#38bdf8><server_update_date></color>\n" +
                    "   <color:#a3a3a3>State:</color> <color:#38bdf8><server_state></color>\n" +
                    "   <color:#a3a3a3>Server-IP:</color> <color:#38bdf8><server_ip></color>\n" +
                    "   <color:#a3a3a3>Port:</color> <color:#38bdf8><server_port></color>\n" +
                    "   <color:#a3a3a3>Players:</color> <color:#38bdf8><online_players>/<max_players></color>'>" +
                    "<color:white>Server <server_group> <server_id></hover> is now <color:#a3e635>online</color>."
        ),

        ServerStateChangedFilterEntry(
            ServerState.STOPPING,
            "<color:#38bdf8><bold>⚡</bold></color> <hover:show_text:'<color:#38bdf8><bold>⚡</bold></color> Information of <server_group> <server_id>\n" +
                    "   <color:#a3a3a3>Timestamp:</color> <color:#38bdf8><server_update_date></color>\n" +
                    "   <color:#a3a3a3>State:</color> <color:#38bdf8><server_state></color>\n" +
                    "   <color:#a3a3a3>Server-IP:</color> <color:#38bdf8><server_ip></color>\n" +
                    "   <color:#a3a3a3>Port:</color> <color:#38bdf8><server_port></color>\n" +
                    "   <color:#a3a3a3>Players:</color> <color:#38bdf8><online_players>/<max_players></color>'>" +
                    "<color:white>Server <server_group> <server_id></hover> is now <color:#dc2626>stopping</color>."
        ),
    )
)