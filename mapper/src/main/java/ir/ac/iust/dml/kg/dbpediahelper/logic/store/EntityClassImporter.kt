package ir.ac.iust.dml.kg.dbpediahelper.logic.store

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.raw.utils.PrefixService
import org.springframework.stereotype.Service

@Service
class EntityClassImporter {

  val TYPE_OF_ALL_RESOURCES = PrefixService.prefixToUri(PrefixService.TYPE_OF_ALL_RESOURCES)!!
  val LABEL = PrefixService.prefixToUri(PrefixService.LABEL_URL)!!
  val INSTANCE_OF = PrefixService.prefixToUri(PrefixService.INSTANCE_OF_URL)!!
  val TYPE_URL = PrefixService.prefixToUri(PrefixService.TYPE_URL)!!

  fun writeEntityTrees(entityTree: MutableMap<String, MutableSet<String>>, store: FkgTripleDao) {
    entityTree.forEach { entity, ontologyClass ->
      var longestTree = listOf<String>("Thing")
      val allClasses = mutableSetOf<String>()

      ontologyClass.forEach {
        val t = it.split('/')
        if (t.size > longestTree.size) longestTree = t
        allClasses.addAll(t)
      }

      store.convertAndSave(entity, entity, entity.substringAfterLast('/').replace('_', ' ').trim(), LABEL)

      store.convertAndSave(entity, entity, PrefixService.getFkgOntologyClass(longestTree.first()),
          INSTANCE_OF)

      store.convertAndSave(entity, entity, TYPE_OF_ALL_RESOURCES, TYPE_URL)

      allClasses.forEach {
        store.convertAndSave(entity, entity, PrefixService.getFkgOntologyClass(it), TYPE_URL)
      }
    }
  }
}