package ir.ac.iust.dml.kg.ontologytranslator.access.dao

import ir.ac.iust.dml.kg.ontologytranslator.access.entities.DBpediaClass
import ir.ac.iust.dml.kg.utils.PagedData
import ir.ac.iust.dml.kg.utils.hibernate.SqlJpaTools
import org.hibernate.SessionFactory
import org.hibernate.criterion.Restrictions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
open class DBpediaOntologyClassDaoImpl : DBpediaOntologyClassDao {

   @Autowired
   lateinit var sessionFactory: SessionFactory

   override fun save(p: DBpediaClass) {
      val session = this.sessionFactory.openSession()
      val tx = session.beginTransaction()
      session.saveOrUpdate(p)
      tx.commit()
      session.close()
   }

   override fun read(id: Long): DBpediaClass? {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(DBpediaClass::class.java)
      criteria.add(Restrictions.eq("id", id))
      val mapping = criteria.uniqueResult() as DBpediaClass
      session.close()
      return mapping
   }

   override fun read(name: String, parentId: Long?): DBpediaClass? {
      val session = this.sessionFactory.openSession()
      val criteria = session.createCriteria(DBpediaClass::class.java)
      criteria.add(Restrictions.eq("name", name))
      if (parentId != null) criteria.add(Restrictions.eq("parentId", parentId))
      val mapping = criteria.uniqueResult() as DBpediaClass
      session.close()
      return mapping
   }

   override fun search(name: String?, parentId: Long?, like: Boolean, pageSize: Int, page: Int): PagedData<DBpediaClass> {
      val session = this.sessionFactory.openSession()
      val criteria = SqlJpaTools.conditionalCriteria(
            name != null && !like, Restrictions.eq("name", name),
            name != null && like, Restrictions.like("name", "%$name%"),
            parentId != null, Restrictions.eq("parentId", parentId)
      )
      val list = SqlJpaTools.page(DBpediaClass::class.java, page, pageSize, session, *criteria)
      session.close()
      return list
   }

}