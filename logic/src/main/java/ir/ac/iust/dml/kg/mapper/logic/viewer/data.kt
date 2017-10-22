/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.viewer

import java.util.*


data class EntityPropertyValue(val value: String, var url: String? = null, var image: Boolean = false) : Comparable<EntityPropertyValue> {
  override fun compareTo(other: EntityPropertyValue) = this.value.compareTo(other.value)
  override fun hashCode() = value.hashCode()
  override fun equals(other: Any?) = value == (other as EntityPropertyValue).value
}

data class EntityProperty(var value: EntityPropertyValue,
                          var moreInfo: MutableMap<String, EntityProperty> = mutableMapOf()) : Comparable<EntityProperty> {
  override fun compareTo(other: EntityProperty) = this.value.compareTo(other.value)
  override fun hashCode() = value.hashCode()
  override fun equals(other: Any?) = value == (other as EntityProperty).value
}

data class EntityData(var label: String? = null, var type: String? = null, var wikiLink: String? = null,
                      var abstract: String? = null, var image: String? = null,
                      var properties: SortedMap<String, SortedSet<EntityProperty>> = sortedMapOf())
