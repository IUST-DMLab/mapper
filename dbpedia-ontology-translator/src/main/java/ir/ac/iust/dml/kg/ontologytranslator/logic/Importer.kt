package ir.ac.iust.dml.kg.ontologytranslator.logic

import ir.ac.iust.dml.kg.access.dao.DBpediaClassDao
import ir.ac.iust.dml.kg.access.dao.FkgClassDao
import ir.ac.iust.dml.kg.access.entities.DBpediaClass
import ir.ac.iust.dml.kg.access.entities.FkgClass
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class Importer {

   @Autowired lateinit var sourceDao: DBpediaClassDao
   @Autowired lateinit var translationDao: FkgClassDao

   fun importFromDb() {
      var page = 0
      do {
         val paged = sourceDao.search(page = page++, pageSize = 50)
         for (clazz in paged.data)
            createNode(clazz)
      } while (paged.data.isNotEmpty())
   }

   //TODO can we remove this?
   fun fixName(name: String) = if (name.startsWith("owl#")) name.substring(4) else name

   fun createNode(clazz: DBpediaClass): FkgClass {
      val parentTranslation: FkgClass?
      if (clazz.parentId != null && clazz.parentId != clazz.id) {
         val parent = sourceDao.read(clazz.parentId!!)!!
         val translatedParent = translationDao.read(fixName(parent.name!!), null)
         if (translatedParent == null) parentTranslation = createNode(parent)
         else parentTranslation = translatedParent
      } else parentTranslation = null

      if (clazz.comment != "") println(clazz.comment)
      var translated = translationDao.read(fixName(clazz.name!!), parentTranslation?.id)
      if (translated == null) {
         translated = FkgClass(
               name = fixName(clazz.name!!),
               enLabel = clazz.enLabel,
               parentId = parentTranslation?.id,
               comment = clazz.comment)
      } else {
         translated.parentId = parentTranslation?.id
         translated.enLabel = clazz.enLabel
         translated.comment = clazz.comment
      }
      translationDao.save(translated)
      return translated
   }
}