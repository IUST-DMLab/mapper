import ir.ac.iust.dml.kg.dbpediahelper.logic.triple.PropertyNormaller

fun main(vararg args: String) {
  println(PropertyNormaller.removeDigits("دست راست۲"))
  println(PropertyNormaller.removeDigits("دست راست ۲"))
  println(PropertyNormaller.removeDigits("دست راست 2"))
  println(PropertyNormaller.removeDigits("majid2"))
  println(PropertyNormaller.removeDigits("dbo:majid2"))
  println(PropertyNormaller.removeDigits("majid۲"))
}