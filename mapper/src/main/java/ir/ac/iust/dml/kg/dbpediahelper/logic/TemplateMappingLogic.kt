package ir.ac.iust.dml.kg.dbpediahelper.logic

import ir.ac.iust.dml.kg.access.dao.FkgTemplateMappingDao
import ir.ac.iust.dml.kg.access.entities.FkgTemplateMapping
import ir.ac.iust.dml.kg.dbpediahelper.logic.data.FkgTemplateMappingData
import ir.ac.iust.dml.kg.utils.LanguageChecker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class TemplateMappingLogic {

   @Autowired lateinit var dao: FkgTemplateMappingDao

   @PostConstruct
   fun fillUpdateEpoch() {
      var page = 0
      do {
         val pages = dao.search(pageSize = 100, page = page++, noUpdateEpoch = true)
         val now = System.currentTimeMillis()
         pages.data.forEach {
            it.updateEpoch = now
            dao.save(it)
         }
      } while (pages.data.isNotEmpty())
   }

   @Throws(Exception::class)
   fun exportAll(after: Long?) = dao.search(page = 0, pageSize = 0, after = after).data

   fun search(page: Int = 0, pageSize: Int = 20, templateName: String? = null,
              className: String? = null, like: Boolean = false,
              language: String? = null, approved: Boolean? = null,
              after: Long? = null)
         = dao.search(page = page, pageSize = pageSize,
         templateName = templateName, className = className, like = like,
         language = language, approved = approved, after = after)

   fun getEditData(id: Long? = null): FkgTemplateMappingData {
      if (id == null) return FkgTemplateMappingData(approved = false)
      val t = dao.read(id) ?: return FkgTemplateMappingData(approved = false)
      return FkgTemplateMappingData(id = t.id, language = t.language,
            templateName = t.templateName, ontologyClass = t.ontologyClass,
            approved = t.approved)
   }

   fun edit(data: FkgTemplateMappingData): FkgTemplateMappingData {
      val entity =
            if (data.id == null) FkgTemplateMapping()
            else dao.read(data.id!!) ?: FkgTemplateMapping()
      entity.ontologyClass = data.ontologyClass
      entity.templateName = data.templateName
      entity.approved = data.approved
      entity.language =
            if (data.language == null)
               (if (LanguageChecker.isEnglish(data.templateName!!)) "en" else "fa")
            else data.language
      entity.updateEpoch = System.currentTimeMillis()
      dao.save(entity)
      return getEditData(entity.id)
   }

   fun searchTemplateName(page: Int, pageSize: Int, keyword: String?) = dao.searchTemplateName(page, pageSize, keyword)
   fun searchOntologyClass(page: Int, pageSize: Int, keyword: String?) = dao.searchOntologyClass(page, pageSize, keyword)
}