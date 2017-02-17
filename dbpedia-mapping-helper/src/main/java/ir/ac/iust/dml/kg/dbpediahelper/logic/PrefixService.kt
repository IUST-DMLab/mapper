package ir.ac.iust.dml.kg.dbpediahelper.logic

import ir.ac.iust.dml.kg.utils.ConfigReader
import org.apache.log4j.Logger
import org.springframework.stereotype.Service
import java.io.FileInputStream
import java.nio.file.Files
import java.util.*

@Service
class PrefixService {

    val logger = Logger.getLogger(this.javaClass)!!
    val prefixNames = mutableMapOf<String, String>()
    val prefixAddresses = mutableMapOf<String, String>()

    init {
        reload()
    }

    fun reload() {
        val DBPEDIA_PREFIXES = "dbpedia.prefixes"
        val config = ConfigReader.getConfig(mapOf(DBPEDIA_PREFIXES to "~/pkg/data/prefixes.properties"))
        val path = ConfigReader.getPath(config[DBPEDIA_PREFIXES]!! as String)
        if (!Files.exists(path)) Files.copy(this.javaClass.getResourceAsStream("/prefixes.properties"), path)
        val prefixServices = Properties()
        prefixServices.load(FileInputStream(path.toFile()))
        prefixServices.keys.forEach {
            prefixNames[it as String] = prefixServices.getProperty(it)!!
            prefixAddresses[prefixServices.getProperty(it)!!] = it
        }
        logger.trace("${prefixNames.size} prefixes has been loaded.")
    }

    fun replacePrefixes(text: String): String {
        var result = text
        prefixAddresses.keys.asSequence()
                .filter { result.contains(it) }
                .forEach { result = result.replace(it, prefixAddresses[it]!! + ":") }
        return result
    }
}