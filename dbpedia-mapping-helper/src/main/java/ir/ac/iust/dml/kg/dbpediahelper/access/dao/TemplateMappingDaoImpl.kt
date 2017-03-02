package ir.ac.iust.dml.kg.dbpediahelper.access.dao

import ir.ac.iust.dml.kg.dbpediahelper.access.entities.WikiTemplateMapping
import ir.ac.iust.dml.kg.utils.PagedData
import ir.ac.iust.dml.kg.utils.hibernate.SqlJpaTools
import org.hibernate.SessionFactory
import org.hibernate.criterion.Restrictions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
open class TemplateMappingDaoImpl : TemplateMappingDao {

   @Autowired
   lateinit var sessionFactory: SessionFactory

   override fun list(pageSize: Int, page: Int): PagedData<WikiTemplateMapping> {
      val session = this.sessionFactory.openSession()
      val list = SqlJpaTools.page(WikiTemplateMapping::class.java, page, pageSize, session)
      session.close()
      return list
   }

   @Suppress("UNCHECKED_CAST")
   override fun read(nameFa: String?, typeFa: String?, nameEn: String?, typeEn: String?, like: Boolean):
         MutableList<WikiTemplateMapping> {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(WikiTemplateMapping::class.java)
      if (nameFa != null)
         if (like) criteria.add(Restrictions.like("nameFa", "%$nameFa%"))
         else criteria.add(Restrictions.eq("nameFa", nameFa))
      if (nameEn != null)
         if (like) criteria.add(Restrictions.like("nameEn", "%$nameEn%"))
         else criteria.add(Restrictions.eq("nameEn", nameEn))
      if (typeFa != null) criteria.add(Restrictions.eq("typeFa", typeFa))
      if (typeEn != null) criteria.add(Restrictions.eq("typeEn", typeEn))
      val mapping = criteria.list() as MutableList<WikiTemplateMapping>
      session.close()
      return mapping
   }
}