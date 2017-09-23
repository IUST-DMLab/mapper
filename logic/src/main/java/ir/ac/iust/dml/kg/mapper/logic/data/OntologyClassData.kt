/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.data

/**
 * rdf:type -> owl:Class
 * rdfs:label
 * rdfs:subClassOf
 * owl:equivalentClass
 * owl:disjointWith
 * rdfs:comment
 */
data class OntologyClassData(
    var url: String? = null,
    var name: String? = null,
    var faLabel: String? = null,
    var enLabel: String? = null,
    var faVariantLabels: MutableList<String> = mutableListOf(),
    var enVariantLabels: MutableList<String> = mutableListOf(),
    var faComment: String? = null,
    var enComment: String? = null,
    var subClassOf: String? = null,
    var wasDerivedFrom: String? = null,
    var equivalentClasses: MutableList<String> = mutableListOf(),
    var disjointWith: MutableList<String> = mutableListOf(),
    var properties: MutableList<OntologyPropertyData> = mutableListOf(),
    var next: String? = null,
    var previous: String? = null
)