package ir.ac.iust.dml.kg.access.dao.hibernate

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.entities.FkgPropertyMapping
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.access.utils.SqlJpaTools
import ir.ac.iust.dml.kg.raw.utils.PagedData
import org.hibernate.SessionFactory
import org.hibernate.criterion.Restrictions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
open class FkgTripleDaoImpl : FkgTripleDao {

   @Autowired
   lateinit var sessionFactory: SessionFactory

   override fun save(t: FkgTriple, mapping: FkgPropertyMapping?) {
      val session = this.sessionFactory.openSession()
      val tx = session.beginTransaction()
      session.saveOrUpdate(t)
      tx.commit()
      session.close()
   }

   override fun deleteAll() {
      val session = this.sessionFactory.openSession()
      val q = session.createQuery("delete from FkgTriple")
      q.executeUpdate()
      session.close()
   }

   override fun list(pageSize: Int, page: Int): PagedData<FkgTriple> {
      val session = this.sessionFactory.openSession()
//      val criteria = SqlJpaTools.conditionalCriteria(hasClass, Restrictions.isNotNull("ontologyClass"))
      val list = SqlJpaTools.page(FkgTriple::class.java, page, pageSize, session, null)
      session.close()
      return list
   }

   @Suppress("UNCHECKED_CAST")
   override fun read(subject: String?, predicate: String?, objekt: String?,
                     status: MappingStatus?): MutableList<FkgTriple> {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(FkgTriple::class.java)
      if (subject != null) criteria.add(Restrictions.eq("subject", subject))
      if (predicate != null) criteria.add(Restrictions.eq("predicate", predicate))
      if (objekt != null) criteria.add(Restrictions.eq("objekt", objekt))
      if (status != null) criteria.add(Restrictions.eq("status", status))
      val triple = criteria.list() as MutableList<FkgTriple>
      session.close()
      return triple
   }
}