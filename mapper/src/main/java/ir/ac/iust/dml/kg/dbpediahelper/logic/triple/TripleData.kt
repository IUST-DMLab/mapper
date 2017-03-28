package ir.ac.iust.dml.kg.dbpediahelper.logic.triple

import com.google.gson.annotations.SerializedName

data class TripleData(
        var source: String? = null,
        var subject: String? = null,
        var predicate: String? = null,
        @SerializedName("template_name")
        var templateNameFull: String? = null,
        var templateName: String? = null,
        @SerializedName("template_type")
        var templateType: String? = null,
        @SerializedName("object")
        var objekt: String? = null
)