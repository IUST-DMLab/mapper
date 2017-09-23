/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

import ir.ac.iust.dml.kg.raw.utils.PropertyNormaller

fun main(vararg args: String) {
  println(PropertyNormaller.removeDigits("دست راست۲"))
  println(PropertyNormaller.removeDigits("دست راست ۲"))
  println(PropertyNormaller.removeDigits("دست راست 2"))
  println(PropertyNormaller.removeDigits("majid2"))
  println(PropertyNormaller.removeDigits("dbo:majid2"))
  println(PropertyNormaller.removeDigits("majid۲"))
}