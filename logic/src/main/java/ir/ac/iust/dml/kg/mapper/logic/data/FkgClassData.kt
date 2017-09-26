/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.data

@Deprecated("it is using for deprecated services")
data class FkgClassData(
    var ontologyClass: String? = null,
    var parentOntologyClass: String? = null,
    var enLabel: String? = null,
    var comment: String? = null,
    var faLabel: String? = null,
    var faOtherLabels: String? = null,
    var note: String? = null,
    var approved: Boolean? = null)
