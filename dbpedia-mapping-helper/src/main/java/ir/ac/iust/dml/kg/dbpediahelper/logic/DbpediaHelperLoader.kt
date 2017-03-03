package ir.ac.iust.dml.kg.dbpediahelper.logic

import ir.ac.iust.dml.kg.dbpediahelper.access.dao.DBpediaPropertyMappingDao
import ir.ac.iust.dml.kg.dbpediahelper.access.entities.DBpediaPropertyMapping
import ir.ac.iust.dml.kg.dbpediahelper.access.entities.MappingStatus
import ir.ac.iust.dml.kg.dbpediahelper.logic.dump.OwlDumpReader
import ir.ac.iust.dml.kg.utils.ConfigReader
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Files

@Service
class DbpediaHelperLoader {

   @Autowired
   lateinit var dao: DBpediaPropertyMappingDao
   @Autowired
   lateinit var prefixService: PrefixService
   val logger = Logger.getLogger(this.javaClass)!!

   @Throws(Exception::class)
   fun load() {
      val ONTOLOGY_DUMP = "ontology.dump.en"
      val config = ConfigReader.getConfig(mapOf(ONTOLOGY_DUMP to "~/.pkg/data/dbpedia_mapping.owl"))
      val path = ConfigReader.getPath(config[ONTOLOGY_DUMP]!! as String)
      Files.createDirectories(path.parent)
      if (!Files.exists(path)) {
         throw Exception("There is no file ${path.toAbsolutePath()} existed.")
      }

      dao.deleteAll()
      prefixService.reload()
      OwlDumpReader(path).use {
         owlDumpReader ->
         var ontologyClass: String? = null
         var infoboxType: String? = null
         var ontologyProperty: String? = null
         var templateProperty: String? = null
         while (owlDumpReader.hasNext()) {
            val triples = owlDumpReader.next()
            for (triple in triples) {
               triple.subject = prefixService.replacePrefixes(triple.subject)
               triple.predicate = prefixService.replacePrefixes(triple.predicate)
               triple.objekt = prefixService.replacePrefixes(triple.objekt)
               if (triple.objekt == "rr:TriplesMap" && triple.subject.startsWith("<dboeni:"))
                  infoboxType = triple.subject.substringAfter(":").substringBeforeLast(">")
               if (triple.predicate == "rr:class" && triple.objekt.startsWith("<"))
                  ontologyClass = triple.objekt.substringAfterLast("<").substringBeforeLast(">")
               if (triple.predicate == "rr:predicate" && triple.objekt.startsWith("<"))
                  ontologyProperty = triple.objekt.substringAfter("<").substringBeforeLast(">")
               if (triple.predicate == "rml:reference")
                  templateProperty = triple.objekt
//                    println("$infoboxType $ontologyClass $ontologyProperty $templateProperty")
               if (infoboxType != null && ontologyClass != null
                     && ontologyProperty != null && templateProperty != null) {
                  logger.info("found: $infoboxType $templateProperty $ontologyClass $ontologyProperty")
                  dao.save(DBpediaPropertyMapping(language = "en",
                        type = infoboxType,
                        clazz = ontologyClass,
                        templateProperty = templateProperty,
                        ontologyProperty = ontologyProperty,
                        status = MappingStatus.NearlyApproved))
                  templateProperty = null
                  ontologyProperty = null
               }
            }
         }
      }
   }

   fun generatePersian() {
      var page = 0
      do {
         val list = dao.listTemplatePropertyMapping(page = page++)
         for ((id, type, faProperty, enProperty, notTranslated) in list.data) {
            if (notTranslated!!) continue
            val dbpediaEnglishMapping = dao.read(language = "en", type = type,
                  templateProperty = enProperty!!)
            if (dbpediaEnglishMapping.isEmpty())
               dbpediaEnglishMapping.addAll(dao.read(language = "en", type = type,
                     templateProperty = enProperty.replace('_', ' ')))
            if (dbpediaEnglishMapping.isNotEmpty()) {
               val persianMapping = DBpediaPropertyMapping(language = "fa",
                     type = type, clazz = dbpediaEnglishMapping[0].clazz,
                     templateProperty = faProperty,
                     ontologyProperty = dbpediaEnglishMapping[0].ontologyProperty,
                     status = MappingStatus.NearlyApproved)
               logger.info("persian mapping found: $persianMapping")
               dao.save(persianMapping)
            } else {
               //not found
               dbpediaEnglishMapping.addAll(dao.read(language = "en", templateProperty = enProperty))
               dbpediaEnglishMapping.addAll(dao.read(language = "en", type = type,
                     templateProperty = enProperty.replace('_', ' ')))
               val checked = mutableSetOf<String>()
               if (dbpediaEnglishMapping.isNotEmpty())
                  for (mapping in dbpediaEnglishMapping) {
                     val ontologyProperty = mapping.ontologyProperty!!
                     if (checked.contains(ontologyProperty)) continue
                     val persianMapping = DBpediaPropertyMapping(language = "fa", type = type,
                           clazz = null, templateProperty = faProperty, ontologyProperty = ontologyProperty,
                           status = MappingStatus.NotApproved)
                     checked.add(ontologyProperty)
                     logger.info("not accurate persian mapping found: $persianMapping")
                     dao.save(persianMapping)
                  }
               else {
                  val persianMapping = DBpediaPropertyMapping(language = "fa", type = type,
                        clazz = null, templateProperty = faProperty,
                        ontologyProperty = "dbpe:" + enProperty,
                        status = MappingStatus.Translated)
                  dao.save(persianMapping)
               }
            }
         }
      } while (list.data.isNotEmpty())
   }

   class PropertyAndCount(val property: String, val count: Long) : Comparable<PropertyAndCount> {
      override fun compareTo(other: PropertyAndCount) = count.compareTo(other.count)
   }

   fun generateByCount() {
      var page = 0
      do {
         val list = dao.listUniqueProperties(language = "fa", page = page++)
         for (uniqueProperty in list) {
            val ontologyProperties = dao.listUniqueOntologyProperties(uniqueProperty)
            if (ontologyProperties.size == 1) {
               addMapping(uniqueProperty, ontologyProperties[0])
               continue
            }

            // list is larger than 1
            val countList = ontologyProperties.map {
               PropertyAndCount(property = it,
                     count = dao.countOntologyProperties(uniqueProperty, it))
            }
            countList.sortedDescending()

            var total = 0L
            countList.forEach { total += it.count }
            val totalThird = total / 3

            for (c in countList) {
               if (c.count < totalThird) break
               addMapping(uniqueProperty, c.property)
            }
         }
      } while (list.isNotEmpty())
   }

   private fun addMapping(templateProperty: String, ontologyProperty: String) {
      val m = DBpediaPropertyMapping(language = "fa", clazz = null,
            ontologyProperty = ontologyProperty, templateProperty = templateProperty,
            status = MappingStatus.Translated, type = null)
      dao.save(m)
   }
}