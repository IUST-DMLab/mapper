/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.data

data class TemplateMapping(
    var template: String? = null,
    var properties: MutableMap<String, PropertyMapping>? = mutableMapOf(),
    var rules: MutableList<MapRule>? = mutableListOf(),
    var ontologyClass: String = "Thing",
    var tree: List<String> = mutableListOf("Thing"),
    var weight: Double? = null
)