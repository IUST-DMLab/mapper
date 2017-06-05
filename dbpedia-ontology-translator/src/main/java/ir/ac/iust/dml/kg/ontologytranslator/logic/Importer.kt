package ir.ac.iust.dml.kg.ontologytranslator.logic

import ir.ac.iust.dml.kg.access.dao.DBpediaClassDao
import ir.ac.iust.dml.kg.access.dao.FkgClassDao
import ir.ac.iust.dml.kg.access.entities.DBpediaClass
import ir.ac.iust.dml.kg.access.entities.FkgOntologyClass
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class Importer {

   @Autowired lateinit var dbpediaDao: DBpediaClassDao
   @Autowired lateinit var fkgDao: FkgClassDao

   fun importFromDb() {
      var page = 0
      do {
         val paged = dbpediaDao.search(page = page++, pageSize = 50)
         for (clazz in paged.data)
            createNode(clazz)
      } while (paged.data.isNotEmpty())
   }

   //TODO can we remove this?
   fun fixName(name: String) = if (name.startsWith("owl#")) name.substring(4) else name

  fun createNode(clazz: DBpediaClass): FkgOntologyClass {
    val parentFkgClass: FkgOntologyClass?
      if (clazz.parentId != null && clazz.parentId != clazz.id) {
         val dbpediaParentClass = dbpediaDao.read(clazz.parentId!!)!!
         val parent = fkgDao.read(fixName(dbpediaParentClass.name!!), null)
         if (parent == null) parentFkgClass = createNode(dbpediaParentClass)
         else parentFkgClass = parent
      } else parentFkgClass = null

      if (clazz.comment != "") println(clazz.comment)
      var fkgClass = fkgDao.read(fixName(clazz.name!!), parentFkgClass?.id)
      if (fkgClass == null) {
        fkgClass = FkgOntologyClass(
               name = fixName(clazz.name!!),
               enLabel = clazz.enLabel,
               parentId = parentFkgClass?.id,
               comment = clazz.comment)
      } else {
         fkgClass.parentId = parentFkgClass?.id
         fkgClass.enLabel = clazz.enLabel
         fkgClass.comment = clazz.comment
      }
      fkgDao.save(fkgClass)
      return fkgClass
   }
}