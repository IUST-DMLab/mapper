package ir.ac.iust.dml.kg.mapper.logic.store

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.mapper.logic.data.InfoBoxAndCount
import ir.ac.iust.dml.kg.raw.utils.PrefixService
import org.springframework.stereotype.Service

@Service
class EntityClassImporter {

  val TYPE_OF_ALL_RESOURCES = PrefixService.prefixToUri(PrefixService.TYPE_OF_ALL_RESOURCES)!!
  val LABEL = PrefixService.prefixToUri(PrefixService.LABEL_URL)!!
  val VARIANT_LABEL = PrefixService.prefixToUri(PrefixService.VARIANT_LABEL_URL)!!
  val INSTANCE_OF = PrefixService.prefixToUri(PrefixService.INSTANCE_OF_URL)!!
  val TYPE_URL = PrefixService.prefixToUri(PrefixService.TYPE_URL)!!
  val THING = "Thing"
  val WIKI_DUMP_URL = "http://dumps.wikimedia.org"

  fun addResourceAsThing(entity: String, store: FkgTripleDao) = addResource(entity, store, THING, setOf(THING))

  private fun addResource(entity: String, store: FkgTripleDao, instanceOf: String, classes: Set<String>) {
    val subject = PrefixService.getFkgResourceUrl(entity)

    val fullLabel = entity.substringAfterLast('/').replace('_', ' ').trim()
    store.convertAndSave(WIKI_DUMP_URL, subject, fullLabel, VARIANT_LABEL)
    if (fullLabel.contains("(")) {
      val label = fullLabel.substringBefore("(").trim()
      store.convertAndSave(WIKI_DUMP_URL, subject, label, LABEL)
      store.convertAndSave(WIKI_DUMP_URL, subject, label, VARIANT_LABEL)
    } else store.convertAndSave(WIKI_DUMP_URL, subject, fullLabel, LABEL)

    store.convertAndSave(WIKI_DUMP_URL, subject, TYPE_OF_ALL_RESOURCES, TYPE_URL)

    store.convertAndSave(WIKI_DUMP_URL, subject, PrefixService.getFkgOntologyClass(instanceOf), INSTANCE_OF)
    classes.forEach { store.convertAndSave(WIKI_DUMP_URL, subject, PrefixService.getFkgOntologyClass(it), TYPE_URL) }
  }

  fun writeEntityTrees(entity: String, trees: MutableSet<InfoBoxAndCount>, store: FkgTripleDao) {
    val allClasses = mutableSetOf<String>()
    val mainClass = trees.toList().sortedByDescending { it.propertyCount }
        .firstOrNull()?.tree?.firstOrNull() ?: THING
    trees.forEach { it.tree.forEach { allClasses.add(it) } }
    addResource(entity, store, mainClass, allClasses)
  }
}