package app.simplecloud.plugin.notify.shared.command

import app.simplecloud.api.player.CloudPlayer
import app.simplecloud.plugin.notify.shared.NotifyPlugin
import app.simplecloud.plugin.notify.shared.utilities.NotifyPermissions
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.incendo.cloud.CommandManager
import org.incendo.cloud.parser.standard.BooleanParser
import org.incendo.cloud.parser.standard.StringParser
import org.slf4j.LoggerFactory
import java.util.UUID

class NotifyCommand<C : CloudSender>(
    private val manager: CommandManager<C>,
    private val notify: NotifyPlugin
) {

    private val logger = LoggerFactory.getLogger(NotifyCommand::class.java)
    private val messages get() = notify.messages.get()
    private val renderer = notify.renderer
    private val settings = notify.settings

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
        val root = manager.commandBuilder("scnotify").permission(NotifyPermissions.COMMAND_HELP)
        manager.command(root.handler { context -> sendHelp(context.sender()) })
        manager.command(root.literal("help").handler { context -> sendHelp(context.sender()) })
    }

    private fun registerReload() {
        manager.command(
            manager.commandBuilder("scnotify")
                .literal("reload").literal("config")
                .permission(NotifyPermissions.COMMAND_RELOAD_CONFIG)
                .handler { context ->
                    val message = if (notify.reload()) messages.command.reload.config.success else messages.command.reload.config.failed
                    context.sender().sendMessage(renderer.message(message))
                }
        )
    }

    private fun registerToggleSelf() {
        val root = manager.commandBuilder("scnotify").permission(NotifyPermissions.COMMAND_TOGGLE_SELF)
        manager.command(root.literal("enable").handler { context -> toggleSelf(context.sender(), true) })
        manager.command(root.literal("disable").handler { context -> toggleSelf(context.sender(), false) })
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

                    notify.scope.launch {
                        toggleOther(sender, player, value)
                    }
                }
        )
    }

    private fun sendHelp(sender: C) {
        val usable = commands.filterValues { permission -> sender.hasPermission(permission) }.keys

        if (usable.isEmpty()) {
            sender.sendMessage(renderer.message(messages.command.help.empty))
            return
        }

        sender.sendMessage(renderer.message(messages.command.help.title))
        usable.forEach { usage ->
            sender.sendMessage(renderer.message(messages.command.help.entry, Placeholder.unparsed("command", usage)))
        }
    }

    private fun toggleSelf(sender: C, value: Boolean) {
        val id = sender.getUniqueId()

        if (id == null) {
            sender.sendMessage(renderer.message(messages.command.error.onlyPlayers))
            return
        }

        val notifyMessages = messages.command.notify

        if (settings.isEnabled(id) == value) {
            sender.sendMessage(renderer.message(if (value) notifyMessages.alreadyEnabled else notifyMessages.alreadyDisabled))
            return
        }

        notify.scope.launch {
            if (!updateSetting(sender, id, value)) return@launch
            sender.sendMessage(renderer.message(if (value) notifyMessages.enabled else notifyMessages.disabled))
        }
    }

    private suspend fun toggleOther(sender: C, playerName: String, value: Boolean) {
        val target = try {
            notify.api.player().get(playerName).await()
        } catch (e: Exception) {
            logger.error("Failed to load player '$playerName'", e)
            sender.sendMessage(renderer.message(messages.command.error.storageUnavailable))
            return
        }

        if (target == null) {
            sender.sendMessage(playerError(messages.command.error.playerNotFound, playerName))
            return
        }

        if (!target.isOnline) {
            sender.sendMessage(playerError(messages.command.error.playerNotOnline, playerName))
            return
        }

        val setMessages = messages.command.set

        if (settings.isEnabled(target.uniqueId) == value) {
            sender.sendMessage(targetMessage(if (value) setMessages.alreadyEnabled else setMessages.alreadyDisabled, target))
            return
        }

        if (!updateSetting(sender, target.uniqueId, value)) return

        sender.sendMessage(targetMessage(if (value) setMessages.enabled else setMessages.disabled, target))
        target.sendMessage(renderer.message(if (value) messages.command.notify.enabled else messages.command.notify.disabled))
    }

    private suspend fun updateSetting(sender: C, id: UUID, value: Boolean): Boolean {
        try {
            settings.set(id, value)
            return true
        } catch (e: Exception) {
            logger.error("Failed to update the notify setting of '$id'", e)
            sender.sendMessage(renderer.message(messages.command.error.storageUnavailable))
            return false
        }
    }

    private fun targetMessage(message: String, target: CloudPlayer) = renderer.message(
        message,
        Placeholder.parsed("playername", target.name),
        Placeholder.unparsed("displayname", target.displayName ?: target.name)
    )

    private fun playerError(message: String, playerName: String) = renderer.message(
        message,
        Placeholder.unparsed("playername", playerName)
    )

}
