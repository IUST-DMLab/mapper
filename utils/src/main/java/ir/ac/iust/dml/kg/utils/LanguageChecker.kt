package ir.ac.iust.dml.kg.utils

object LanguageChecker {
    val englishRegex = Regex("\\w+")
    fun isEnglish(text: String) = englishRegex.matches(text)
}