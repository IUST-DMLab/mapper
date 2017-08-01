package ir.ac.iust.dml.kg.mapper.logic

import ir.ac.iust.dml.kg.access.dao.FkgClassDao
import ir.ac.iust.dml.kg.access.dao.FkgEntityClassesDao
import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.entities.FkgOntologyClass
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.mapper.logic.type.StoreType
import ir.ac.iust.dml.kg.raw.utils.URIs
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class EntityToClassLogic {

  @Autowired lateinit var dao: FkgEntityClassesDao
  @Autowired lateinit var classDao: FkgClassDao
  @Autowired lateinit var storeProvider: StoreProvider
  private val logger = Logger.getLogger(this.javaClass)!!
  private val treeCache = mutableMapOf<String, String>()
//  private val childrenCache = mutableMapOf<String, List<String>>()

//  fun getTree(ontologyClass: String) = treeCache[ontologyClass]

//  fun getChildren(ontologyClass: String) = childrenCache[ontologyClass]

  private fun reloadTreeCache() {
    treeCache.clear()
    val allClasses = classDao.search(page = 0, pageSize = 0)
    allClasses.data.forEach { ontologyClass ->
      treeCache[ontologyClass.name!!] = getTree(ontologyClass)!!
//      val children = mutableListOf<String>()
//      fillChildren(ontologyClass, children)
//      childrenCache[ontologyClass.name!!] = children
    }
  }

  fun writeTree(type: StoreType) = writeTree(storeProvider.getStore(type))

  val otherLabelsSplitRegex = Regex("(\\s*[,،]\\s*)+")
  fun writeTree(dao: FkgTripleDao) {
    reloadTreeCache()
    logger.info("writing tree started.")
    treeCache.forEach { key, value ->
      val splits = value.split("/")
      val subjectUrl = URIs.getFkgOntologyClassUri(key)
      if (splits.size > 1) {
        dao.save(FkgTriple(
            subject = subjectUrl,
            predicate = URIs.subClassOf,
            objekt = URIs.getFkgOntologyClassUri(splits[1])
        ), null)
      }
    }

    val classes = classDao.search(page = 0, pageSize = 0).data
    classes.forEach {
      val subjectUrl = URIs.getFkgOntologyClassUri(it.name!!)
      dao.save(FkgTriple(subject = subjectUrl,
          predicate = URIs.type, objekt = URIs.typeOfAllClasses), null)
      val name = subjectUrl.substring(subjectUrl.indexOf("/ontology/") + 10)
      dao.save(FkgTriple(subject = subjectUrl, predicate = URIs.name, objekt = name), null)
      if (it.wasDerivedFrom != null)
        dao.save(FkgTriple(subject = subjectUrl, predicate = URIs.wasDerivedFrom, objekt = it.wasDerivedFrom!!), null)
      if (it.comment != null && it.comment!!.isNotBlank())
        dao.save(FkgTriple(subject = subjectUrl,
            predicate = URIs.comment, objekt = it.comment), null)
      dao.save(FkgTriple(subject = subjectUrl,
          predicate = URIs.label, objekt = it.faLabel, language = "fa"), null)
      dao.save(FkgTriple(subject = subjectUrl,
          predicate = URIs.label, objekt = it.enLabel, language = "en"), null)
      if (it.faOtherLabels != null)
        if (it.faOtherLabels!!.isNotBlank())
          it.faOtherLabels!!.split(otherLabelsSplitRegex).filter { it.trim().isNotEmpty() }.forEach {
            dao.save(FkgTriple(subject = subjectUrl,
                predicate = URIs.variantLabel, objekt = it, language = "fa"), null)
          }
    }

    dao.flush()

    logger.info("writing tree ended.")
  }

//  private fun fillChildren(ontologyClass: FkgOntologyClass, list: MutableList<String> = mutableListOf()) {
//    val children = classDao.getChildren(ontologyClass.id!!)
//    children.forEach {
//      list.add(it.name!!)
//      fillChildren(it, list)
//    }
//  }

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
}