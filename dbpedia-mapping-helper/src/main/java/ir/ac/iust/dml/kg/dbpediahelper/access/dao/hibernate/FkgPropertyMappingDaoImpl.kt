package ir.ac.iust.dml.kg.dbpediahelper.access.dao.hibernate

import ir.ac.iust.dml.kg.dbpediahelper.access.dao.FkgPropertyMappingDao
import ir.ac.iust.dml.kg.dbpediahelper.access.entities.FkgPropertyMapping
import ir.ac.iust.dml.kg.dbpediahelper.access.entities.WikipediaPropertyTranslation
import ir.ac.iust.dml.kg.dbpediahelper.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.utils.PagedData
import ir.ac.iust.dml.kg.utils.hibernate.SqlJpaTools
import org.hibernate.Criteria
import org.hibernate.SessionFactory
import org.hibernate.criterion.Projections
import org.hibernate.criterion.Restrictions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
open class FkgPropertyMappingDaoImpl : FkgPropertyMappingDao {

   @Suppress("UNCHECKED_CAST")
   override fun read(language: String?, clazz: String?, type: String?,
                     like: Boolean, hasClass: Boolean, templateProperty: String?,
                     secondTemplateProperty: String?, ontologyProperty: String?, status: MappingStatus?):
         MutableList<FkgPropertyMapping> {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(FkgPropertyMapping::class.java)
      if (language != null) criteria.add(Restrictions.eq("language", language))
      if (status != null) criteria.add(Restrictions.eq("status", status))
      if (clazz != null) criteria.add(Restrictions.eq("clazz", clazz))
      if (type != null) criteria.add(Restrictions.eq("type", type))
      if (hasClass) criteria.add(Restrictions.isNotNull("clazz"))
      if (like) {
         if (templateProperty != null)
            criteria.add(Restrictions.like("templateProperty", "%$templateProperty%"))
         if (ontologyProperty != null)
            criteria.add(Restrictions.like("ontologyProperty", "%$ontologyProperty%"))
      } else {
         if (templateProperty != null && secondTemplateProperty != null && templateProperty != secondTemplateProperty)
            criteria.add(Restrictions.or(
                  Restrictions.eq("templateProperty", templateProperty),
                  Restrictions.eq("templateProperty", secondTemplateProperty)))
         else if (templateProperty != null)
            criteria.add(Restrictions.eq("templateProperty", templateProperty))
         if (ontologyProperty != null)
            criteria.add(Restrictions.eq("ontologyProperty", ontologyProperty))
      }
      val mapping = criteria.list() as MutableList<FkgPropertyMapping>
      session.close()
      return mapping
   }

   @Autowired
   lateinit var sessionFactory: SessionFactory

   override fun save(p: FkgPropertyMapping) {
      val session = this.sessionFactory.openSession()
      val tx = session.beginTransaction()
      session.saveOrUpdate(p)
      tx.commit()
      session.close()
   }

   override fun deleteAll() {
      val session = this.sessionFactory.openSession()
      val q = session.createQuery("delete from FkgPropertyMapping")
      q.executeUpdate()
      session.close()
   }

   @Suppress("UNCHECKED_CAST")
   override fun list(pageSize: Int, page: Int, hasClass: Boolean): PagedData<FkgPropertyMapping> {
      val session = this.sessionFactory.openSession()
      val criteria = SqlJpaTools.conditionalCriteria(hasClass, Restrictions.isNotNull("clazz"))
      val list = SqlJpaTools.page(FkgPropertyMapping::class.java, page, pageSize, session, *criteria)
      session.close()
      return list
   }

   @Suppress("UNCHECKED_CAST")
   override fun readOntologyProperty(templateProperty: String): List<String> {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(FkgPropertyMapping::class.java)
      criteria.add(Restrictions.eq("templateProperty", templateProperty))
      criteria.setProjection(Projections.distinct(Projections.property("ontologyProperty")))
      val mapping = criteria.list() as MutableList<String>
      session.close()
      return mapping
   }

   @Suppress("UNCHECKED_CAST")
   override fun readByEnTitle(type: String?, enProperty: String): MutableList<WikipediaPropertyTranslation> {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(WikipediaPropertyTranslation::class.java)
      if (type != null) criteria.add(Restrictions.like("type", "%$type%"))
      criteria.add(Restrictions.like("enProperty", "%$enProperty%"))
      val mapping = criteria.list() as MutableList<WikipediaPropertyTranslation>
      session.close()
      return mapping
   }

   @Suppress("UNCHECKED_CAST")
   override fun listTemplatePropertyMapping(pageSize: Int, page: Int): PagedData<WikipediaPropertyTranslation> {
      val session = this.sessionFactory.openSession()
      val list = SqlJpaTools.page(WikipediaPropertyTranslation::class.java, page, pageSize, session)
      session.close()
      return list
   }

   @Suppress("UNCHECKED_CAST")
   override fun listUniqueProperties(language: String?, pageSize: Int, page: Int): List<String> {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(FkgPropertyMapping::class.java)
      if (language != null) criteria.add(Restrictions.like("language", language))
      criteria.setProjection(Projections.distinct(Projections.property("templateProperty")))
      criteria.setFirstResult(page * pageSize)
      criteria.setMaxResults(pageSize)
      val mapping = criteria.list() as MutableList<String>
      session.close()
      return mapping
   }

   override fun countTemplateProperties(templateProperty: String): Long {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(FkgPropertyMapping::class.java)
      criteria.add(Restrictions.eq("templateProperty", templateProperty))
      val count = criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
            .setProjection(Projections.rowCount()).uniqueResult() as Long
      session.close()
      return count
   }

   @Suppress("UNCHECKED_CAST")
   override fun listUniqueOntologyProperties(templateProperty: String): List<String> {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(FkgPropertyMapping::class.java)
      criteria.add(Restrictions.eq("templateProperty", templateProperty))
      criteria.setProjection(Projections.distinct(Projections.property("ontologyProperty")))
      val mapping = criteria.list() as MutableList<String>
      session.close()
      return mapping
   }

   override fun countOntologyProperties(templateProperty: String, ontologyProperty: String): Long {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(FkgPropertyMapping::class.java)
      criteria.add(Restrictions.eq("templateProperty", templateProperty))
      criteria.add(Restrictions.eq("ontologyProperty", ontologyProperty))
      val count = criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
            .setProjection(Projections.rowCount()).uniqueResult() as Long
      session.close()
      return count
   }
}