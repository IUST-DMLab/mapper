package ir.ac.iust.dml.kg.dbpediahelper.access.dao

import ir.ac.iust.dml.kg.dbpediahelper.access.entities.DBpediaPropertyMapping
import ir.ac.iust.dml.kg.dbpediahelper.access.entities.TemplatePropertyMapping
import ir.ac.iust.dml.kg.utils.PagedData
import ir.ac.iust.dml.kg.utils.hibernate.SqlJpaTools
import org.hibernate.SessionFactory
import org.hibernate.criterion.Projections
import org.hibernate.criterion.Restrictions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
open class DBpediaPropertyMappingDaoImpl : DBpediaPropertyMappingDao {

   @Suppress("UNCHECKED_CAST")
   override fun read(language: String?, clazz: String?, type: String?,
                     like: Boolean, hasClass: Boolean, templateProperty: String?, ontologyProperty: String?):
         MutableList<DBpediaPropertyMapping> {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(DBpediaPropertyMapping::class.java)
      if (language != null) criteria.add(Restrictions.eq("language", language))
      if (clazz != null) criteria.add(Restrictions.eq("clazz", clazz))
      if (type != null) criteria.add(Restrictions.eq("type", type))
      if (hasClass) criteria.add(Restrictions.isNotNull("clazz"))
      if (like) {
         if (templateProperty != null)
            criteria.add(Restrictions.like("templateProperty", "%$templateProperty%"))
         if (ontologyProperty != null)
            criteria.add(Restrictions.like("ontologyProperty", "%$ontologyProperty%"))
      } else {
         if (templateProperty != null)
            criteria.add(Restrictions.eq("templateProperty", templateProperty))
         if (ontologyProperty != null)
            criteria.add(Restrictions.eq("ontologyProperty", ontologyProperty))
      }
      val mapping = criteria.list() as MutableList<DBpediaPropertyMapping>
      session.close()
      return mapping
   }

   @Autowired
   lateinit var sessionFactory: SessionFactory

   override fun save(p: DBpediaPropertyMapping) {
      val session = this.sessionFactory.openSession()
      val tx = session.beginTransaction()
      session.saveOrUpdate(p)
      tx.commit()
      session.close()
   }

   override fun deleteAll() {
      val session = this.sessionFactory.openSession()
      val q = session.createQuery("delete from DBpediaPropertyMapping")
      q.executeUpdate()
      session.close()
   }

   @Suppress("UNCHECKED_CAST")
   override fun list(pageSize: Int, page: Int, hasClass: Boolean): PagedData<DBpediaPropertyMapping> {
      val session = this.sessionFactory.openSession()
      val criteria = SqlJpaTools.condtionalCriteria(hasClass, Restrictions.isNotNull("clazz"))
      val list = SqlJpaTools.page(DBpediaPropertyMapping::class.java, page, pageSize, session, *criteria)
      session.close()
      return list
   }

   @Suppress("UNCHECKED_CAST")
   override fun readOntologyProperty(templateProperty: String): List<String> {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(DBpediaPropertyMapping::class.java)
      criteria.add(Restrictions.eq("templateProperty", templateProperty))
      criteria.setProjection(Projections.distinct(Projections.property("ontologyProperty")))
      val mapping = criteria.list() as MutableList<String>
      session.close()
      return mapping
   }

   @Suppress("UNCHECKED_CAST")
   override fun readByEnTitle(type: String?, enProperty: String): MutableList<TemplatePropertyMapping> {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(TemplatePropertyMapping::class.java)
      if (type != null) criteria.add(Restrictions.like("type", "%$type%"))
      criteria.add(Restrictions.like("enProperty", "%$enProperty%"))
      val mapping = criteria.list() as MutableList<TemplatePropertyMapping>
      session.close()
      return mapping
   }

   @Suppress("UNCHECKED_CAST")
   override fun listTemplatePropertyMapping(pageSize: Int, page: Int): PagedData<TemplatePropertyMapping> {
      val session = this.sessionFactory.openSession()
      val list = SqlJpaTools.page(TemplatePropertyMapping::class.java, page, pageSize, session)
      session.close()
      return list
   }
}