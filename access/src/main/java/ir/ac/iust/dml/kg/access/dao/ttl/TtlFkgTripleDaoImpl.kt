/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.access.dao.ttl

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.dao.TripleFixer
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.knowledge.core.ValueType
import ir.ac.iust.dml.kg.raw.utils.PagedData
import ir.ac.iust.dml.kg.raw.utils.URIs
import org.apache.commons.io.FileUtils
import org.apache.log4j.Logger
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.model.util.ModelBuilder
import org.eclipse.rdf4j.model.vocabulary.XMLSchema
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class TtlFkgTripleDaoImpl(private val path: Path, private val flushSize: Int = 50000) : FkgTripleDao() {
  private val logger = Logger.getLogger(this.javaClass)!!

  override fun newVersion(module: String) = 1

  override fun activateVersion(module: String, version: Int) = true

  init {
    if (!Files.exists(path)) Files.createDirectories(path)
  }

  private var notFlushedTriples = mutableListOf<FkgTriple>()
  private var subRelations = mutableMapOf<String, Int>()
  private var numberOfFiles = 0

  override fun save(t: FkgTriple) {
    synchronized(notFlushedTriples) {
      if (validate && !TripleFixer.fix(t)) return
      notFlushedTriples.add(t)
      if (notFlushedTriples.size > flushSize) {
        flush()
      }
    }
  }

  override fun flush() {
    val builder = ModelBuilder()
    val out: FileOutputStream
    try {
      out = FileOutputStream(path.resolve("$numberOfFiles.ttl").toFile())
      val writer = Rio.createWriter(RDFFormat.TURTLE, out)
      writer.startRDF()
      writer.handleNamespace(URIs.fkgResourcePrefix,
          URIs.prefixedToUri(URIs.fkgResourcePrefix + ":"))
      writer.handleNamespace(URIs.fkgNotMappedPropertyPrefix,
          URIs.prefixedToUri(URIs.fkgNotMappedPropertyPrefix + ":"))
      writer.handleNamespace(URIs.fkgCategoryPrefix,
          URIs.prefixedToUri(URIs.fkgCategoryPrefix + ":"))
      writer.handleNamespace(URIs.fkgDataTypePrefix,
          URIs.prefixedToUri(URIs.fkgDataTypePrefix + ":"))
      writer.handleNamespace(URIs.fkgOntologyPrefix,
          URIs.prefixedToUri(URIs.fkgOntologyPrefix + ":"))
      writer.handleNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
      writer.handleNamespace("skos", "http://www.w3.org/2004/02/skos/core#")
      writer.handleNamespace("foaf", "http://xmlns.com/foaf/0.1/")
      writer.handleNamespace("owl", "http://www.w3.org/2002/07/owl#")
      writer.handleNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
      writer.handleNamespace("dct", "http://dublincore.org/2012/06/14/dcterms#")
      writer.handleNamespace("dbpm", "http://mappings.dbpedia.org/index.php/")
      notFlushedTriples.forEach { triple ->
        if (triple.properties.isEmpty())
          builder.namedGraph(URIs.defaultContext).add(triple.subject, triple.predicate,
              createValue(triple.objekt!!, triple.language, triple.valueType!!))
        else {
          val number = subRelations.getOrDefault(triple.subject!!, 0)
          val relation = triple.subject + "_" + "/relation_" + number
          subRelations[triple.subject!!] = number + 1
          val relationValue = SimpleValueFactory.getInstance().createIRI(relation)
          val defaultPredicateValue = SimpleValueFactory.getInstance().createIRI(triple.predicate)
          builder.namedGraph(URIs.defaultContext)
              .add(triple.subject, URIs.relatedPredicates, relationValue)
              .add(relation, URIs.type, SimpleValueFactory.getInstance().createIRI(URIs.relatedPredicatesClass))
              .add(relation, URIs.mainPredicate, defaultPredicateValue)
              .add(relation, triple.predicate, createValue(triple.objekt!!, triple.language, triple.valueType!!))
          for (prop in triple.properties) {
            builder.namedGraph(URIs.defaultContext).add(relation,
                prop.predicate,
                createValue(prop.objekt!!, prop.language, prop.valueType!!))
          }
        }
      }
      val model = builder.build()
      model.forEach { writer.handleStatement(it) }
      writer.endRDF()
    } catch (e: IOException) {
      e.printStackTrace()
    }
    numberOfFiles++
    notFlushedTriples.clear()
  }

  private fun createValue(value: String, language: String?, type: ValueType): Value {
    val vf = SimpleValueFactory.getInstance()
    return when (type) {
      ValueType.Resource -> vf.createIRI(value)
      ValueType.String -> vf.createLiteral(value, language!!)
      ValueType.Boolean -> vf.createLiteral(value, XMLSchema.BOOLEAN)
      ValueType.Byte -> vf.createLiteral(value, XMLSchema.BYTE)
      ValueType.Short -> vf.createLiteral(value, XMLSchema.SHORT)
      ValueType.Integer -> vf.createLiteral(value, XMLSchema.INTEGER)
      ValueType.Long -> vf.createLiteral(value, XMLSchema.LONG)
      ValueType.Double -> vf.createLiteral(value, XMLSchema.DOUBLE)
      ValueType.Float -> vf.createLiteral(value, XMLSchema.FLOAT)
      ValueType.Date -> vf.createLiteral(Date(value.toLong()))
    }
  }

  override fun deleteAll() {
    FileUtils.deleteDirectory(path.toFile())
    Files.createDirectories(path)
  }

  override fun delete(subject: String, predicate: String, `object`: String) {
    TODO("not implemented")
  }

  override fun list(pageSize: Int, page: Int): PagedData<FkgTriple> {
    throw UnsupportedOperationException("not implemented")
  }

  override fun read(subject: String?, predicate: String?, objekt: String?): MutableList<FkgTriple> {
    throw UnsupportedOperationException("not implemented")
  }

}