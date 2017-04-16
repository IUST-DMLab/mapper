package ir.ac.iust.dml.kg.utils

object PropertyNormaller {
   val DIGIT_END_REGEX = Regex("([ابپتثجچحخدذرزژسشصضطظعغفقکگلمنوهی\\w: _]+)([۰۱۲۳۴۵۶۷۸۹\\d]+)")
   fun removeDigits(property: String, removeUnderscore: Boolean = true, removeSpaces: Boolean = false): String {
      var result =
            if (removeUnderscore) property.replace("_", " ")
            else if (removeSpaces) property.replace(" ", "_")
            else property
      if (DIGIT_END_REGEX.matches(result))
         result = DIGIT_END_REGEX.matchEntire(result)!!.groups[1]!!.value.trim()
      return result.toLowerCase()
   }
}