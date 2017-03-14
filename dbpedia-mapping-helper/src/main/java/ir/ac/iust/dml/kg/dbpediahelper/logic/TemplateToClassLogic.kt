package ir.ac.iust.dml.kg.dbpediahelper.logic

import ir.ac.iust.dml.kg.dbpediahelper.access.dao.TemplateToClassDao
import ir.ac.iust.dml.kg.dbpediahelper.access.entities.FkgTemplateMapping
import ir.ac.iust.dml.kg.dbpediahelper.logic.data.TemplateToClassData
import ir.ac.iust.dml.kg.utils.LanguageChecker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class TemplateToClassLogic {

   @Autowired lateinit var dao: TemplateToClassDao

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

   fun getEditData(id: Long? = null): TemplateToClassData {
      if (id == null) return TemplateToClassData(approved = false)
      val t = dao.read(id) ?: return TemplateToClassData(approved = false)
      return TemplateToClassData(id = t.id, language = t.language,
            templateName = t.templateName, className = t.className,
            approved = t.approved)
   }

   fun edit(data: TemplateToClassData): TemplateToClassData {
      val entity =
            if (data.id == null) FkgTemplateMapping()
            else dao.read(data.id!!) ?: FkgTemplateMapping()
      entity.className = data.className
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
   fun searchClassName(page: Int, pageSize: Int, keyword: String?) = dao.searchClassName(page, pageSize, keyword)
}