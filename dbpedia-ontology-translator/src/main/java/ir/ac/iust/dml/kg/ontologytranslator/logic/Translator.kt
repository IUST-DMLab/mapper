package ir.ac.iust.dml.kg.ontologytranslator.logic

import ir.ac.iust.dml.kg.access.dao.FkgClassDao
import ir.ac.iust.dml.kg.access.entities.FkgClass
import ir.ac.iust.dml.kg.ontologytranslator.logic.export.FkgClassData
import ir.ac.iust.dml.kg.raw.utils.PagedData
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class Translator {
   @Autowired lateinit var dao: FkgClassDao
   val logger = Logger.getLogger(this.javaClass)

   fun sync(fkgClass: FkgClass?): FkgClassData? {
      if (fkgClass == null) return null
      return FkgClassData(
            ontologyClass = fkgClass.name,
            parentOntologyClass =
            if (fkgClass.parentId == null) null
            else dao.read(fkgClass.parentId)!!.name,
            enLabel = fkgClass.enLabel,
            comment = fkgClass.comment,
            faLabel = fkgClass.faLabel,
            faOtherLabels = fkgClass.faOtherLabels,
            note = fkgClass.note,
            approved = fkgClass.approved
      )
   }

   fun getNode(name: String): FkgClassData? {
      return sync(dao.read(name = name))
   }

   fun getParent(name: String): FkgClassData? {
      val fkgClass = dao.read(name = name) ?: return null
      return if (fkgClass.parentId == null) null else sync(dao.read(fkgClass.parentId))
   }

   fun getRoot(): FkgClassData? {
      return sync(dao.readRoot())
   }

   fun getChildren(parent: String): List<FkgClassData> {
      val fkgClass = dao.read(name = parent) ?: return mutableListOf()
      val children = dao.getChildren(fkgClass.id!!)
      val data = mutableListOf<FkgClassData>()
      children.forEach { data.add(sync(it)!!) }
      return data
   }

   fun search(name: String?, parent: String?, like: Boolean, approved: Boolean?, hasFarsi: Boolean?,
              pageSize: Int, page: Int): PagedData<FkgClassData> {
      val parentId = if (parent == null) null else dao.read(name = parent)?.id
      val paged = dao.search(name, parentId, like, approved, hasFarsi, pageSize, page)
      val data = mutableListOf<FkgClassData>()
      paged.data.forEach { data.add(sync(it)!!) }
      return PagedData(data, paged.page, paged.pageSize, paged.pageCount, paged.rowCount)
   }

   fun translate(data: FkgClassData): Boolean {
      val fkgClass = dao.read(name = data.ontologyClass!!) ?: return false
      fkgClass.faLabel = data.faLabel
      fkgClass.faOtherLabels = data.faOtherLabels
      fkgClass.note = data.note
      fkgClass.approved = data.approved
      try {
         dao.save(fkgClass)
         return true
      } catch (e: Throwable) {
         logger.error("can not save translation", e)
         return false
      }
   }
}