package ir.ac.iust.dml.kg.dbpediahelper.access.dao.memory

data class TypedString(var type: String, var string: String) {
   override fun toString() = type + " >> " + string
}