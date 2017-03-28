package ir.ac.iust.dml.kg.dbpediahelper.logic.triple

object PropertyNormaller {
  val DIGIT_END_REGEX = Regex("([ابپتثجچحخدذرزژسشصضطظعغفقکگلمنوهی\\w: ]+)([۰۱۲۳۴۵۶۷۸۹\\d]+)")
  fun removeDigits(property: String): String {
    var result = property.replace("_", " ")
    if (DIGIT_END_REGEX.matches(result))
      result = DIGIT_END_REGEX.matchEntire(result)!!.groups[1]!!.value.trim()
    return result.toLowerCase()
  }
}