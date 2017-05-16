package ir.ac.iust.dml.kg.dbpediahelper.logic

import ir.ac.iust.dml.kg.access.dao.FkgPropertyMappingDao
import ir.ac.iust.dml.kg.access.dao.FkgTemplateMappingDao
import ir.ac.iust.dml.kg.access.dao.FkgTripleStatisticsDao
import ir.ac.iust.dml.kg.access.dao.WikipediaPropertyTranslationDao
import ir.ac.iust.dml.kg.access.dao.knowldegestore.KnowledgeStoreFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.entities.FkgPropertyMapping
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.access.entities.enumerations.TripleStatisticsType
import ir.ac.iust.dml.kg.dbpediahelper.logic.data.FkgPropertyMappingData
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.LanguageChecker
import ir.ac.iust.dml.kg.raw.utils.PrefixService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.annotation.PostConstruct

@Service
class PropertyMappingLogic {

   val logger = Logger.getLogger(this.javaClass)!!
   @Autowired lateinit var dao: FkgPropertyMappingDao
   @Autowired lateinit var wikiPropertyTranslationDao: WikipediaPropertyTranslationDao
   @Autowired lateinit var templateDao: FkgTemplateMappingDao
   @Autowired lateinit var statsDao: FkgTripleStatisticsDao

   @PostConstruct
   fun fillUpdateEpoch() {
      do {
         val pages = dao.search(pageSize = 100, page = 0, noUpdateEpoch = true, language = null)
         val now = System.currentTimeMillis()
         pages.data.forEach {
            it.updateEpoch = now
            dao.save(it)
         }
      } while (pages.data.isNotEmpty())
   }

   @PostConstruct
   fun fillTemplatePropertyLanguage() {
      do {
         val pages = dao.search(pageSize = 100, page = 0, noTemplatePropertyLanguage = true, language = null)
         pages.data.forEach { dao.save(it) }
      } while (pages.data.isNotEmpty())
   }

   @Throws(Exception::class)
   fun exportAll(after: Long?) = dao.search(page = 0, pageSize = 0, after = after, language = null).data

   fun search(page: Int = 0, pageSize: Int = 20, templateName: String? = null, className: String? = null,
              templateProperty: String?, ontologyProperty: String?, like: Boolean = false,
              language: String? = null, approved: Boolean? = null, status: MappingStatus?,
              after: Long? = null)
         = dao.search(page = page, pageSize = pageSize,
         type = templateName, clazz = className,
         templateProperty = templateProperty, ontologyProperty = ontologyProperty, like = like,
         language = language, approved = approved, after = after, status = status)

   fun getEditData(id: Long? = null): FkgPropertyMappingData {
      if (id == null) return FkgPropertyMappingData(approved = false)
      val t = dao.read(id) ?: return FkgPropertyMappingData(approved = false)
      return FkgPropertyMappingData(id = t.id, language = t.language,
            templateName = t.templateName, ontologyClass = t.ontologyClass,
            templateProperty = t.templateProperty, ontologyProperty = t.ontologyProperty,
            approved = t.approved, status = t.status)
   }

   fun edit(data: FkgPropertyMappingData): FkgPropertyMappingData {
      val entity =
            if (data.id == null) FkgPropertyMapping()
            else dao.read(data.id!!) ?: FkgPropertyMapping()
      entity.ontologyClass = data.ontologyClass
      entity.templateName = data.templateName
      entity.ontologyProperty = data.ontologyProperty
      entity.templateProperty = data.templateProperty
      entity.approved = data.approved
      entity.status = data.status
      entity.language =
            if (data.language == null)
               (if (LanguageChecker.isEnglish(data.templateName!!)) "en" else "fa")
            else data.language
      entity.updateEpoch = System.currentTimeMillis()
      dao.save(entity)
      return getEditData(entity.id)
   }

   fun searchTemplateName(page: Int, pageSize: Int, keyword: String?)
         = dao.searchTemplateName(page, pageSize, keyword)

   fun searchOntologyClass(page: Int, pageSize: Int, keyword: String?)
         = dao.searchOntologyClass(page, pageSize, keyword)

