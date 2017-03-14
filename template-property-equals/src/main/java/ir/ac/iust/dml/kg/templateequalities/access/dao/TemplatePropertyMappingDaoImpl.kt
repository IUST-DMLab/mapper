package ir.ac.iust.dml.kg.templateequalities.access.dao

import ir.ac.iust.dml.kg.templateequalities.access.entities.WikipediaPropertyTranslation
import ir.ac.iust.dml.kg.utils.PagedData
import ir.ac.iust.dml.kg.utils.hibernate.SqlJpaTools
import org.hibernate.SessionFactory
import org.hibernate.criterion.Restrictions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
open class TemplatePropertyMappingDaoImpl : TemplatePropertyMappingDao {

    @Suppress("UNCHECKED_CAST")
    override fun readByFaTitle(type: String?, faProperty: String): MutableList<WikipediaPropertyTranslation> {
        val session = this.sessionFactory.openSession()
        val criteria = session.createCriteria(WikipediaPropertyTranslation::class.java)
        if (type != null) criteria.add(Restrictions.like("type", "%$type%"))
        criteria.add(Restrictions.like("faProperty", "%$faProperty%"))
        val mapping = criteria.list() as MutableList<WikipediaPropertyTranslation>
        session.close()
        return mapping
    }

    @Suppress("UNCHECKED_CAST")
    override fun readByEnTitle(type: String?, enProperty: String): MutableList<WikipediaPropertyTranslation> {
        val session = this.sessionFactory.openSession()
        val criteria = session.createCriteria(WikipediaPropertyTranslation::class.java)
        if (type != null) criteria.add(Restrictions.like("type", "%$type%"))
        criteria.add(Restrictions.like("enProperty", "%$enProperty%"))
        val mapping = criteria.list() as MutableList<WikipediaPropertyTranslation>
        session.close()
        return mapping
    }

    @Autowired
    lateinit var sessionFactory: SessionFactory

    override fun save(p: WikipediaPropertyTranslation) {
        val session = this.sessionFactory.openSession()
        val tx = session.beginTransaction()
        session.saveOrUpdate(p)
        tx.commit()
        session.close()
    }

    @Suppress("UNCHECKED_CAST")
    override fun list(pageSize: Int, page: Int): PagedData<WikipediaPropertyTranslation> {
        val session = this.sessionFactory.openSession()
        val list = SqlJpaTools.page(WikipediaPropertyTranslation::class.java, page, pageSize, session)
        session.close()
        return list
    }
}