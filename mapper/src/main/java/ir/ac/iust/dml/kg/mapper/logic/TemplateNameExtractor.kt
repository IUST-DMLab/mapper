package ir.ac.iust.dml.kg.mapper.logic

object TemplateNameExtractor {

  fun getName(typeAndName: String): String {
    val n = typeAndName.toLowerCase()
    if (n.startsWith("infobox ") && n.length > 8) return n.substring(8).trim()
    if (n.startsWith("chembox ") && n.length > 8) return n.substring(8).trim()
    if (n.startsWith("جعبه اطلاعات ") && n.length > 13) return n.substring(13).trim()
    if (n.startsWith("جعبه ") && n.length > 5) return n.substring(5).trim()
    return n
  }
}