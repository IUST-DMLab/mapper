package ir.ac.iust.dml.kg.access.dao

import ir.ac.iust.dml.kg.access.dao.hibernate.FkgTripleDaoImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DaoCenter {
   @Autowired lateinit var dbpediaClass: DBpediaClassDao
   @Autowired lateinit var fkgClass: FkgClassDao
   @Autowired lateinit var fkgPropertyMapping: FkgPropertyMappingDao
   @Autowired lateinit var fkgTemplateMapping: FkgTemplateMappingDao
   @Autowired lateinit var fkgTriple: FkgTripleDaoImpl
   @Autowired lateinit var fkgTripleStats: FkgTripleStatisticsDao
   @Autowired lateinit var wikiPropertyTranslation: WikipediaPropertyTranslationDao
   @Autowired lateinit var wikiTemplateRedirect: WikipediaTemplateRedirectDao
}