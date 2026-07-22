package app.simplecloud.plugin.notify.shared.command

import app.simplecloud.api.player.CloudPlayer
import app.simplecloud.api.player.PlayerApi
import app.simplecloud.plugin.notify.shared.NotifyPlugin
import app.simplecloud.plugin.notify.shared.config.MessageConfig
import app.simplecloud.plugin.notify.shared.notification.NotificationRenderer
import app.simplecloud.plugin.notify.shared.notification.NotificationSettings
import app.simplecloud.plugin.notify.shared.utilities.NotifyPermissions
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.incendo.cloud.CommandManager
import org.incendo.cloud.parser.standard.BooleanParser
import org.incendo.cloud.parser.standard.StringParser

class NotifyCommand<C : CloudSender>(
    private val manager: CommandManager<C>,
    private val notify: NotifyPlugin,
    private val renderer: NotificationRenderer,
    private val settings: NotificationSettings,
    private val api: PlayerApi,
) {

    private val config: MessageConfig
        get() = notify.config.get()

    private val commands = linkedMapOf(
        "/scnotify help" to NotifyPermissions.COMMAND_HELP,
        "/scnotify reload config" to NotifyPermissions.COMMAND_RELOAD_CONFIG,
        "/scnotify <enable/disable>" to NotifyPermissions.COMMAND_TOGGLE_SELF,
        "/scnotify set <player> <true/false>" to NotifyPermissions.COMMAND_TOGGLE_OTHER,
    )

    fun register() {
        registerHelp()
        registerReload()
        registerToggleSelf()
        registerToggleOther()
    }

    private fun registerHelp() {
        manager.command(
            manager.commandBuilder("scnotify")
                .permission(NotifyPermissions.COMMAND_HELP)
                .handler { context -> sendHelp(context.sender()) }
        )

        manager.command(
            manager.commandBuilder("scnotify")
                .literal("help")
                .permission(NotifyPermissions.COMMAND_HELP)
                .handler { context -> sendHelp(context.sender()) }
        )
    }

    private fun sendHelp(sender: C) {
        val usable = commands.filterValues { sender.hasPermission(it) }.keys

        if (usable.isEmpty()) {
            sender.sendMessage(renderer.message(config.command.help.empty))
            return
        }

        sender.sendMessage(renderer.message(config.command.help.title))
        usable.forEach { usage ->
            sender.sendMessage(renderer.message(config.command.help.entry, Placeholder.unparsed("command", usage)))
        }
    }

    private fun registerReload() {
        manager.command(
            manager.commandBuilder("scnotify")
                .literal("reload").literal("config")
                .permission(NotifyPermissions.COMMAND_RELOAD_CONFIG)
                .handler { context ->
                    val sender = context.sender()

                    if (notify.reload()) {
                        sender.sendMessage(renderer.message(config.command.reload.config.success))
                    } else {
                        sender.sendMessage(renderer.message(config.command.reload.config.failed))
                    }
                }
        )
    }

    private fun registerToggleSelf() {
        manager.command(
            manager.commandBuilder("scnotify")
                .literal("enable")
                .permission(NotifyPermissions.COMMAND_TOGGLE_SELF)
                .handler { context -> toggleSelf(context.sender(), true) }
        )

        manager.command(
            manager.commandBuilder("scnotify")
                .literal("disable")
                .permission(NotifyPermissions.COMMAND_TOGGLE_SELF)
                .handler { context -> toggleSelf(context.sender(), false) }
        )
    }

    private fun toggleSelf(sender: C, value: Boolean) {
        val id = sender.uniqueId

        if (id == null) {
            sender.sendMessage(renderer.message(config.command.error.onlyPlayers))
            return
        }

        val messages = config.command.notify

        if (settings.isEnabled(id) == value) {
            sender.sendMessage(renderer.message(if (value) messages.alreadyEnabled else messages.alreadyDisabled))
            return
        }

        settings.set(id, value).whenComplete { _, error ->
            if (error != null) {
                sender.sendMessage(renderer.message(config.command.error.storageUnavailable))
                return@whenComplete
            }

            sender.sendMessage(renderer.message(if (value) messages.enabled else messages.disabled))
        }
    }

    private fun registerToggleOther() {
        manager.command(
            manager.commandBuilder("scnotify")
                .literal("set")
                .required("player", StringParser.stringParser())
                .required("value", BooleanParser.booleanParser())
                .permission(NotifyPermissions.COMMAND_TOGGLE_OTHER)
                .handler { context ->
                    val sender = context.sender()
                    val player = context.get<String>("player")
                    val value = context.get<Boolean>("value")

                    api.get(player).whenComplete { target, error ->
                        if (error != null) {
                            sender.sendMessage(renderer.message(config.command.error.storageUnavailable))
                            return@whenComplete
                        }

                        if (target == null) {
                            sender.sendMessage(playerError(config.command.error.playerNotFound, player))
                            return@whenComplete
                        }

                        if (!target.isOnline) {
                            sender.sendMessage(playerError(config.command.error.playerNotOnline, player))
                            return@whenComplete
                        }

                        applyToTarget(sender, target, value)
                    }
                }
        )
    }

    private fun applyToTarget(sender: C, target: CloudPlayer, value: Boolean) {
        if (settings.isEnabled(target.uniqueId) == value) {
            sender.sendMessage(targetMessage(if (value) config.command.set.alreadyEnabled else config.command.set.alreadyDisabled, target))
            return
        }

        settings.set(target.uniqueId, value).whenComplete { _, error ->
            if (error != null) {
                sender.sendMessage(renderer.message(config.command.error.storageUnavailable))
                return@whenComplete
            }

            sender.sendMessage(targetMessage(if (value) config.command.set.enabled else config.command.set.disabled, target))
            target.sendMessage(renderer.message(if (value) config.command.notify.enabled else config.command.notify.disabled))
        }
    }

    private fun targetMessage(message: String, target: CloudPlayer) = renderer.message(
        message,
        // Must stay parsed: <playername> is consumed as an argument of the <head:...> tag.
        Placeholder.parsed("playername", target.name),
        Placeholder.unparsed("displayname", target.displayName ?: target.name)
    )

    private fun playerError(message: String, playerName: String) = renderer.message(
        message,
        Placeholder.unparsed("playername", playerName)
    )

}
