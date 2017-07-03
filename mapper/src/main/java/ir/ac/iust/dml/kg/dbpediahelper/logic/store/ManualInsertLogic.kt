package ir.ac.iust.dml.kg.dbpediahelper.logic.store

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.dao.knowldegestore.KnowledgeStoreFkgTripleDaoImpl
import ir.ac.iust.dml.kg.dbpediahelper.logic.EntityToClassLogic
import ir.ac.iust.dml.kg.dbpediahelper.logic.StoreProvider
import ir.ac.iust.dml.kg.dbpediahelper.logic.TripleImporter
import ir.ac.iust.dml.kg.raw.utils.PrefixService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class ManualInsertLogic {
  private val logger = Logger.getLogger(this.javaClass)!!
  @Autowired lateinit var provider: StoreProvider
  @Autowired private lateinit var classLogic: EntityToClassLogic
  private lateinit var virtuosoDao: FkgTripleDao
  private lateinit var kgDao: FkgTripleDao
  private val rdfType = PrefixService.prefixToUri(PrefixService.TYPE_URL)!!
  private val rdfsLabel = PrefixService.prefixToUri(PrefixService.LABEL_URL)!!
  private val fkgVariantLabel = PrefixService.prefixToUri(PrefixService.VARIANT_LABEL_URL)!!
  private val owlObjectProperty = PrefixService.prefixToUri(PrefixService.TYPE_OF_ALL_PROPERTIES)!!
  private val owlNamedIndividual = PrefixService.prefixToUri(PrefixService.TYPE_OF_ALL_RESOURCES)!!

  @PostConstruct
  fun setDao() {
    virtuosoDao = provider.getStore(TripleImporter.StoreType.virtuoso)
    kgDao = provider.getStore(TripleImporter.StoreType.knowledgeStore)
    classLogic.reloadTreeCache()
  }

  fun saveResource(resourceUrl: String, ontologyClass: String?, label: String?, variantLabel: String?, permanent: Boolean): Boolean {
    val url = if (!resourceUrl.startsWith("http://")) PrefixService.getFkgResourceUrl(resourceUrl) else resourceUrl
    saveTriple(url, rdfType, owlNamedIndividual, permanent)
    if (label != null) saveTriple(url, rdfsLabel, label, permanent)
    if (variantLabel != null) saveTriple(url, fkgVariantLabel, variantLabel, permanent)
    if (ontologyClass != null) {
      val tree = classLogic.getTree(ontologyClass)
      tree?.split("/")?.forEach { saveTriple(url, rdfType, PrefixService.convertFkgOntologyUrl(it), permanent) }
    }
    return true
  }

  fun savePredicate(predicateUrl: String, label: String?, variantLabel: String?, permanent: Boolean): Boolean {
    val url = if (!predicateUrl.startsWith("http://")) PrefixService.getFkgOntologyPropertyUrl(predicateUrl) else predicateUrl
    saveTriple(url, rdfType, owlObjectProperty, permanent)
    if (label != null) saveTriple(url, rdfsLabel, label, permanent)
    if (variantLabel != null) saveTriple(url, fkgVariantLabel, variantLabel, permanent)
    return true
  }

  fun saveTriple(subjectUrl: String, predicateUrl: String, objectUrl: String, permanent: Boolean): Boolean {
    val sourceUrl = PrefixService.getFkgManualUrl(subjectUrl.substringAfterLast('/'))
    logger.info("save permanent: $permanent")
    virtuosoDao.save(sourceUrl, subjectUrl, objectUrl, predicateUrl)
    kgDao.save(sourceUrl, subjectUrl, objectUrl, predicateUrl)
    (kgDao as KnowledgeStoreFkgTripleDaoImpl).flush()
    return true
  }
}