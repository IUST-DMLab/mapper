package ir.ac.iust.dml.kg.utils

import com.google.common.base.CaseFormat
import org.apache.log4j.Logger
import java.util.*

object PrefixService {

   val logger = Logger.getLogger(this.javaClass)!!
   val prefixNames = mutableMapOf<String, String>()
   val prefixAddresses = mutableMapOf<String, String>()

   val KG_RESOURCE_PREFIX = "fkgr"
   val KG_ONTOLOGY_PREFIX = "fkgo"
   val KG_AUTO_PROPERTY_PREFIX = "fkgp"

  val PROPERTY_URI = "rdf:Property"
  val PROPERTY_LABEL_URL = "rdfs:label"
  val RESOURCE_LABEL_URL = "rdfs:label"
  val PROPERTY_DOMAIN_URL = "rdfs:domain"
  val TYPE_URL = "rdf:type"
  val PROPERTY_VARIANT_LABEL_URL = "fkgo:variantLabel"
  val INSTANCE_OF_URL = "fkgo:instanceOf"

   init {
      reload()
   }

   fun reload() {
      val prefixServices = Properties()
      prefixServices.load(this.javaClass.getResourceAsStream("/prefixes.properties"))
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
     if (source == null || !source.contains(':') || source.startsWith("http")) return source
      val splits = source.split(":")
      var address = prefixNames[splits[0]]
      if (address != null && !address.startsWith("http://") && !address.startsWith("https://"))
         address = "http://" + address
      return if (address == null) splits[1] else address + splits[1]
   }

   fun getFkgResourceUrl(name: String) = prefixNames[KG_RESOURCE_PREFIX] + name.replace(' ', '_')

   fun getFkgResource(name: String) = KG_RESOURCE_PREFIX + ":" + name.replace(' ', '_')

   fun getFkgOntologyPropertyUrl(name: String) =
         prefixNames[KG_ONTOLOGY_PREFIX] +
               CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name.replace(' ', '_'))

   fun getFkgOntologyClassUrl(name: String) =
         prefixNames[KG_ONTOLOGY_PREFIX] +
               CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name.replace(' ', '_'))

   fun getFkgOntologyProperty(name: String) =
         KG_ONTOLOGY_PREFIX + ":" + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name.replace(' ', '_'))

   fun getFkgOntologyClass(name: String) =
         KG_ONTOLOGY_PREFIX + ":" + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name.replace(' ', '_'))

  fun convertFkgResourceUrl(url: String): String {
      if (url.startsWith("http://fa.wikipedia.org/wiki/")
            || url.startsWith("fa.wikipedia.org/wiki/"))
         return prefixNames[KG_RESOURCE_PREFIX] + url.substringAfterLast("/")
      return url
   }

  fun convertFkgOntologyUrl(url: String): String {
      return prefixNames[KG_ONTOLOGY_PREFIX] + url.substringAfterLast("/")
   }

   // this is different from dbpedia. they converts xx_yy to xxYy. but we don't change that.
   // because persian letters has not upper case
   fun generateOntologyProperty(rawProperty: String, prefix: String = "dbo")
         = prefix + ":" + PropertyNormaller.removeDigits(rawProperty).replace(' ', '_')

   fun convertFkgProperty(property: String): String? {
      if (property.contains("://")) return property
      val p =
            if (!property.contains(":")) generateOntologyProperty(property, KG_AUTO_PROPERTY_PREFIX)
            else property.replace(' ', '_')
      return prefixToUri(p)
   }

   fun isUrlFast(str: String?): Boolean {
      if (str == null) return false
      if ((str.startsWith("http://") || str.startsWith("https://")) && !str.contains(' ')) return true
      return false
   }
}