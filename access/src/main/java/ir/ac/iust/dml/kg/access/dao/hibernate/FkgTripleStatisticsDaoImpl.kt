package ir.ac.iust.dml.kg.access.dao.hibernate

import ir.ac.iust.dml.kg.access.dao.FkgTripleStatisticsDao
import ir.ac.iust.dml.kg.access.entities.FkgTripleStatistics
import ir.ac.iust.dml.kg.access.entities.enumerations.TripleStatisticsType
import ir.ac.iust.dml.kg.raw.utils.PagedData
import ir.ac.iust.dml.kg.utils.hibernate.SqlJpaTools
import org.hibernate.SessionFactory
import org.hibernate.criterion.Order
import org.hibernate.criterion.Restrictions
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

  override fun readProperty(property: String): FkgTripleStatistics? {
    val session = this.sessionFactory.openSession()
    val criteria = session.createCriteria(FkgTripleStatistics::class.java)
    criteria.add(Restrictions.eq("property", property))
    criteria.add(Restrictions.eq("countType", TripleStatisticsType.property))
    val triple = criteria.uniqueResult() as FkgTripleStatistics?
    session.close()
    return triple
  }

  override fun readTypedProperty(templateType: String, property: String): FkgTripleStatistics? {
    val session = this.sessionFactory.openSession()
    val criteria = session.createCriteria(FkgTripleStatistics::class.java)
    criteria.add(Restrictions.eq("property", property))
    criteria.add(Restrictions.eq("templateType", templateType))
    criteria.add(Restrictions.eq("countType", TripleStatisticsType.typedProperty))
    val triple = criteria.uniqueResult() as FkgTripleStatistics?
    session.close()
    return triple
  }

  override fun readTypedEntity(templateType: String, entity: String): FkgTripleStatistics? {
    val session = this.sessionFactory.openSession()
    val criteria = session.createCriteria(FkgTripleStatistics::class.java)
    criteria.add(Restrictions.eq("entity", entity))
    criteria.add(Restrictions.eq("templateType", templateType))
    criteria.add(Restrictions.eq("countType", TripleStatisticsType.typedEntity))
    val triple = criteria.uniqueResult() as FkgTripleStatistics?
    session.close()
    return triple
  }

  override fun readType(templateType: String): FkgTripleStatistics? {
    val session = this.sessionFactory.openSession()
    val criteria = session.createCriteria(FkgTripleStatistics::class.java)
    criteria.add(Restrictions.eq("templateType", templateType))
    criteria.add(Restrictions.eq("countType", TripleStatisticsType.type))
    val triple = criteria.uniqueResult() as FkgTripleStatistics?
    session.close()
    return triple
  }

  override fun search(page: Int, pageSize: Int, countType: TripleStatisticsType?): PagedData<FkgTripleStatistics> {
    val session = this.sessionFactory.openSession()
    val criteria = SqlJpaTools.conditionalCriteria(countType != null, Restrictions.eq("countType", countType))
    val list = SqlJpaTools.page(FkgTripleStatistics::class.java, page, pageSize, session, listOf(Order.desc("count")), *criteria)
    session.close()
    return list
  }
}