/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.data

data class OntologyPropertyData(
    var url: String? = null,
    var name: String? = null,
    var wasDerivedFrom: String? = null,
    var faLabel: String? = null,
    var enLabel: String? = null,
    var faVariantLabels: MutableList<String> = mutableListOf(),
    var enVariantLabels: MutableList<String> = mutableListOf(),
    var types: MutableList<String> = mutableListOf(),
    var domains: MutableList<String> = mutableListOf(),
    var autoDomains: MutableList<String> = mutableListOf(),
    var ranges: MutableList<String> = mutableListOf(),
    var autoRanges: MutableList<String> = mutableListOf(),
    var equivalentProperties: MutableList<String> = mutableListOf()
)