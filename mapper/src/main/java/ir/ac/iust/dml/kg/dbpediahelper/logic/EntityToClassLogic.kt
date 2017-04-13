package ir.ac.iust.dml.kg.dbpediahelper.logic

import ir.ac.iust.dml.kg.access.dao.FkgClassDao
import ir.ac.iust.dml.kg.access.dao.FkgEntityClassesDao
import ir.ac.iust.dml.kg.access.dao.FkgTemplateMappingDao
import ir.ac.iust.dml.kg.access.dao.knowldegestore.KnowledgeStoreFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.entities.FkgClass
import ir.ac.iust.dml.kg.access.entities.FkgEntityClasses
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.dbpediahelper.logic.data.FkgEntityClassesData
import ir.ac.iust.dml.kg.dbpediahelper.logic.dump.EntityDataDumpReader
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import ir.ac.iust.dml.kg.raw.utils.dump.triple.TripleJsonFileReader
import ir.ac.iust.dml.kg.utils.PrefixService
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
   val knowledgeStoreDao = KnowledgeStoreFkgTripleDaoImpl()

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
//
//   fun exportTypes(after: Long?, response: HttpServletResponse) {
//      val gson = Gson()
//      response.contentType = "text/html; charset=UTF-8"
//      response.characterEncoding = "UTF-8"
//      val stream = response.writer
//      stream.printlnprintln("{")
//      var page = 0
//      do {
//         val pages = dao.search(page++, 1000, after = after)
//         pages.data.forEachIndexed { index, d ->
//            if(d.entity!!.contains("\"")) return
//            if(d.classTree != null && d.classTree!!.contains("\"")) return
//            stream.print("\"${d.entity!!}\":")
//            if (d.classTree != null) stream.print(gson.toJson(d.classTree!!.split('/')))
//            else stream.print("[]")
//            if (pages.page < pages.pageCount - 1 || index < pages.data.size - 1) stream.println(",")
//            else stream.println()
//         }
//      } while (pages.data.isNotEmpty())
//      stream.println("}")
//   }

   fun exportTypes(after: Long?): Map<String, List<String>> {
      val result = mutableMapOf<String, List<String>>()
      var page = 0
      do {
         val pages = dao.search(page++, 1000, after = after)
         pages.data.forEach {
            result[it.entity!!] = if (it.classTree == null) mutableListOf() else it.classTree!!.split('/')
         }
      } while (pages.data.isNotEmpty())
      return result
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

   fun classParents(): Map<String, List<String>> {
      reloadTreeCache()
      val result = mutableMapOf<String, List<String>>()
      treeCache.keys.forEach { key -> result[key] = treeCache[key]!!.split("/").filter { key != it } }
      return result
   }

   fun writeEntityTypesToKnowledgeStore() {
      val startTime = System.currentTimeMillis()

      val maxNumberOfFiles = ConfigReader.getInt("entity.process.max.files", "30")

      val path = ConfigReader.getPath("wiki.triple.input.folder", "~/.pkg/data/triples")
      Files.createDirectories(path.parent)
      if (!Files.exists(path)) {
         throw Exception("There is no file ${path.toAbsolutePath()} existed.")
      }

      val addedEntities = mutableSetOf<String>()
      reloadTreeCache()

      treeCache.forEach { key, value ->
         val splits = value.split("/")
         if (splits.size > 1)
            knowledgeStoreDao.save(FkgTriple(
                  subject = "http://dbpedia.org/ontology/" + key,
                  predicate = "rdfs:subClassOf",
                  objekt = "http://dbpedia.org/ontology/" + splits[1]
            ), null)
      }

      val result = PathWalker.getPath(path, Regex("\\d+-infoboxes\\.json"))
      var tripleNumber = 0
      var entityNumber = 0
      result.subList(0, Math.min(result.size, maxNumberOfFiles)).forEachIndexed { index, p ->
         TripleJsonFileReader(p).use { reader ->
            while (reader.hasNext()) {
               val s = System.currentTimeMillis()
               val triple = reader.next()
               try {
                  tripleNumber++
                  if (tripleNumber % 5000 == 0)
                     logger.info("triple number is $tripleNumber. \tfile: $index\t" +
                           "time: ${(System.currentTimeMillis() - startTime) / 1000}\tsecs")

                  if (triple.subject == null) continue
                  if (!triple.subject!!.contains("://")) continue
                  val entity = triple.subject!!.substringAfterLast('/').replace('_', ' ')
                  if (addedEntities.contains(entity)) continue
                  entityNumber++
                  if (entityNumber % 1000 == 0)
                     logger.info("entity number is $entityNumber.")
                  addedEntities.add(entity)

                  knowledgeStoreDao.save(FkgTriple(
                        subject = triple.subject!!,
                        predicate = "rdfs:label",
                        objekt = entity
                  ), null)

                  val mapping = templateDao.read(triple.templateNameFull!!, null)
                  if (mapping != null) {
                     knowledgeStoreDao.save(FkgTriple(
                           subject = PrefixService.convertFkgResource(triple.subject!!),
                           predicate = "fkg:instanceOf",
                           objekt = "http://dbpedia.org/ontology/" + mapping.ontologyClass
                     ), null)
                     treeCache[mapping.ontologyClass]!!.split("/").forEach {
                        knowledgeStoreDao.save(FkgTriple(
                              subject = PrefixService.convertFkgResource(triple.subject!!),
                              predicate = "rdf:type",
                              objekt = "http://dbpedia.org/ontology/" + it
                        ), null)
                     }
                  } else {
                     val typeUrl = "http://fa.wikipedia.org/wiki/template/" + triple.templateNameFull!!.replace(' ', '_')
                     knowledgeStoreDao.save(FkgTriple(
                           subject = PrefixService.convertFkgResource(triple.subject!!),
                           predicate = "fkg:instanceOf",
                           objekt = typeUrl
                     ), null)
                     knowledgeStoreDao.save(FkgTriple(
                           subject = PrefixService.convertFkgResource(triple.subject!!),
                           predicate = "rdf:type",
                           objekt = typeUrl
                     ), null)
                  }
               } catch (th: Throwable) {
                  logger.info("triple: $triple")
                  logger.error(th)
               }
            }
         }
      }
   }
}