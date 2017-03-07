package ir.ac.iust.dml.kg.dbpediahelper.access.dao.hibernate

import ir.ac.iust.dml.kg.dbpediahelper.access.dao.TripleStatisticsDao
import ir.ac.iust.dml.kg.dbpediahelper.access.entities.TripleStatistics
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
open class TripleStatisticsDaoImpl : TripleStatisticsDao {

   @Autowired
   lateinit var sessionFactory: SessionFactory

   override fun save(t: TripleStatistics) {
      val session = this.sessionFactory.openSession()
      val tx = session.beginTransaction()
      session.saveOrUpdate(t)
      tx.commit()
      session.close()
   }

   override fun deleteAll() {
      val session = this.sessionFactory.openSession()
      val q = session.createQuery("delete from TripleStatistics")
      q.executeUpdate()
      session.close()
   }
}