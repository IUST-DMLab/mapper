/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.data

import com.google.gson.annotations.SerializedName

data class ExportedPropertyData(
    @SerializedName("en_label")
    var label: String? = null,
    var comment: String? = null,
    var domain: String? = null,
    var parent: MutableList<String> = mutableListOf(),
    var range: String? = null,
    var wasDerivedFrom: String? = null,
    var equivalentProperty: String? = null,
    var type: String? = null
)