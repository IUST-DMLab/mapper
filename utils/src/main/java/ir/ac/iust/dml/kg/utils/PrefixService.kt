package ir.ac.iust.dml.kg.utils

import org.apache.log4j.Logger
import java.util.*

object PrefixService {

   val logger = Logger.getLogger(this.javaClass)!!
   val prefixNames = mutableMapOf<String, String>()
   val prefixAddresses = mutableMapOf<String, String>()

   init {
      reload()
   }

   fun reload() {
      val prefixServices = Properties()
      prefixServices.load(this.javaClass.getResourceAsStream("/src/main/resources/prefixes.properties"))
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
      if (address != null && !address.startsWith("http://") && !address.startsWith("https://"))
         address = "http://" + address
      return if (address == null) splits[1] else address + splits[1]
   }

   fun getFkgResourceUrl(name: String) = prefixNames["fkgr"] + name.replace(' ', '_')

   fun convertFkgResource(url: String): String {
      if (url.startsWith("http://fa.wikipedia.org/wiki/")
            || url.startsWith("fa.wikipedia.org/wiki/"))
         return prefixNames["fkgr"] + url.substringAfterLast("/")
      return url
   }

   fun convertFkgProperty(predicate: String): String? {
      if (predicate.contains(":")) return predicate
      return prefixNames["fkgp"] + PropertyNormaller.removeDigits(predicate, false, true)
   }
}