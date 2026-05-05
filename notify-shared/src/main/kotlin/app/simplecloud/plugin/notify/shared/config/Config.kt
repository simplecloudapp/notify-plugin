package app.simplecloud.plugin.notify.shared.config

import app.simplecloud.api.server.ServerState
import app.simplecloud.plugin.api.shared.config.AbstractMessageConfig
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class MessageConfig(
    @Setting("version")
    val version: String = "1",

    @Setting("format")
    val format: Format = Format(),

    @Setting("command")
    val command: Command = Command(),

    @Setting("notifications")
    val notifications: Notifications = Notifications(),

    override val variables: Map<String, String> = mapOf(
        "prefix" to "<color:#0EA5E9><bold>⚡</bold></color>"
    ),
) : AbstractMessageConfig()

@ConfigSerializable
data class Format(
    @Setting("date")
    val date: String = "dd.MM.yyyy HH:mm:ss"
)

@ConfigSerializable
data class Command(
    @Setting("help")
    val help: Help = Help(),

    @Setting("usage")
    val usage: Usage = Usage(),

    @Setting("permission")
    val permission: Permission = Permission(),

    @Setting("notify")
    val notify: Notify = Notify(),

    @Setting("set")
    val set: SetNotify = SetNotify(),

    @Setting("reload")
    val reload: Reload = Reload(),

    @Setting("error")
    val error: Error = Error()
)

@ConfigSerializable
data class Help(
    val title: String = "<prefix> <#0EA5E9>SimpleCloud Notify commands",
    val entry: String = "<#E2E8F0><command>",
    val empty: String = "<prefix> <#F59E0B>No notify commands are available for you."
)

@ConfigSerializable
data class Usage(
    val invalid: String = "<prefix> <#DC2626>Use <#F8FAFC><command> <#DC2626>instead.",
    val entry: String = "<#E2E8F0><command>",
    @Setting("invalid-state")
    val invalidState: String =
        "<prefix> <#DC2626>Use <#F8FAFC>enable <#DC2626>or <#F8FAFC>disable<#DC2626>.",
    @Setting("invalid-toggle-value")
    val invalidToggleValue: String =
        "<prefix> <#DC2626>Use <#F8FAFC>true <#DC2626>or <#F8FAFC>false<#DC2626>.",
    @Setting("missing-player")
    val missingPlayer: String =
        "<prefix> <#DC2626>Missing player <#F8FAFC><playername><#DC2626>."
)

@ConfigSerializable
data class Permission(
    val denied: String =
        "<prefix> <#DC2626>You do not have permission to use this command."
)

@ConfigSerializable
data class Notify(
    val enabled: String =
        "<prefix> <#A3E635>Server notifications are now <#F8FAFC>enabled<#A3E635>.",
    val disabled: String =
        "<prefix> <#A3E635>Server notifications are now <#F8FAFC>disabled<#A3E635>.",
    @Setting("already-enabled")
    val alreadyEnabled: String =
        "<prefix> <#F59E0B>Server notifications are already <#F8FAFC>enabled<#F59E0B>.",
    @Setting("already-disabled")
    val alreadyDisabled: String =
        "<prefix> <#F59E0B>Server notifications are already <#F8FAFC>disabled<#F59E0B>."
)

@ConfigSerializable
data class SetNotify(
    val enabled: String =
        "<prefix> <#A3E635>Server notifications are now <#F8FAFC>enabled <#A3E635>for <white><head:<playername>:true> <#F8FAFC><displayname><#A3E635>.",
    val disabled: String =
        "<prefix> <#A3E635>Server notifications are now <#F8FAFC>disabled <#A3E635>for <white><head:<playername>:true> <#F8FAFC><displayname><#A3E635>.",
    @Setting("already-enabled")
    val alreadyEnabled: String =
        "<prefix> <#F59E0B>Server notifications are already <#F8FAFC>enabled <#F59E0B>for <white><head:<playername>:true> <#F8FAFC><displayname><#F59E0B>.",
    @Setting("already-disabled")
    val alreadyDisabled: String =
        "<prefix> <#F59E0B>Server notifications are already <#F8FAFC>disabled <#F59E0B>for <white><head:<playername>:true> <#F8FAFC><displayname><#F59E0B>."
)

@ConfigSerializable
data class Reload(
    val config: ReloadConfig = ReloadConfig()
)

@ConfigSerializable
data class ReloadConfig(
    val success: String =
        "<prefix> <#A3E635>Notify configuration was reloaded.",
    val failed: String =
        "<prefix> <#DC2626>Notify configuration could not be reloaded."
)

@ConfigSerializable
data class Error(
    @Setting("player-not-found")
    val playerNotFound: String =
        "<prefix> <#DC2626>Player <#F8FAFC><playername> <#DC2626>was not found.",

    @Setting("player-not-online")
    val playerNotOnline: String =
        "<prefix> <#DC2626>Player <#F8FAFC><playername> <#DC2626>is not online.",

    @Setting("storage-unavailable")
    val storageUnavailable: String =
        "<prefix> <#DC2626>Notify settings are currently unavailable.",

    val internal: String =
        "<prefix> <#DC2626>An internal error occurred. Try again later."
)

@ConfigSerializable
data class Notifications(
    val hover: Hover = Hover(),
    val server: ServerNotifications? = ServerNotifications(),
    val group: GroupNotifications? = GroupNotifications()
)

@ConfigSerializable
data class Hover(
    val server: String = """
        <#E2E8F0>Server information
        <br><#94A3B8>Time<#475569>: <#E2E8F0><time>
        <br><#94A3B8>State<#475569>: <#E2E8F0><state>
        <br><#94A3B8>Address<#475569>: <#E2E8F0><ip>:<port>
        <br><#94A3B8>Players<#475569>: <#E2E8F0><players>/<max>
    """.trimIndent(),

    val group: String = """
        <#E2E8F0>Group information
        <br><#94A3B8>Time<#475569>: <#E2E8F0><time>
    """.trimIndent()
)

@ConfigSerializable
data class ServerNotifications(
    val starting: String? =
        "<prefix> <#0EA5E9>Server <hover:show_text:'<notifications.hover.server>'><#F8FAFC><server></hover> <#0EA5E9>is now <#F8FAFC>starting<#0EA5E9>.",
    val available: String? =
        "<prefix> <#A3E635>Server <hover:show_text:'<notifications.hover.server>'><#F8FAFC><server></hover> <#A3E635>is now <#F8FAFC>available<#A3E635>.",
    val stopping: String? =
        "<prefix> <#F59E0B>Server <hover:show_text:'<notifications.hover.server>'><#F8FAFC><server></hover> <#F59E0B>is now <#F8FAFC>stopping<#F59E0B>.",
    val stopped: String? =
        "<prefix> <#DC2626>Server <hover:show_text:'<notifications.hover.server>'><#F8FAFC><server></hover> <#DC2626>is now <#F8FAFC>stopped<#DC2626>."
) {
    fun getByState(state: ServerState): String? {
        return when (state) {
            ServerState.STARTING -> starting
            ServerState.AVAILABLE -> available
            ServerState.STOPPING -> stopping
            else -> null
        }
    }
}

@ConfigSerializable
data class GroupNotifications(
    val created: String? =
        "<prefix> <#0EA5E9>Group <hover:show_text:'<notifications.hover.group>'><#F8FAFC><group></hover> <#0EA5E9>was created.",
    val deleted: String? =
        "<prefix> <#DC2626>Group <hover:show_text:'<notifications.hover.group>'><#F8FAFC><group></hover> <#DC2626>was deleted."
)