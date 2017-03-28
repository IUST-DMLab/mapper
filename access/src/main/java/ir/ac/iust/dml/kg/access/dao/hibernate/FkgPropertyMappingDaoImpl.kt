package ir.ac.iust.dml.kg.access.dao.hibernate

import ir.ac.iust.dml.kg.access.dao.FkgPropertyMappingDao
import ir.ac.iust.dml.kg.access.entities.FkgPropertyMapping
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.utils.PagedData
import ir.ac.iust.dml.kg.utils.hibernate.SqlJpaTools
import org.hibernate.Criteria
import org.hibernate.SessionFactory
import org.hibernate.criterion.Projections
import org.hibernate.criterion.Restrictions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
open class FkgPropertyMappingDaoImpl : FkgPropertyMappingDao {

  @Suppress("UNCHECKED_CAST")
  override fun search(page: Int, pageSize: Int, language: String?, clazz: String?, type: String?,
                      like: Boolean, hasClass: Boolean, templateProperty: String?,
                      secondTemplateProperty: String?, ontologyProperty: String?, status: MappingStatus?,
                      approved: Boolean?, after: Long?, noUpdateEpoch: Boolean?):
          PagedData<FkgPropertyMapping> {
    val session = this.sessionFactory.openSession()
    val twoTemplateProperties = templateProperty != null
            && secondTemplateProperty != null && templateProperty != secondTemplateProperty
    val c = SqlJpaTools.conditionalCriteria(
            ontologyProperty != null && like, Restrictions.like("ontologyProperty", "%$ontologyProperty%"),
            ontologyProperty != null && !like, Restrictions.eq("ontologyProperty", ontologyProperty),

            templateProperty != null && like, Restrictions.like("templateProperty", "%$templateProperty%"),

            templateProperty != null && !like && !twoTemplateProperties,
            Restrictions.eq("templateProperty", templateProperty),

            templateProperty != null && !like && !twoTemplateProperties,
            Restrictions.or(
                    Restrictions.eq("templateProperty", templateProperty),
                    Restrictions.eq("templateProperty", secondTemplateProperty)),

            type != null, Restrictions.eq("templateName", type),
            clazz != null, Restrictions.eq("ontologyClass", clazz),
            hasClass, Restrictions.isNotNull("ontologyClass"),
            language != null, Restrictions.eq("language", language),
            status != null, Restrictions.eq("status", status),
            approved != null, Restrictions.eq("approved", approved),
            after != null, Restrictions.gt("updateEpoch", after),
            noUpdateEpoch != null && noUpdateEpoch, Restrictions.isNull("updateEpoch"),
            noUpdateEpoch != null && !noUpdateEpoch, Restrictions.isNotNull("updateEpoch")
    )
    val list = SqlJpaTools.page(FkgPropertyMapping::class.java, page, pageSize, session, null, *c)
    session.close()
    return list
  }


  override fun read(id: Long): FkgPropertyMapping? {
    val session = this.sessionFactory.openSession()
    val criteria = session.createCriteria(FkgPropertyMapping::class.java)
    criteria.add(Restrictions.eq("id", id))
    val mapping = criteria.uniqueResult() as FkgPropertyMapping?
    session.close()
    return mapping
  }

  @Suppress("UNCHECKED_CAST")
  override fun searchTemplateName(page: Int, pageSize: Int, keyword: String?): List<String> {
    val session = this.sessionFactory.openSession()
    val criteria = session.createCriteria(FkgPropertyMapping::class.java)
    criteria.add(Restrictions.like("templateName", "%$keyword%"))
    criteria.setProjection(Projections.distinct(Projections.property("templateName")))
    val mapping = criteria.list() as MutableList<String>
    session.close()
    return mapping
  }

  @Suppress("UNCHECKED_CAST")
  override fun searchOntologyClass(page: Int, pageSize: Int, keyword: String?): List<String> {
    val session = this.sessionFactory.openSession()
    val criteria = session.createCriteria(FkgPropertyMapping::class.java)
    criteria.add(Restrictions.like("ontologyClass", "%$keyword%"))
    criteria.setProjection(Projections.distinct(Projections.property("ontologyClass")))
    val mapping = criteria.list() as MutableList<String>
    session.close()
    return mapping
  }

