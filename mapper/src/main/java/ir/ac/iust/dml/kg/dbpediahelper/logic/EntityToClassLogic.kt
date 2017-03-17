package ir.ac.iust.dml.kg.dbpediahelper.logic

import ir.ac.iust.dml.kg.access.dao.FkgClassDao
import ir.ac.iust.dml.kg.access.dao.FkgEntityClassesDao
import ir.ac.iust.dml.kg.access.dao.FkgTemplateMappingDao
import ir.ac.iust.dml.kg.access.entities.FkgClass
import ir.ac.iust.dml.kg.access.entities.FkgEntityClasses
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.dbpediahelper.logic.dump.EntityDataDumpReader
import ir.ac.iust.dml.kg.utils.ConfigReader
import ir.ac.iust.dml.kg.utils.PathWalker
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Files

@Service
class EntityToClassLogic {

   @Autowired lateinit var dao: FkgEntityClassesDao
   @Autowired lateinit var templateDao: FkgTemplateMappingDao
   @Autowired lateinit var classDao: FkgClassDao
   private val logger = Logger.getLogger(this.javaClass)!!
   private val treeCache = mutableMapOf<String, String>()

   fun load() {
      val EntityTypesFolder = "entity.types.folder"
      val config = ConfigReader.getConfig(mapOf(EntityTypesFolder to "~/.pkg/data/entity_types_folder"))
      val path = ConfigReader.getPath(config[EntityTypesFolder]!! as String)
      Files.createDirectories(path.parent)
      if (!Files.exists(path)) {
         throw Exception("There is no file ${path.toAbsolutePath()} existed.")
      }

      treeCache.clear()
      val allClasses = classDao.search(page = 0, pageSize = 0)
      for (ontologyClass in allClasses.data) {
         var oc: FkgClass? = ontologyClass
         val treeBuilder = StringBuilder().append(ontologyClass.name!!)
         while (true) {
            if (oc!!.parentId == null) break
            oc = classDao.read(oc.parentId)
            if (oc != null)
               treeBuilder.append("/").append(oc.name)
         }
         treeCache[ontologyClass.name!!] = treeBuilder.toString()
      }

      val result = PathWalker.getPath(path, Regex("\\d+\\.json"))
      for (file in result) {
         EntityDataDumpReader(file).use {
            while (it.hasNext()) {
               try {
                  val data = it.next()
                  for (infobox in data.infoboxes) {
                     val entityClass = FkgEntityClasses(entity = data.entityName)

                     val mapping = templateDao.read(infobox, null)
                     if (mapping == null) {
                        logger.info("no mapping found for $infobox of entity ${data.entityName}")
                        entityClass.className = infobox
                        entityClass.classTree = infobox
                        entityClass.status = MappingStatus.NotMapped
                     } else {
                        entityClass.className = mapping.ontologyClass
                        entityClass.classTree = treeCache[mapping.ontologyClass]
                        entityClass.status = MappingStatus.NearlyApproved
                     }
                     val old = dao.read(entityClass.entity!!, entityClass.className!!)
                     if (old == null || (entityClass.status == MappingStatus.NearlyApproved
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
}