/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.data

data class PropertyMapping(
    var property: String? = null,
    var weight: Double? = null,
    var rules: MutableList<MapRule> = mutableListOf(),
    var recommendations: MutableList<MapRule> = mutableListOf()
)