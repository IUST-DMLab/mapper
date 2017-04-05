package ir.ac.iust.dml.kg.access.dao.hibernate

import ir.ac.iust.dml.kg.access.dao.FkgEntityClassesDao
import ir.ac.iust.dml.kg.access.entities.FkgEntityClasses
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.utils.PagedData
import ir.ac.iust.dml.kg.utils.hibernate.SqlJpaTools
import org.hibernate.SessionFactory
import org.hibernate.criterion.Projections
import org.hibernate.criterion.Restrictions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
open class FkgEntityClassesDaoImpl : FkgEntityClassesDao {

   @Autowired
   lateinit var sessionFactory: SessionFactory

   override fun save(p: FkgEntityClasses) {
      val session = this.sessionFactory.openSession()
      val tx = session.beginTransaction()
      session.saveOrUpdate(p)
      tx.commit()
      session.close()
   }

   override fun delete(p: FkgEntityClasses) {
      val session = this.sessionFactory.openSession()
      val tx = session.beginTransaction()
      session.delete(p)
      tx.commit()
      session.close()
   }

   override fun deleteAll() {
      val session = this.sessionFactory.openSession()
      val q = session.createQuery("delete from FkgEntityClasses")
      q.executeUpdate()
      session.close()
   }

   @Suppress("UNCHECKED_CAST")
   override fun list(pageSize: Int, page: Int): PagedData<FkgEntityClasses> {
      val session = this.sessionFactory.openSession()
      val list = SqlJpaTools.page(FkgEntityClasses::class.java, page, pageSize, session, null)
      session.close()
      return list
   }

   override fun read(id: Long): FkgEntityClasses? {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(FkgEntityClasses::class.java)
      criteria.add(Restrictions.eq("id", id))
      val mapping = criteria.uniqueResult() as FkgEntityClasses?
      session.close()
      return mapping
   }

   override fun read(entity: String, className: String): FkgEntityClasses? {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(FkgEntityClasses::class.java)
      criteria.add(Restrictions.eq("entity", entity))
      criteria.add(Restrictions.eq("className", className))
      val mapping = criteria.uniqueResult() as FkgEntityClasses?
      session.close()
      return mapping
   }

   @Suppress("UNCHECKED_CAST")
   override fun readClassesOfEntity(entity: String): List<String> {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(FkgEntityClasses::class.java)
      criteria.add(Restrictions.eq("entity", entity))
      criteria.setProjection(Projections.distinct(Projections.property("className")))
      val mapping = criteria.list() as MutableList<String>
      session.close()
      return mapping
   }

   @Suppress("UNCHECKED_CAST")
   override fun search(page: Int, pageSize: Int,
                       entity: String?, className: String?, like: Boolean,
                       status: MappingStatus?, approved: Boolean?, after: Long?):
           PagedData<FkgEntityClasses> {
      val session = this.sessionFactory.openSession()
      val c = SqlJpaTools.conditionalCriteria(
              status != null, Restrictions.eq("status", status),
              approved != null, Restrictions.eq("approved", approved),
              after != null, Restrictions.gt("updateEpoch", after),
              entity != null && like, Restrictions.like("entity", "%$entity%"),
              entity != null && !like, Restrictions.eq("entity", entity),
              className != null && like, Restrictions.like("className", "%$className%"),
              className != null && !like, Restrictions.eq("className", className)
      )
      val list = SqlJpaTools.page(FkgEntityClasses::class.java, page, pageSize, session, null, *c)
      session.close()
      return list
   }
}