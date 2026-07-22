package app.simplecloud.plugin.notify.shared.utilities

import app.simplecloud.api.server.ServerState

object NotifyPermissions {

    const val SERVER_STATE_PREFIX = "notify.state.server"
    const val SERVER_STOPPED = "$SERVER_STATE_PREFIX.stopped"
    const val LEGACY_SERVER_STOPPED = "$SERVER_STATE_PREFIX.deleted"

    const val GROUP_CREATED = "notify.state.group.created"
    const val GROUP_DELETED = "notify.state.group.deleted"

    const val COMMAND_HELP = "simplecloud.command.notify.help"
    const val COMMAND_RELOAD_CONFIG = "simplecloud.command.notify.reload.config"
    const val COMMAND_TOGGLE_SELF = "simplecloud.command.notify.toggle.self"
    const val COMMAND_TOGGLE_OTHER = "simplecloud.command.notify.toggle.other"

    val SERVER_STOPPED_ALIASES = setOf(SERVER_STOPPED, LEGACY_SERVER_STOPPED)

    fun serverStatePermission(state: ServerState): String {
        return "$SERVER_STATE_PREFIX.${state.name.lowercase()}"
    }

}
