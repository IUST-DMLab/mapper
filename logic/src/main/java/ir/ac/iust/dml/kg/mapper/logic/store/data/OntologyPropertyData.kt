package ir.ac.iust.dml.kg.mapper.logic.store.data

/**
 * rdf:type
 * rdfs:domain
 * rdfs:label
 * rdfs:range
 * owl:equivalentProperty
 */
data class OntologyPropertyData(
    var url: String? = null,
    var faLabel: String? = null,
    var enLabel: String? = null,
    var faVariantLabels: MutableList<String> = mutableListOf(),
    var enVariantLabels: MutableList<String> = mutableListOf(),
    var types: MutableList<String> = mutableListOf(),
    var domains: MutableList<String> = mutableListOf(),
    var ranges: MutableList<String> = mutableListOf(),
    var equivalentProperties: MutableList<String> = mutableListOf()
)