   fun searchTemplatePropertyName(page: Int, pageSize: Int, keyword: String?)
         = dao.searchTemplatePropertyName(page, pageSize, keyword)

   fun searchOntologyPropertyName(page: Int, pageSize: Int, keyword: String?)
         = dao.searchOntologyPropertyName(page, pageSize, keyword)

   fun generateMapping(): Boolean {
      var page = 0
      val old = dao.search(page = 0, pageSize = 0, language = "fa",
            status = MappingStatus.Multiple, approved = false)
      old.data.forEach { dao.delete(it) }
      do {
         val pagedData = statsDao.search(page = page++, pageSize = 100,
               countType = TripleStatisticsType.typedProperty)
         logger.info("processing page $page")
         for (it in pagedData.data) {
            try {
               val classMapping = templateDao.read(templateName = it.templateName!!, className = null)
               if (classMapping != null) {
                  val rawProperty = it.property!!
                  // nearTemplateNames is for backward compatibility
                  var mapping = dao.read(templateName = it.templateName!!, nearTemplateNames = true,
                        templateProperty = rawProperty)
                  if (mapping == null)
                     mapping = FkgPropertyMapping(language = "fa", templateName = it.templateName,
                           templateProperty = rawProperty, status = MappingStatus.AutoGenerated, approved = false,
                           tupleCount = it.count!!.toLong(), ontologyClass = classMapping.ontologyClass,
                           updateEpoch = System.currentTimeMillis())
                  else {
                     logger.info("repeated property: ${it.templateName}/$rawProperty")
                     // backward compatibility
                     val count = (it.count ?: 0).toLong()
                     val language = LanguageChecker.detectLanguage(mapping.templateProperty)
                     if (mapping.templateName != it.templateName
                           || mapping.templateProperty != it.property
                           || !mapping.ontologyClass!!.startsWith("dbo")
                           || mapping.tupleCount == count
                           || mapping.templatePropertyLanguage != language) {
                        mapping.templateName = it.templateName
                        mapping.templateProperty = it.property
                        if (!mapping.ontologyClass!!.startsWith("dbo"))
                           mapping.ontologyClass = "dbo:" + mapping.ontologyClass
                        mapping.tupleCount = count
                        mapping.templatePropertyLanguage = language
                        dao.save(mapping)
                     }
                     continue
                  }

                  var translations
                        = wikiPropertyTranslationDao.readByFaTitle(type = it.templateName!!, faProperty = rawProperty)
                  if (translations.isEmpty())
                     translations = wikiPropertyTranslationDao.readByFaTitle(type = null, faProperty = rawProperty)
                  val p = if (translations.isNotEmpty()) translations[0].enProperty!! else rawProperty
                  // searching in english dbpedia mappings
                  val exact = dao.search(page = 0, pageSize = 0, language = "en", templateProperty = p,
                        secondTemplateProperty = p.replace(' ', '_'), clazz = "dbo:" + classMapping.ontologyClass)
                  if (exact.data.isNotEmpty()) {
                     mapping.status = MappingStatus.NearlyMapped
                     mapping.ontologyProperty = exact.data[0].ontologyProperty
                     dao.save(mapping)
                  } else {
                     val l = dao.listUniqueOntologyProperties(rawProperty).filter { !it.startsWith("dbp") }
                     if (l.isNotEmpty() && l.size < 4) {
                        mapping.status = if (l.size == 1) MappingStatus.Translated else MappingStatus.Multiple
                        mapping.ontologyProperty = l.joinToString(",")
                     } else
                        mapping.ontologyProperty = PrefixService.generateOntologyProperty(rawProperty)
                     dao.save(mapping)
                  }
               }
            } catch (e: Throwable) {
               e.printStackTrace()
               logger.error(e)
            }
         }
         Thread.sleep(500)
      } while (pagedData.data.isNotEmpty() && page < 100)
      return true
   }

