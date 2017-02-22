package ir.ac.iust.dml.kg.ontologytranslator.logic

import ir.ac.iust.dml.kg.ontologytranslator.access.dao.OntologyClassTranslationDao
import ir.ac.iust.dml.kg.ontologytranslator.access.entities.OntologyClassTranslation
import ir.ac.iust.dml.kg.ontologytranslator.logic.export.OntologyClassTranslationData
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class Translator {
   @Autowired lateinit var dao: OntologyClassTranslationDao
   val logger = Logger.getLogger(this.javaClass)

   fun sync(translation: OntologyClassTranslation?): OntologyClassTranslationData? {
      if (translation == null) return null
      return OntologyClassTranslationData(
            ontologyClass = translation.name,
            parentOntologyClass = if (translation.parentId == null) null else dao.read(translation.parentId)!!.name,
            enLabel = translation.enLabel,
            faLabel = translation.faLabel,
            faOtherLabels = translation.faOtherLabels,
            note = translation.note
      )
   }

   fun getNode(name: String): OntologyClassTranslationData? {
      return sync(dao.read(name = name))
   }

   fun getParent(name: String): OntologyClassTranslationData? {
      val translation = dao.read(name = name) ?: return null
      return if (translation.parentId == null) null else sync(dao.read(translation.parentId))
   }

   fun getRoot(): OntologyClassTranslationData? {
      return sync(dao.readRoot())
   }

   fun getChildren(parent: String): List<OntologyClassTranslationData> {
      val translation = dao.read(name = parent) ?: return mutableListOf()
      val children = dao.getChildren(translation.id!!)
      val data = mutableListOf<OntologyClassTranslationData>()
      children.forEach { data.add(sync(it)!!) }
      return data
   }

   fun translate(data: OntologyClassTranslationData): Boolean {
      val translation = dao.read(name = data.ontologyClass!!) ?: return false
      translation.faLabel = data.faLabel
      translation.faOtherLabels = data.faOtherLabels
      translation.note = data.note
      try {
         dao.save(translation)
         return true
      } catch (e: Throwable) {
         logger.error("can not save translation", e)
         return false
      }
   }
}