/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.ontology

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.mapper.logic.data.InfoBoxAndCount
import ir.ac.iust.dml.kg.raw.utils.URIs
import org.springframework.stereotype.Service

@Service
class EntityClassImporter {

  private val THING = "Thing"
  private val WIKI_DUMP_URL = "http://dumps.wikimedia.org"

  fun addResourceAsThing(entity: String, store: FkgTripleDao, module: String, version: Int) =
      addResource(entity, store, THING, setOf(THING), module, version)

  private fun addResource(entity: String, store: FkgTripleDao,
                          instanceOf: String, classes: Set<String>, module: String, version: Int) {
    val subject = URIs.getFkgResourceUri(entity)

    val fullLabel = entity.substringAfterLast('/').replace('_', ' ').trim()
    store.convertAndSave(WIKI_DUMP_URL, subject, URIs.variantLabel, fullLabel, module, version)
    if (fullLabel.contains("(")) {
      val label = fullLabel.substringBefore("(").trim()
      store.convertAndSave(WIKI_DUMP_URL, subject, URIs.label, label, module, version)
      store.convertAndSave(WIKI_DUMP_URL, subject, URIs.variantLabel, label, module, version)
    } else store.convertAndSave(WIKI_DUMP_URL, subject, URIs.label, fullLabel, module, version)

    store.convertAndSave(WIKI_DUMP_URL, subject, URIs.type, URIs.typeOfAllResources, module, version)

    store.convertAndSave(WIKI_DUMP_URL, subject, URIs.instanceOf,
        URIs.getFkgOntologyClassPrefixed(instanceOf), module, version)
    classes.forEach {
      store.convertAndSave(WIKI_DUMP_URL, subject, URIs.type, URIs.getFkgOntologyClassPrefixed(it),
          module, version)
    }
  }

  fun writeEntityTrees(entity: String, trees: MutableSet<InfoBoxAndCount>, store: FkgTripleDao,
                       module: String, version: Int) {
    val allClasses = mutableSetOf<String>()
    val mainClass = trees.toList().sortedByDescending { it.propertyCount }
        .firstOrNull()?.tree?.firstOrNull() ?: THING
    trees.forEach { it.tree.forEach { allClasses.add(it) } }
    addResource(entity, store, mainClass, allClasses, module, version)
  }
}