   fun writeResourcesToKnowledgeStore() {
      val startTime = System.currentTimeMillis()
      logger.info("starting at ${Date()}")
      val maxNumberOfRelations = ConfigReader.getInt("test.mode.max.relations", "50000")
      val language = if (maxNumberOfRelations != 50000) "fa" else null
      val approved = if (maxNumberOfRelations != 50000) true else null
      logger.info("max number of relations is $maxNumberOfRelations")

      val knowledgeStoreDao = KnowledgeStoreFkgTripleDaoImpl()
      val addedRelations = mutableSetOf<String>()
      val addedLabels = mutableSetOf<String>()
      var relationNumber = 0
      var page = 0

      val PROPERTY_URI = PrefixService.prefixToUri(PrefixService.PROPERTY_URI)
      val LABEL_URL = PrefixService.prefixToUri(PrefixService.PROPERTY_LABEL_URL)
      val PROPERTY_DOMAIN_URL = PrefixService.prefixToUri(PrefixService.PROPERTY_DOMAIN_URL)
      val TYPE_URL = PrefixService.prefixToUri(PrefixService.TYPE_URL)
      val VARIANT_LABEL_URL = PrefixService.prefixToUri(PrefixService.PROPERTY_VARIANT_LABEL_URL)

      do {
         val data = dao.search(pageSize = 100, page = page++, language = null,
               templatePropertyLanguage = language, approved = approved)
         for (relation in data.data) {
            relationNumber++
            if (relationNumber > maxNumberOfRelations) break
            if (relationNumber % 100 == 0)
               logger.info("relation number is $relationNumber.\t" +
                     "time: ${(System.currentTimeMillis() - startTime) / 1000}\tsecs")
            if (relation.status == MappingStatus.Multiple || relation.status == null) continue
            if (relation.ontologyProperty == null) continue
            try {
               val property = relation.ontologyProperty!!.replace("dbo:", PrefixService.KG_ONTOLOGY_PREFIX + ":")
               //TODO: why?!
               var uri = PrefixService.prefixToUri(property)!!
               if (uri.isBlank()) continue
               if (!uri.contains(':')) uri = PrefixService.getFkgOntologyPropertyUrl(uri)
               val label = relation.templateProperty!!.replace('_', ' ')
               if (!addedRelations.contains(property)) {
                  knowledgeStoreDao.save(FkgTriple(
                      subject = uri, predicate = TYPE_URL, objekt = PROPERTY_URI
                  ), null)
                  knowledgeStoreDao.save(FkgTriple(
                      subject = uri, predicate = LABEL_URL, objekt = label,
                        language = relation.templatePropertyLanguage
                  ), null)
                  addedRelations.add(property)
               }

               val propertyAndLabel = property + "~" + label
               if (!addedLabels.contains(propertyAndLabel)) {
                  knowledgeStoreDao.save(FkgTriple(
                      subject = uri, predicate = VARIANT_LABEL_URL,
                        objekt = label, language = relation.templatePropertyLanguage
                  ), null)
                  addedRelations.add(property)
               }
               knowledgeStoreDao.save(FkgTriple(
                   subject = uri, predicate = PROPERTY_DOMAIN_URL,
                   objekt = PrefixService.convertFkgOntologyUrl(relation.ontologyClass!!)
               ), null)
            } catch (e: Throwable) {
               e.printStackTrace()
               logger.error(e)
            }
         }
      } while (data.page < data.pageCount && relationNumber <= maxNumberOfRelations)
      knowledgeStoreDao.flush()
   }

   fun predicateExport(page: Int, pageSize: Int, keyword: String?,
                       ontologyClass: String?, templateName: String?,
                       status: MappingStatus?, language: String?):
         Map<String, List<FkgPropertyMapping>> {
      val uniqueProperties = dao.listUniqueProperties(page, pageSize, "fa", keyword, ontologyClass, templateName, status, language)
      val result = mutableMapOf<String, List<FkgPropertyMapping>>()
      uniqueProperties.forEach { templateProperty ->
         result[templateProperty] = dao.search(page = 0, pageSize = 0, language = "fa", clazz = ontologyClass,
               templateProperty = templateProperty, status = status).data
      }
      return result
   }
}