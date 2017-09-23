/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.data

data class PropertyStats(
    var property: String,
    var templates: MutableSet<String> = mutableSetOf(),
    var classes: MutableSet<String> = mutableSetOf(),
    var nullInTemplates: MutableSet<String> = mutableSetOf(),
    var approvedInTemplates: MutableSet<String> = mutableSetOf(),
    var predicates: MutableSet<String> = mutableSetOf()
)