/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.access.utils

object TemplateNameConverter {
  private val TEMPLATE_REGEX = Regex("(infobox|Infobox|جعبه|chembox) (اطلاعات )*(.*)")

  fun convert(templateName: String): String? {
    if (TEMPLATE_REGEX.matches(templateName))
      return TEMPLATE_REGEX.matchEntire(templateName)!!.groups[3]!!.value.trim()
    return null
  }
}