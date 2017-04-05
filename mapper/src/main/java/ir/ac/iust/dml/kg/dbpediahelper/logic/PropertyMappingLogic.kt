package ir.ac.iust.dml.kg.dbpediahelper.logic

import com.google.common.base.CaseFormat
import ir.ac.iust.dml.kg.access.dao.FkgPropertyMappingDao
import ir.ac.iust.dml.kg.access.dao.FkgTemplateMappingDao
import ir.ac.iust.dml.kg.access.dao.FkgTripleStatisticsDao
import ir.ac.iust.dml.kg.access.dao.WikipediaPropertyTranslationDao
import ir.ac.iust.dml.kg.access.entities.FkgPropertyMapping
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.access.entities.enumerations.TripleStatisticsType
import ir.ac.iust.dml.kg.dbpediahelper.logic.data.FkgPropertyMappingData
import ir.ac.iust.dml.kg.raw.utils.LanguageChecker
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
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
    var page = 0
    do {
      val pages = dao.search(pageSize = 100, page = page++, noUpdateEpoch = true, language = null)
      val now = System.currentTimeMillis()
      pages.data.forEach {
        it.updateEpoch = now
        dao.save(it)
      }
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

  fun updateCounts(): Boolean {
    var page = 0
    do {
      val pagedData = statsDao.search(page = page++, pageSize = 100, countType = TripleStatisticsType.typedProperty)
      logger.info("processing page $page")
      pagedData.data.forEach {
        val classMapping = templateDao.read(it.templateType!!, null, true)
        if (classMapping != null) {
          val noURI = if (it.property!!.contains(":")) it.property!!.substringAfter(':') else it.property!!
          val mapping = FkgPropertyMapping(language = "fa", templateName = it.templateType,
                  templateProperty = noURI, status = MappingStatus.NotMapped, approved = false,
                  tupleCount = it.count!!.toLong(), ontologyClass = classMapping.ontologyClass,
                  updateEpoch = System.currentTimeMillis())
          val translations = wikiPropertyTranslationDao.readByFaTitle(type = null, faProperty = noURI)
          val p = if (translations.isNotEmpty()) translations[0].enProperty!! else noURI
          val exact = dao.search(page = 0, pageSize = 0, language = "en", templateProperty = p,
                  clazz = classMapping.ontologyClass)
          if (exact.data.isNotEmpty()) {
            mapping.status = MappingStatus.NearlyApproved
            mapping.ontologyProperty = exact.data[0].ontologyProperty
            dao.save(mapping)
          } else {
            val l = dao.listUniqueOntologyProperties(noURI)
            if (l.isNotEmpty() && l.size < 3) {
              mapping.status = if (l.size == 1) MappingStatus.Translated else MappingStatus.Multiple
              l.forEach {
                dao.save(mapping.copy(ontologyProperty = it))
              }
            } else {
              mapping.ontologyProperty = "dbp:" + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, noURI)
              dao.save(mapping)
            }
          }
        }
      }
    } while (pagedData.data.isNotEmpty() && page < 50)
    return true
  }
}