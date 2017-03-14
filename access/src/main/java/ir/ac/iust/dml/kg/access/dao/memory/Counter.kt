package ir.ac.iust.dml.kg.access.dao.memory

class Counter<out T>(val target: T) {
   var count: Long = 0
   override fun toString() = "$count\t$target"
}