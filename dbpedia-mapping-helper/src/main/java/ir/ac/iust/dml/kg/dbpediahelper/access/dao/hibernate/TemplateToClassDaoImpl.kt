package ir.ac.iust.dml.kg.dbpediahelper.access.dao.hibernate

import ir.ac.iust.dml.kg.dbpediahelper.access.dao.TemplateToClassDao
import ir.ac.iust.dml.kg.dbpediahelper.access.entities.TemplateToClassMapping
import ir.ac.iust.dml.kg.utils.PagedData
import ir.ac.iust.dml.kg.utils.hibernate.SqlJpaTools
import org.hibernate.SessionFactory
import org.hibernate.criterion.Restrictions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
open class TemplateToClassDaoImpl : TemplateToClassDao {

   @Autowired
   lateinit var sessionFactory: SessionFactory

   override fun save(t: TemplateToClassMapping) {
      val session = this.sessionFactory.openSession()
      val tx = session.beginTransaction()
      session.saveOrUpdate(t)
      tx.commit()
      session.close()
   }

   override fun deleteAll() {
      val session = this.sessionFactory.openSession()
      val q = session.createQuery("delete from TemplateToClassMapping")
      q.executeUpdate()
      session.close()
   }

   override fun read(templateName: String, className: String?): TemplateToClassMapping? {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(TemplateToClassMapping::class.java)
      criteria.add(Restrictions.eq("templateName", templateName))
      if (className != null) criteria.add(Restrictions.eq("className", className))
      val mapping = criteria.uniqueResult() as TemplateToClassMapping?
      session.close()
      return mapping
   }

   override fun read(id: Long): TemplateToClassMapping? {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(TemplateToClassMapping::class.java)
      criteria.add(Restrictions.eq("id", id))
      val mapping = criteria.uniqueResult() as TemplateToClassMapping?
      session.close()
      return mapping
   }

   override fun search(page: Int, pageSize: Int,
                       templateName: String?, className: String?, like: Boolean,
                       language: String?, approved: Boolean?,
                       after: Long?, noUpdateEpoch: Boolean?): PagedData<TemplateToClassMapping> {
      val session = this.sessionFactory.openSession()
      val c = SqlJpaTools.conditionalCriteria(
            templateName != null && !like, Restrictions.eq("templateName", templateName),
            templateName != null && like, Restrictions.like("templateName", "%$templateName%"),
            className != null && !like, Restrictions.eq("className", className),
            className != null && like, Restrictions.like("className", "%$className%"),
            language != null, Restrictions.eq("language", language),
            approved != null, Restrictions.eq("approved", approved),
            after != null, Restrictions.gt("updateEpoch", after),
            noUpdateEpoch != null && noUpdateEpoch, Restrictions.isNull("updateEpoch"),
            noUpdateEpoch != null && !noUpdateEpoch, Restrictions.isNotNull("updateEpoch")
      )
      val list = SqlJpaTools.page(TemplateToClassMapping::class.java, page, pageSize, session, *c)
      session.close()
      return list
   }
}