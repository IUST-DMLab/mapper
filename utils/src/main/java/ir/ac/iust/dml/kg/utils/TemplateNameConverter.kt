package ir.ac.iust.dml.kg.utils

object TemplateNameConverter {
   val TEMPLATE_REGEX = Regex("(infobox|Infobox|جعبه|chembox) (اطلاعات )*(.*)")

   fun convert(templateName: String): String? {
      if (TEMPLATE_REGEX.matches(templateName))
         return TEMPLATE_REGEX.matchEntire(templateName)!!.groups[3]!!.value.trim()
      return null
   }
}