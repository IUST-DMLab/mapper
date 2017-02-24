package ir.ac.iust.dml.kg.dbpediahelper.logic.triple

import com.google.gson.annotations.SerializedName

data class TripleData(
      var source: String? = null,
      var subject: String? = null,
      var predicate: String? = null,
      var infoboxType: String? = null,
      @SerializedName("object")
      var objekt: String? = null
)