/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.data

import ir.ac.iust.dml.kg.knowledge.core.ValueType

data class MapRule(
    var predicate: String? = null,
    var constant: String? = null,
    var type: ValueType? = null,
    var unit: String? = null,
    var transform: String? = null
)