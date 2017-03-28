import ir.ac.iust.dml.kg.dbpediahelper.logic.triple.PropertyNormaller

fun main(vararg args: String) {
  println(PropertyNormaller.targetProperty("دست راست۲"))
  println(PropertyNormaller.targetProperty("دست راست ۲"))
  println(PropertyNormaller.targetProperty("دست راست 2"))
  println(PropertyNormaller.targetProperty("majid2"))
  println(PropertyNormaller.targetProperty("dbo:majid2"))
  println(PropertyNormaller.targetProperty("majid۲"))
}