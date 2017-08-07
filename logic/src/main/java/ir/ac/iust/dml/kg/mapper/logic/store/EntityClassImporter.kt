package ir.ac.iust.dml.kg.mapper.logic.store

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.mapper.logic.data.InfoBoxAndCount
import ir.ac.iust.dml.kg.raw.utils.URIs
import org.springframework.stereotype.Service

@Service
class EntityClassImporter {

  val THING = "Thing"
  val WIKI_DUMP_URL = "http://dumps.wikimedia.org"

  fun addResourceAsThing(entity: String, store: FkgTripleDao, module: String?) =
      addResource(entity, store, THING, setOf(THING), module)

  private fun addResource(entity: String, store: FkgTripleDao, instanceOf: String, classes: Set<String>, module: String?) {
    val subject = URIs.getFkgResourceUri(entity)

    val fullLabel = entity.substringAfterLast('/').replace('_', ' ').trim()
    store.convertAndSave(WIKI_DUMP_URL, subject, fullLabel, URIs.variantLabel, module)
    if (fullLabel.contains("(")) {
      val label = fullLabel.substringBefore("(").trim()
      store.convertAndSave(WIKI_DUMP_URL, subject, label, URIs.label, module)
      store.convertAndSave(WIKI_DUMP_URL, subject, label, URIs.variantLabel, module)
    } else store.convertAndSave(WIKI_DUMP_URL, subject, fullLabel, URIs.label, module)

    store.convertAndSave(WIKI_DUMP_URL, subject, URIs.typeOfAllResources, URIs.type, module)

    store.convertAndSave(WIKI_DUMP_URL, subject, URIs.getFkgOntologyClassPrefixed(instanceOf), URIs.instanceOf, module)
    classes.forEach { store.convertAndSave(WIKI_DUMP_URL, subject, URIs.getFkgOntologyClassPrefixed(it), URIs.type, module) }
  }

  fun writeEntityTrees(entity: String, trees: MutableSet<InfoBoxAndCount>, store: FkgTripleDao, module: String?) {
    val allClasses = mutableSetOf<String>()
    val mainClass = trees.toList().sortedByDescending { it.propertyCount }
        .firstOrNull()?.tree?.firstOrNull() ?: THING
    trees.forEach { it.tree.forEach { allClasses.add(it) } }
    addResource(entity, store, mainClass, allClasses, module)
  }
}