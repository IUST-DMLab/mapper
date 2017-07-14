package ir.ac.iust.dml.kg.dbpediahelper.logic

import ir.ac.iust.dml.kg.access.dao.FkgClassDao
import ir.ac.iust.dml.kg.access.dao.FkgEntityClassesDao
import ir.ac.iust.dml.kg.access.dao.FkgTemplateMappingDao
import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.entities.FkgEntityClasses
import ir.ac.iust.dml.kg.access.entities.FkgOntologyClass
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.dbpediahelper.logic.data.FkgEntityClassesData
import ir.ac.iust.dml.kg.dbpediahelper.logic.dump.EntityDataDumpReader
import ir.ac.iust.dml.kg.dbpediahelper.logic.type.StoreType
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import ir.ac.iust.dml.kg.raw.utils.PrefixService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Files


@Service
class EntityToClassLogic {

  @Autowired lateinit var dao: FkgEntityClassesDao
  @Autowired lateinit var templateDao: FkgTemplateMappingDao
  @Autowired lateinit var classDao: FkgClassDao
  @Autowired lateinit var storeProvider: StoreProvider
  private val logger = Logger.getLogger(this.javaClass)!!
  private val treeCache = mutableMapOf<String, String>()
  private val childrenCache = mutableMapOf<String, List<String>>()

  fun load() {
    val path = ConfigReader.getPath("wiki.folder.with.info.box", "~/.pkg/data/with_infobox")
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

  public fun getTree(ontologyClass: String) = treeCache.get(ontologyClass)

  fun getChildren(ontologyClass: String) = childrenCache[ontologyClass]

  public fun reloadTreeCache() {
    treeCache.clear()
    val allClasses = classDao.search(page = 0, pageSize = 0)
    allClasses.data.forEach { ontologyClass ->
      treeCache[ontologyClass.name!!] = getTree(ontologyClass)!!
      val children = mutableListOf<String>()
      fillChildren(ontologyClass, children)
      childrenCache[ontologyClass.name!!] = children
    }
  }

  private fun fillChildren(ontologyClass: FkgOntologyClass, list: MutableList<String> = mutableListOf()) {
    val children = classDao.getChildren(ontologyClass.id!!)
    children.forEach {
      list.add(it.name!!)
      fillChildren(it, list)
    }
  }

  private fun getTree(ontologyClass: FkgOntologyClass): String? {
    var oc: FkgOntologyClass? = ontologyClass
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

  fun writeTree(type: StoreType) = writeTree(storeProvider.getStore(type))

  fun writeTree(dao: FkgTripleDao) {

    val RDFS_LABEL_URL = PrefixService.prefixToUri(PrefixService.LABEL_URL)
    val RDFS_SUBCLASS_OF_URL = PrefixService.prefixToUri(PrefixService.SUB_CLASS_OF)
    val RDF_TYPE_URL = PrefixService.prefixToUri(PrefixService.TYPE_URL)
    val RDFS_COMMENT = PrefixService.prefixToUri(PrefixService.COMMENT_URL)
    val OWL_CLASS_URL = PrefixService.prefixToUri(PrefixService.TYPE_OF_ALL_CLASSES)

    logger.info("writing tree started.")
    treeCache.forEach { key, value ->
      val splits = value.split("/")
      val subjectUrl = PrefixService.getFkgOntologyClassUrl(key)
      if (splits.size > 1) {
        dao.save(FkgTriple(
            subject = subjectUrl,
            predicate = RDFS_SUBCLASS_OF_URL,
            objekt = PrefixService.getFkgOntologyClassUrl(splits[1])
        ), null)
      }
    }

    val classes = classDao.search(page = 0, pageSize = 0).data
    classes.forEach {
      val subjectUrl = PrefixService.getFkgOntologyClassUrl(it.name!!)
      dao.save(FkgTriple(subject = subjectUrl,
          predicate = RDF_TYPE_URL, objekt = OWL_CLASS_URL), null)
      dao.save(FkgTriple(subject = subjectUrl,
          predicate = RDFS_COMMENT, objekt = it.comment), null)
      dao.save(FkgTriple(subject = subjectUrl,
          predicate = RDFS_LABEL_URL, objekt = it.faLabel, language = "fa"), null)
      dao.save(FkgTriple(subject = subjectUrl,
          predicate = RDFS_LABEL_URL, objekt = it.faLabel, language = "fa"), null)
    }

    logger.info("writing tree ended.")
  }
}