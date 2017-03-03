package ir.ac.iust.dml.kg.dbpediahelper.access.dao.mongo

import ir.ac.iust.dml.kg.dbpediahelper.access.dao.KnowledgeBaseTripleDao
import ir.ac.iust.dml.kg.dbpediahelper.access.entities.KnowledgeBaseTriple
import ir.ac.iust.dml.kg.dbpediahelper.access.entities.MappingStatus
import ir.ac.iust.dml.kg.utils.PagedData
import ir.ac.iust.dml.kg.utils.hibernate.SqlJpaTools
import org.hibernate.SessionFactory
import org.hibernate.criterion.Restrictions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
open class KnowledgeBaseTripleDaoImpl : KnowledgeBaseTripleDao {

   @Autowired
   lateinit var sessionFactory: SessionFactory

   override fun save(t: KnowledgeBaseTriple) {
      val session = this.sessionFactory.openSession()
      val tx = session.beginTransaction()
      session.saveOrUpdate(t)
      tx.commit()
      session.close()
   }

   override fun deleteAll() {
      val session = this.sessionFactory.openSession()
      val q = session.createQuery("delete from KnowledgeBaseTriple")
      q.executeUpdate()
      session.close()
   }

   override fun list(pageSize: Int, page: Int): PagedData<KnowledgeBaseTriple> {
      val session = this.sessionFactory.openSession()
//      val criteria = SqlJpaTools.condtionalCriteria(hasClass, Restrictions.isNotNull("clazz"))
      val list = SqlJpaTools.page(KnowledgeBaseTriple::class.java, page, pageSize, session)
      session.close()
      return list
   }

   @Suppress("UNCHECKED_CAST")
   override fun read(subject: String?, predicate: String?, objekt: String?,
                     status: MappingStatus?): MutableList<KnowledgeBaseTriple> {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(KnowledgeBaseTriple::class.java)
      if (subject != null) criteria.add(Restrictions.eq("subject", subject))
      if (predicate != null) criteria.add(Restrictions.eq("predicate", predicate))
      if (objekt != null) criteria.add(Restrictions.eq("objekt", objekt))
      if (status != null) criteria.add(Restrictions.eq("status", status))
      val triple = criteria.list() as MutableList<KnowledgeBaseTriple>
      session.close()
      return triple
   }
}