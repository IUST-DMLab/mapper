package ir.ac.iust.dml.kg.access.dao.hibernate

import ir.ac.iust.dml.kg.access.dao.FkgTemplateMappingDao
import ir.ac.iust.dml.kg.access.entities.FkgTemplateMapping
import ir.ac.iust.dml.kg.utils.PagedData
import ir.ac.iust.dml.kg.utils.hibernate.SqlJpaTools
import org.hibernate.SessionFactory
import org.hibernate.criterion.Projections
import org.hibernate.criterion.Restrictions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
open class FkgTemplateMappingDaoImpl : FkgTemplateMappingDao {

   @Autowired
   lateinit var sessionFactory: SessionFactory

   override fun save(t: FkgTemplateMapping) {
      val session = this.sessionFactory.openSession()
      val tx = session.beginTransaction()
      session.saveOrUpdate(t)
      tx.commit()
      session.close()
   }

   override fun deleteAll() {
      val session = this.sessionFactory.openSession()
      val q = session.createQuery("delete from FkgTemplateMapping")
      q.executeUpdate()
      session.close()
   }

   override fun read(templateName: String, className: String?): FkgTemplateMapping? {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(FkgTemplateMapping::class.java)
      criteria.add(Restrictions.eq("templateName", templateName))
      if (className != null) criteria.add(Restrictions.eq("className", className))
      val mapping = criteria.uniqueResult() as FkgTemplateMapping?
      session.close()
      return mapping
   }

   override fun read(id: Long): FkgTemplateMapping? {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(FkgTemplateMapping::class.java)
      criteria.add(Restrictions.eq("id", id))
      val mapping = criteria.uniqueResult() as FkgTemplateMapping?
      session.close()
      return mapping
   }

   override fun search(page: Int, pageSize: Int,
                       templateName: String?, className: String?, like: Boolean,
                       language: String?, approved: Boolean?,
                       after: Long?, noUpdateEpoch: Boolean?): PagedData<FkgTemplateMapping> {
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
      val list = SqlJpaTools.page(FkgTemplateMapping::class.java, page, pageSize, session, *c)
      session.close()
      return list
   }

   @Suppress("UNCHECKED_CAST")
   override fun searchTemplateName(page: Int, pageSize: Int, keyword: String?): List<String> {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(FkgTemplateMapping::class.java)
      criteria.add(Restrictions.like("templateName", "%$keyword%"))
      criteria.setProjection(Projections.distinct(Projections.property("templateName")))
      val mapping = criteria.list() as MutableList<String>
      session.close()
      return mapping
   }

   @Suppress("UNCHECKED_CAST")
   override fun searchClassName(page: Int, pageSize: Int, keyword: String?): List<String> {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(FkgTemplateMapping::class.java)
      criteria.add(Restrictions.like("className", "%$keyword%"))
      criteria.setProjection(Projections.distinct(Projections.property("className")))
      val mapping = criteria.list() as MutableList<String>
      session.close()
      return mapping
   }
}