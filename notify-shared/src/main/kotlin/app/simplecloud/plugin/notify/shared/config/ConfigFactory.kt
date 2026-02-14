package app.simplecloud.plugin.notify.shared.config

import kotlinx.coroutines.*
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.kotlin.toNode
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds

object ConfigFactory {
    private val watcherService = FileSystems.getDefault().newWatchService()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun loadOrCreate(dataDirectory: Path, onReload: ((Config) -> Unit)? = null): Config {
        val path = dataDirectory.resolve("config.yml")
        val loader = buildLoader(path)

        if (!Files.exists(path)) {
            create(path, loader)
        }

        val config = load(loader)
        startWatchService(dataDirectory, path, onReload)
        return config
    }

    private fun load(loader: YamlConfigurationLoader): Config {
        val configurationNode = loader.load()
        return configurationNode.get() ?: throw IllegalStateException("Config could not be loaded")
    }

    private fun buildLoader(path: Path): YamlConfigurationLoader {
        return YamlConfigurationLoader.builder()
            .path(path)
            .nodeStyle(NodeStyle.BLOCK)
            .defaultOptions { options ->
                options.serializers {
                    it.registerAnnotatedObjects(objectMapperFactory()).build()
                }
            }
            .build()
    }

    private fun create(path: Path, loader: YamlConfigurationLoader) {
        val config = Config()
        path.parent?.let { Files.createDirectories(it) }
        Files.createFile(path)

        val configurationNode = loader.load()
        config.toNode(configurationNode)
        loader.save(configurationNode)
    }

    private fun startWatchService(dataDirectory: Path, configPath: Path, onReload: ((Config) -> Unit)?) {
        dataDirectory.register(watcherService, StandardWatchEventKinds.ENTRY_MODIFY)

        scope.launch {
            while (isActive) {
                val key = withContext(Dispatchers.IO) { watcherService.take() }
                for (event in key.pollEvents()) {
                    val changed = dataDirectory.resolve(event.context() as Path)
                    if (changed == configPath) {
                        delay(100)
                        val newConfig = withContext(Dispatchers.IO) {
                            load(buildLoader(configPath))
                        }
                        onReload?.invoke(newConfig)
                    }
                }
                key.reset()
            }
        }
    }
}