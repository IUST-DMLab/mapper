package ir.ac.iust.dml.kg.utils

import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import org.apache.log4j.Logger
import java.io.FileInputStream
import java.nio.file.Files
import java.util.*

object PrefixService {

   val logger = Logger.getLogger(this.javaClass)!!
   val prefixNames = mutableMapOf<String, String>()
   val prefixAddresses = mutableMapOf<String, String>()

   init {
      reload()
   }

   fun reload() {
      val path = ConfigReader.getPath("dbpedia.prefixes", "~/.pkg/data/prefixes.properties")
      Files.createDirectories(path.parent)
      if (!Files.exists(path)) Files.copy(this.javaClass.getResourceAsStream("/src/main/resources/prefixes.properties"), path)
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

   fun prefixToUri(source: String?): String? {
      if (source == null || !source.contains(':')) return source
      val splits = source.split(":")
      var address = prefixNames[splits[0]]
      if (address != null && !address.startsWith("http://")) address = "http://" + address
      return if (address == null) splits[1] else address + splits[1]
   }
}