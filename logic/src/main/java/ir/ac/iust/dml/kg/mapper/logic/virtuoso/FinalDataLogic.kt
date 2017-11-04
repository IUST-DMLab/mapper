package ir.ac.iust.dml.kg.mapper.logic.virtuoso

import ir.ac.iust.dml.kg.access.dao.virtuoso.ExportFormat
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.URIs
import ir.ac.iust.dml.kg.virtuoso.connector.VirtuosoConnector
import ir.ac.iust.dml.kg.virtuoso.connector.data.VirtuosoTriple
import ir.ac.iust.dml.kg.virtuoso.connector.data.VirtuosoTripleObject
import ir.ac.iust.dml.kg.virtuoso.connector.data.VirtuosoTripleType
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.model.util.ModelBuilder
import org.eclipse.rdf4j.model.vocabulary.XMLSchema
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import org.springframework.stereotype.Service
import java.io.IOException
import java.io.OutputStream

@Service
class FinalDataLogic {
  private var connector: VirtuosoConnector? = null
  private var graphName = ConfigReader.getString("virtuoso.graph", URIs.defaultContext)

  fun getTriplesOfSubject(subjectUrl: String): MutableList<VirtuosoTriple>? {
    if (connector == null) connector = VirtuosoConnector(graphName)
    return connector!!.getTriplesOfSubject(subjectUrl)
  }

  fun export(subjectUrl: String, format: ExportFormat, out: OutputStream) {
    val builder = ModelBuilder()
    val triples = getTriplesOfSubject(subjectUrl)
    triples?.forEach {
      builder.namedGraph(graphName).add(it.source, it.predicate, createValue(it.`object`))
    }
    return writeModelBuilder(out, format.rdfFormat!!, builder)
  }

  private fun createValue(v: VirtuosoTripleObject): Any {
    val vf = SimpleValueFactory.getInstance()
    if (v.type != null)
      when (v.type) {
        VirtuosoTripleType.Resource -> return vf.createIRI(v.value.toString())
        VirtuosoTripleType.String -> return vf.createLiteral(v.value.toString(), v.language)
        VirtuosoTripleType.Boolean -> return vf.createLiteral(v.value.toString(), XMLSchema.BOOLEAN)
        VirtuosoTripleType.Byte -> return vf.createLiteral(v.value.toString(), XMLSchema.BYTE)
        VirtuosoTripleType.Short -> return vf.createLiteral(v.value.toString(), XMLSchema.SHORT)
        VirtuosoTripleType.Int -> return vf.createLiteral(v.value.toString(), XMLSchema.INTEGER)
        VirtuosoTripleType.Long -> return vf.createLiteral(v.value.toString(), XMLSchema.LONG)
        VirtuosoTripleType.Double -> return vf.createLiteral(v.value.toString(), XMLSchema.DOUBLE)
        VirtuosoTripleType.Float -> return vf.createLiteral(v.value.toString(), XMLSchema.FLOAT)
        else -> {
        }
      }
    return vf.createLiteral(v.value.toString())
  }

  private fun writeModelBuilder(out: OutputStream, format: RDFFormat, builder: ModelBuilder) {
    val model = builder.build()
    try {
      val writer = Rio.createWriter(format, out)
      writer.startRDF()
      writer.handleNamespace(URIs.fkgResourcePrefix, URIs.prefixedToUri(URIs.fkgResourcePrefix + ":"))
      writer.handleNamespace(URIs.fkgNotMappedPropertyPrefix,
              URIs.prefixedToUri(URIs.fkgNotMappedPropertyPrefix + ":"))
      writer.handleNamespace(URIs.fkgCategoryPrefix, URIs.prefixedToUri(URIs.fkgCategoryPrefix + ":"))
      writer.handleNamespace(URIs.fkgDataTypePrefix, URIs.prefixedToUri(URIs.fkgDataTypePrefix + ":"))
      writer.handleNamespace(URIs.fkgOntologyPrefix, URIs.prefixedToUri(URIs.fkgOntologyPrefix + ":"))
      writer.handleNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
      writer.handleNamespace("skos", "http://www.w3.org/2004/02/skos/core#")
      writer.handleNamespace("foaf", "http://xmlns.com/foaf/0.1/")
      writer.handleNamespace("owl", "http://www.w3.org/2002/07/owl#")
      writer.handleNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
      writer.handleNamespace("dct", "http://dublincore.org/2012/06/14/dcterms#")
      writer.handleNamespace("dbpm", "http://mappings.dbpedia.org/index.php/")
      model.forEach { writer.handleStatement(it) }
      writer.endRDF()
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }
}