  @Suppress("UNCHECKED_CAST")
  override fun searchTemplatePropertyName(page: Int, pageSize: Int, keyword: String?): List<String> {
    val session = this.sessionFactory.openSession()
    val criteria = session.createCriteria(FkgPropertyMapping::class.java)
    criteria.add(Restrictions.like("templateProperty", "%$keyword%"))
    criteria.setProjection(Projections.distinct(Projections.property("templateProperty")))
    val mapping = criteria.list() as MutableList<String>
    session.close()
    return mapping
  }

  @Suppress("UNCHECKED_CAST")
  override fun searchOntologyPropertyName(page: Int, pageSize: Int, keyword: String?): List<String> {
    val session = this.sessionFactory.openSession()
    val criteria = session.createCriteria(FkgPropertyMapping::class.java)
    criteria.add(Restrictions.like("ontologyProperty", "%$keyword%"))
    criteria.setProjection(Projections.distinct(Projections.property("ontologyProperty")))
    val mapping = criteria.list() as MutableList<String>
    session.close()
    return mapping
  }

  @Autowired
  lateinit var sessionFactory: SessionFactory

  override fun save(p: FkgPropertyMapping) {
    val session = this.sessionFactory.openSession()
    val tx = session.beginTransaction()
    session.saveOrUpdate(p)
    tx.commit()
    session.close()
  }

  override fun deleteAll() {
    val session = this.sessionFactory.openSession()
    val q = session.createQuery("delete from FkgPropertyMapping")
    q.executeUpdate()
    session.close()
  }

  @Suppress("UNCHECKED_CAST")
  override fun list(pageSize: Int, page: Int, hasClass: Boolean): PagedData<FkgPropertyMapping> {
    val session = this.sessionFactory.openSession()
    val criteria = SqlJpaTools.conditionalCriteria(hasClass, Restrictions.isNotNull("ontologyClass"))
    val list = SqlJpaTools.page(FkgPropertyMapping::class.java, page, pageSize, session, null, *criteria)
    session.close()
    return list
  }

  @Suppress("UNCHECKED_CAST")
  override fun readOntologyProperty(templateProperty: String): List<String> {
    val session = this.sessionFactory.openSession()
    val criteria = session.createCriteria(FkgPropertyMapping::class.java)
    criteria.add(Restrictions.eq("templateProperty", templateProperty))
    criteria.setProjection(Projections.distinct(Projections.property("ontologyProperty")))
    val mapping = criteria.list() as MutableList<String>
    session.close()
    return mapping
  }

  @Suppress("UNCHECKED_CAST")
  override fun listUniqueProperties(language: String?, pageSize: Int, page: Int): List<String> {
    val session = this.sessionFactory.openSession()
    val criteria = session.createCriteria(FkgPropertyMapping::class.java)
    if (language != null) criteria.add(Restrictions.like("language", language))
    criteria.setProjection(Projections.distinct(Projections.property("templateProperty")))
    criteria.setFirstResult(page * pageSize)
    criteria.setMaxResults(pageSize)
    val mapping = criteria.list() as MutableList<String>
    session.close()
    return mapping
  }

  override fun countTemplateProperties(templateProperty: String): Long {
    val session = this.sessionFactory.openSession()
    val criteria = session.createCriteria(FkgPropertyMapping::class.java)
    criteria.add(Restrictions.eq("templateProperty", templateProperty))
    val count = criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
            .setProjection(Projections.rowCount()).uniqueResult() as Long
    session.close()
    return count
  }

  @Suppress("UNCHECKED_CAST")
  override fun listUniqueOntologyProperties(templateProperty: String): List<String> {
    val session = this.sessionFactory.openSession()
    val criteria = session.createCriteria(FkgPropertyMapping::class.java)
    criteria.add(Restrictions.eq("templateProperty", templateProperty))
    criteria.setProjection(Projections.distinct(Projections.property("ontologyProperty")))
    val mapping = criteria.list() as MutableList<String>
    session.close()
    return mapping
  }

  override fun countOntologyProperties(templateProperty: String, ontologyProperty: String): Long {
    val session = this.sessionFactory.openSession()
    val criteria = session.createCriteria(FkgPropertyMapping::class.java)
    criteria.add(Restrictions.eq("templateProperty", templateProperty))
    criteria.add(Restrictions.eq("ontologyProperty", ontologyProperty))
    val count = criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
            .setProjection(Projections.rowCount()).uniqueResult() as Long
    session.close()
    return count
  }
}