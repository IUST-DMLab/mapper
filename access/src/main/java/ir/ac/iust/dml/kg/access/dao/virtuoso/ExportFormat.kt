package ir.ac.iust.dml.kg.access.dao.virtuoso

import org.eclipse.rdf4j.rio.RDFFormat

enum class ExportFormat {
  RDFXML, NTRIPLES, TURTLE, N3, NQUADS, JSONLD, RDFJSON;

  val rdfFormat: RDFFormat?
    get() {
      return when (this) {
        RDFXML -> RDFFormat.RDFXML
        NTRIPLES -> RDFFormat.NTRIPLES
        TURTLE -> RDFFormat.TURTLE
        N3 -> RDFFormat.N3
        NQUADS -> RDFFormat.NQUADS
        JSONLD -> RDFFormat.JSONLD
        RDFJSON -> RDFFormat.RDFJSON
      }
    }
}