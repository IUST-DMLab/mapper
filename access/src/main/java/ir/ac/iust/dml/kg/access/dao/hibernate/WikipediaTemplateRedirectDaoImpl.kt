package ir.ac.iust.dml.kg.access.dao.hibernate

import ir.ac.iust.dml.kg.access.dao.WikipediaTemplateRedirectDao
import ir.ac.iust.dml.kg.access.entities.WikipediaTemplateRedirect
import ir.ac.iust.dml.kg.raw.utils.PagedData
import ir.ac.iust.dml.kg.utils.hibernate.SqlJpaTools
import org.hibernate.SessionFactory
import org.hibernate.criterion.Restrictions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
open class WikipediaTemplateRedirectDaoImpl : WikipediaTemplateRedirectDao {

   @Autowired
   lateinit var sessionFactory: SessionFactory

   override fun list(pageSize: Int, page: Int): PagedData<WikipediaTemplateRedirect> {
      val session = this.sessionFactory.openSession()
      val list = SqlJpaTools.page(WikipediaTemplateRedirect::class.java, page, pageSize, session)
      session.close()
      return list
   }

   @Suppress("UNCHECKED_CAST")
   override fun read(nameFa: String?, typeFa: String?, nameEn: String?, typeEn: String?, like: Boolean):
         MutableList<WikipediaTemplateRedirect> {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(WikipediaTemplateRedirect::class.java)
      if (nameFa != null)
         if (like) criteria.add(Restrictions.like("nameFa", "%$nameFa%"))
         else criteria.add(Restrictions.eq("nameFa", nameFa))
      if (nameEn != null)
         if (like) criteria.add(Restrictions.like("nameEn", "%$nameEn%"))
         else criteria.add(Restrictions.eq("nameEn", nameEn))
      if (typeFa != null) criteria.add(Restrictions.eq("typeFa", typeFa))
      if (typeEn != null) criteria.add(Restrictions.eq("typeEn", typeEn))
      val mapping = criteria.list() as MutableList<WikipediaTemplateRedirect>
      session.close()
      return mapping
   }
}