package ir.ac.iust.dml.kg.ontologytranslator.access.dao

import ir.ac.iust.dml.kg.ontologytranslator.access.entities.OntologyClassTranslation
import ir.ac.iust.dml.kg.utils.PagedData
import ir.ac.iust.dml.kg.utils.hibernate.SqlJpaTools
import org.hibernate.SessionFactory
import org.hibernate.criterion.Restrictions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
open class OntologyClassTranslationDaoImpl : OntologyClassTranslationDao {

   @Autowired
   lateinit var sessionFactory: SessionFactory

   override fun save(p: OntologyClassTranslation) {
      val session = this.sessionFactory.openSession()
      val tx = session.beginTransaction()
      session.saveOrUpdate(p)
      tx.commit()
      session.close()
   }

   override fun read(id: Long?): OntologyClassTranslation? {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(OntologyClassTranslation::class.java)
      criteria.add(Restrictions.eq("id", id))
      val mapping = criteria.uniqueResult() as? OntologyClassTranslation
      session.close()
      return mapping
   }

   override fun read(name: String, parentId: Long?): OntologyClassTranslation? {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(OntologyClassTranslation::class.java)
      criteria.add(Restrictions.eq("name", name))
      if (parentId != null) criteria.add(Restrictions.eq("parentId", parentId))
      val mapping = criteria.uniqueResult() as? OntologyClassTranslation
      session.close()
      return mapping
   }

   override fun readRoot(): OntologyClassTranslation? {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(OntologyClassTranslation::class.java)
      criteria.add(Restrictions.isNull("parentId"))
      val mapping = criteria.uniqueResult() as? OntologyClassTranslation
      session.close()
      return mapping
   }

   override fun search(name: String?, parentId: Long?, like: Boolean, approved: Boolean?,
                       pageSize: Int, page: Int): PagedData<OntologyClassTranslation> {
      val session = this.sessionFactory.openSession()
      val criteria = SqlJpaTools.condtionalCriteria(
            name != null && !like, Restrictions.eq("name", name),
            name != null && like, Restrictions.or(
            Restrictions.like("name", "%$name%"),
            Restrictions.like("faLabel", "%$name%"),
            Restrictions.like("enLabel", "%$name%")),
            parentId != null, Restrictions.eq("parentId", parentId),
            approved != null, Restrictions.eq("approved", approved)
      )
      val list = SqlJpaTools.page(OntologyClassTranslation::class.java, page, pageSize, session, *criteria)
      session.close()
      return list
   }

   @Suppress("UNCHECKED_CAST")
   override fun getChildren(id: Long): List<OntologyClassTranslation> {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(OntologyClassTranslation::class.java)
      criteria.add(Restrictions.eq("parentId", id))
      val mapping = criteria.list() as MutableList<OntologyClassTranslation>
      session.close()
      return mapping
   }

}