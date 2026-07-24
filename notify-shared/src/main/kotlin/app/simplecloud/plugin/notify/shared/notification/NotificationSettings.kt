package app.simplecloud.plugin.notify.shared.notification

import app.simplecloud.api.player.PlayerApi
import kotlinx.coroutines.future.await
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class NotificationSettings(
    private val api: PlayerApi,
) {

    private val property = "notify.enabled"
    private val cache: MutableMap<UUID, Boolean> = ConcurrentHashMap()

    suspend fun load(id: UUID) {
        val player = api.get(id).await()
        cache[id] = player?.properties?.get(property)?.toBooleanStrictOrNull() ?: true
    }

    fun unload(id: UUID) {
        cache.remove(id)
    }

    fun isEnabled(id: UUID): Boolean {
        return cache[id] ?: true
    }

    fun set(id: UUID, value: Boolean): CompletableFuture<*> {
        cache[id] = value
        return api.updatePlayerProperty(id, property, value.toString())
    }

    fun clear() {
        cache.clear()
    }

}