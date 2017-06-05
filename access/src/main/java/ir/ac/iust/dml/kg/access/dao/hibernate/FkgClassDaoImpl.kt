package ir.ac.iust.dml.kg.access.dao.hibernate

import ir.ac.iust.dml.kg.access.dao.FkgClassDao
import ir.ac.iust.dml.kg.access.entities.FkgOntologyClass
import ir.ac.iust.dml.kg.access.utils.SqlJpaTools
import ir.ac.iust.dml.kg.raw.utils.PagedData
import org.hibernate.SessionFactory
import org.hibernate.criterion.Restrictions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
open class FkgClassDaoImpl : FkgClassDao {

   @Autowired
   lateinit var sessionFactory: SessionFactory

   override fun save(p: FkgOntologyClass) {
      val session = this.sessionFactory.openSession()
      val tx = session.beginTransaction()
      session.saveOrUpdate(p)
      tx.commit()
      session.close()
   }

   override fun read(id: Long?): FkgOntologyClass? {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(FkgOntologyClass::class.java)
      criteria.add(Restrictions.eq("id", id))
      val mapping = criteria.uniqueResult() as? FkgOntologyClass
      session.close()
      return mapping
   }

   override fun read(name: String, parentId: Long?): FkgOntologyClass? {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(FkgOntologyClass::class.java)
      criteria.add(Restrictions.eq("name", name))
      if (parentId != null) criteria.add(Restrictions.eq("parentId", parentId))
      val list = criteria.list()
      val mapping: FkgOntologyClass?
      if (list.isEmpty()) {
         println("No instance for $name")
         mapping = null
      } else {
         if (list.size > 1) println("multiple instances for $name")
         mapping = list[0] as? FkgOntologyClass
      }
      session.close()
      return mapping
   }

   override fun readRoot(): FkgOntologyClass? {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(FkgOntologyClass::class.java)
      criteria.add(Restrictions.isNull("parentId"))
      val mapping = criteria.uniqueResult() as? FkgOntologyClass
      session.close()
      return mapping
   }

   override fun search(name: String?, parentId: Long?, like: Boolean, approved: Boolean?, hasFarsi: Boolean?,
                       pageSize: Int, page: Int): PagedData<FkgOntologyClass> {
      val session = this.sessionFactory.openSession()
      val criteria = SqlJpaTools.conditionalCriteria(
            name != null && !like, Restrictions.eq("name", name),
            name != null && like, Restrictions.or(
            Restrictions.like("name", "%$name%"),
            Restrictions.like("faLabel", "%$name%"),
            Restrictions.like("enLabel", "%$name%")),
            parentId != null, Restrictions.eq("parentId", parentId),
            approved != null, Restrictions.eq("approved", approved),
            hasFarsi != null && hasFarsi, Restrictions.isNotNull("faLabel"),
            hasFarsi != null && !hasFarsi, Restrictions.isNull("faLabel")
      )
      val list = SqlJpaTools.page(FkgOntologyClass::class.java, page, pageSize, session, null, *criteria)
      session.close()
      return list
   }

   @Suppress("UNCHECKED_CAST")
   override fun getChildren(id: Long): List<FkgOntologyClass> {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(FkgOntologyClass::class.java)
      criteria.add(Restrictions.eq("parentId", id))
      val mapping = criteria.list() as MutableList<FkgOntologyClass>
      session.close()
      return mapping
   }

}