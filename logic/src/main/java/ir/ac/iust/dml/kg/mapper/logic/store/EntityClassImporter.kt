package ir.ac.iust.dml.kg.mapper.logic.store

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.mapper.logic.data.InfoBoxAndCount
import ir.ac.iust.dml.kg.raw.utils.URIs
import org.springframework.stereotype.Service

@Service
class EntityClassImporter {

  val THING = "Thing"
  val WIKI_DUMP_URL = "http://dumps.wikimedia.org"

  fun addResourceAsThing(entity: String, store: FkgTripleDao) = addResource(entity, store, THING, setOf(THING))

  private fun addResource(entity: String, store: FkgTripleDao, instanceOf: String, classes: Set<String>) {
    val subject = URIs.getFkgResourceUri(entity)

    val fullLabel = entity.substringAfterLast('/').replace('_', ' ').trim()
    store.convertAndSave(WIKI_DUMP_URL, subject, fullLabel, URIs.variantLabel)
    if (fullLabel.contains("(")) {
      val label = fullLabel.substringBefore("(").trim()
      store.convertAndSave(WIKI_DUMP_URL, subject, label, URIs.label)
      store.convertAndSave(WIKI_DUMP_URL, subject, label, URIs.variantLabel)
    } else store.convertAndSave(WIKI_DUMP_URL, subject, fullLabel, URIs.label)

    store.convertAndSave(WIKI_DUMP_URL, subject, URIs.typeOfAllResources, URIs.type)

    store.convertAndSave(WIKI_DUMP_URL, subject, URIs.getFkgOntologyClassPrefixed(instanceOf), URIs.instanceOf)
    classes.forEach { store.convertAndSave(WIKI_DUMP_URL, subject, URIs.getFkgOntologyClassPrefixed(it), URIs.type) }
  }

  fun writeEntityTrees(entity: String, trees: MutableSet<InfoBoxAndCount>, store: FkgTripleDao) {
    val allClasses = mutableSetOf<String>()
    val mainClass = trees.toList().sortedByDescending { it.propertyCount }
        .firstOrNull()?.tree?.firstOrNull() ?: THING
    trees.forEach { it.tree.forEach { allClasses.add(it) } }
    addResource(entity, store, mainClass, allClasses)
  }
}