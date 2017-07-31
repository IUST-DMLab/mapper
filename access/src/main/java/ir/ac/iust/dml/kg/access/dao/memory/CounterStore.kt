package ir.ac.iust.dml.kg.access.dao.memory

class CounterStore<T> {
  var map: MutableMap<String, Counter<T>> = mutableMapOf()

  fun count(t: T) {
    val counter = map.getOrPut(t.toString(), { Counter(t) })
    counter.count++
  }

  fun getCount(t: T) = map[t.toString()]!!.count

  override fun toString(): String {
    val builder = StringBuilder()
    map.values.forEach { builder.append(it).append('\n') }
//      if(builder.isNotEmpty()) builder.setLength(builder.length - 1)
    return builder.toString()
  }
}