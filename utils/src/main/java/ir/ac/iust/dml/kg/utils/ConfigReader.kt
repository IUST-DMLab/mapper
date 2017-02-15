package ir.ac.iust.dml.kg.utils

import org.apache.log4j.Logger
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

object ConfigReader {
    fun getConfig(keyValues: Map<String, Any>): Properties {
        val logger = Logger.getLogger(this.javaClass)!!
        val configPath = Paths.get(System.getProperty("user.home")).resolve("pkg").resolve("config.properties")

        Files.createDirectories(configPath.parent)

        val config = Properties()

        if (!Files.exists(configPath)) logger.error("There is no file ${configPath.toAbsolutePath()} existed.")
        else
            FileInputStream(configPath.toFile()).use {
                try {
                    config.load(it)
                } catch (e: Throwable) {
                    logger.error("error in reading config file ${configPath.toAbsolutePath()}.", e)
                }
            }

        keyValues.forEach {
            try {
                config.getProperty(it.key)!!
            } catch (e: Throwable) {
                config[it.key] = it.value
            }
        }

        FileOutputStream(configPath.toFile()).use {
            config.store(it, null)
        }

        return config
    }

    fun getPath(string: String): Path {
        val splits = string.split("/")
        val first = if (splits[0] == "~") System.getProperty("user.home")!! else splits[0]
        return Paths.get(first, *splits.subList(1, splits.size).toTypedArray())
    }
}