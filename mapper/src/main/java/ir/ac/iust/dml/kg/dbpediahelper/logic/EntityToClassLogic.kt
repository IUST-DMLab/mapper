package ir.ac.iust.dml.kg.dbpediahelper.logic

import com.google.gson.Gson
import ir.ac.iust.dml.kg.access.dao.FkgClassDao
import ir.ac.iust.dml.kg.access.dao.FkgEntityClassesDao
import ir.ac.iust.dml.kg.access.dao.FkgTemplateMappingDao
import ir.ac.iust.dml.kg.access.entities.FkgClass
import ir.ac.iust.dml.kg.access.entities.FkgEntityClasses
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.dbpediahelper.logic.data.FkgEntityClassesData
import ir.ac.iust.dml.kg.dbpediahelper.logic.dump.EntityDataDumpReader
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Files
import javax.servlet.http.HttpServletResponse


@Service
class EntityToClassLogic {

   @Autowired lateinit var dao: FkgEntityClassesDao
   @Autowired lateinit var templateDao: FkgTemplateMappingDao
   @Autowired lateinit var classDao: FkgClassDao
   private val logger = Logger.getLogger(this.javaClass)!!
   private val treeCache = mutableMapOf<String, String>()

   fun load() {
      val path = ConfigReader.getPath("entity.types.folder", "~/.pkg/data/entity_types_folder")
      Files.createDirectories(path.parent)
      if (!Files.exists(path)) {
         throw Exception("There is no file ${path.toAbsolutePath()} existed.")
      }

      reloadTreeCache()

      val result = PathWalker.getPath(path, Regex("\\d+\\.json"))
      for (file in result) {
         EntityDataDumpReader(file).use {
            while (it.hasNext()) {
               try {
                  val data = it.next()
                  for (infobox in data.infoboxes) {
                     val entityClass = FkgEntityClasses(entity = data.entityName, approved = false)

                     val mapping = templateDao.read(infobox, null)
                     if (mapping == null) {
                        logger.info("no mapping found for $infobox of entity ${data.entityName}")
                        entityClass.className = infobox
                        entityClass.classTree = infobox
                        entityClass.status = MappingStatus.NotMapped
                     } else {
                        entityClass.className = mapping.ontologyClass
                        entityClass.classTree = treeCache[mapping.ontologyClass]
                        entityClass.status = MappingStatus.NearlyMapped
                     }
                     val old = dao.read(entityClass.entity!!, entityClass.className!!)
                     if (old == null || (entityClass.status == MappingStatus.NearlyMapped
                           && old.classTree != treeCache[old.className])) {
                        entityClass.updateEpoch = System.currentTimeMillis()
                        dao.save(entityClass)
                     }
                  }
               } catch (th: Throwable) {
                  logger.error(th)
               }
            }
         }
      }
   }

   private fun reloadTreeCache() {
      treeCache.clear()
      val allClasses = classDao.search(page = 0, pageSize = 0)
      for (ontologyClass in allClasses.data) {
         treeCache[ontologyClass.name!!] = getTree(ontologyClass)!!
      }
   }

   private fun getTree(ontologyClass: FkgClass): String? {
      var oc: FkgClass? = ontologyClass
      val treeBuilder = StringBuilder().append(ontologyClass.name!!)
      while (true) {
         if (oc!!.parentId == null) break
         oc = classDao.read(oc.parentId)
         if (oc != null)
            treeBuilder.append("/").append(oc.name)
      }
      return treeBuilder.toString()
   }


   @Throws(Exception::class)
   fun exportAll(after: Long?) = dao.search(page = 0, pageSize = 0, after = after).data

   fun exportTypes(after: Long?, response: HttpServletResponse) {
      val gson = Gson()
      response.contentType = "text/html; charset=UTF-8"
      response.characterEncoding = "UTF-8"
      val stream = response.writer
      stream.println("{")
      var page = 0
      do {
         val pages = dao.search(page++, 1000, after = after)
         pages.data.forEachIndexed { index, d ->
            stream.print("\"${d.entity!!}\":")
            if (d.classTree != null) stream.print(gson.toJson(d.classTree!!.split('/')))
            else stream.print("[]")
            if (pages.page < pages.pageCount - 1 || index < pages.data.size - 1) stream.println(",")
            else stream.println()
         }
      } while (pages.data.isNotEmpty())
      stream.println("}")
   }

   fun search(page: Int = 0, pageSize: Int = 20,
              entity: String? = null, className: String? = null, like: Boolean = false,
              approved: Boolean? = null, status: MappingStatus? = null,
              after: Long? = null)
         = dao.search(page = page, pageSize = pageSize,
         entity = entity, className = className, like = like,
         approved = approved, after = after, status = status)

   fun getEditData(id: Long? = null): FkgEntityClassesData {
      if (id == null) return FkgEntityClassesData(approved = false)
      val t = dao.read(id) ?: return FkgEntityClassesData(approved = false)
      return FkgEntityClassesData(id = t.id,
            entity = t.entity, className = t.className,
            approved = t.approved, status = t.status)
   }

   fun getEntity(entity: String) = dao.search(page = 0, pageSize = 10, entity = entity).data

   fun edit(data: FkgEntityClassesData): FkgEntityClassesData? {
      val entity =
            if (data.id == null) FkgEntityClasses()
            else dao.read(data.id!!) ?: FkgEntityClasses()
      entity.entity = data.entity
      entity.className = data.className
      try {
         entity.classTree = getTree(classDao.read(name = entity.className!!)!!)
      } catch (th: Throwable) {
         return null
      }
      entity.approved = data.approved
      entity.status = data.status
      entity.updateEpoch = System.currentTimeMillis()
      dao.save(entity)
      return getEditData(entity.id)
   }

}