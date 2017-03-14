package ir.ac.iust.dml.kg.access.dao.hibernate

import ir.ac.iust.dml.kg.access.dao.FkgTripleStatisticsDao
import ir.ac.iust.dml.kg.access.entities.FkgTripleStatistics
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
open class FkgTripleStatisticsDaoImpl : FkgTripleStatisticsDao {

   @Autowired
   lateinit var sessionFactory: SessionFactory

   override fun save(t: FkgTripleStatistics) {
      val session = this.sessionFactory.openSession()
      val tx = session.beginTransaction()
      session.saveOrUpdate(t)
      tx.commit()
      session.close()
   }

   override fun deleteAll() {
      val session = this.sessionFactory.openSession()
      val q = session.createQuery("delete from FkgTripleStatistics")
      q.executeUpdate()
      session.close()
   }
}