package ir.ac.iust.dml.kg.dbpediahelper.logic.triple

import com.google.gson.annotations.SerializedName

data class TripleData(
      var source: String? = null,
      var subject: String? = null,
      var predicate: String? = null,
      @SerializedName("template_name")
      var templateName: String? = null,
      @SerializedName("type")
      var templateType: String? = null,
      @SerializedName("object")
      var objekt: String? = null
)