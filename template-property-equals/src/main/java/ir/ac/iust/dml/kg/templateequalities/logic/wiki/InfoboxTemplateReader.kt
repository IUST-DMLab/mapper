package ir.ac.iust.dml.kg.templateequalities.logic.wiki

import ir.ac.iust.dml.kg.utils.dump.WikiDumpReader
import org.sweble.wikitext.parser.nodes.WtName
import org.sweble.wikitext.parser.nodes.WtTemplateArgument
import org.sweble.wikitext.parser.nodes.WtText
import org.sweble.wikitext.parser.nodes.WtValue
import java.nio.file.Path

object InfoboxTemplateReader {

    val INVALID_ARTICLE_REGEX = Regex("\\\\|/")

    fun read(path: Path, listener: MappingDiscoveryListener) {
        WikiDumpReader(path).use {
            reader ->
            while (reader.hasNext()) {
                val article = reader.next()
                if (article.ns == 10
                        && article.title != null
                        && (/*article.title!!.startsWith("الگو:جعبه") || */article.title!!.startsWith("الگو:Infobox"))
                        && article.revision!!.text!!.contains("data1")
                        && !article.title!!.contains(INVALID_ARTICLE_REGEX)) {
                    val page = EasyWikiParser.parse(article.revision!!.text!!)
                    val templates = mutableListOf<WtTemplateArgument>()
                    EasyWikiParser.getAllNode(WtTemplateArgument::class.java, page, templates)
                    val labels = mutableMapOf<String, String>()
                    val data = mutableMapOf<String, String>()
                    templates.forEach {
                        if (it.size > 1 && it[0] is WtName && it[0].size > 0 && it[0][0] is WtText
                                && it[1] is WtValue && it[1].size > 0 && it[1][0] is WtText) {
                            val key = EasyWikiParser.getPlainText(it[0])
                            val value = EasyWikiParser.getPlainText(it[1])
                            if (key.startsWith("label"))
                                labels[key] = value
                            if (key.startsWith("data"))
                                data[key] = value
                        }
                    }
                    val mappings = mutableMapOf<String, String>()
                    for ((key, value) in labels) {
                        val dataKey = "data" + key.substring(5)
                        val dataValue = data[dataKey]
                        if (dataValue != null) {
                            mappings[EasyWikiParser.clear(value)] = EasyWikiParser.clear(dataValue)
                        }
                    }
                    listener.discovered(article, mappings)
                }
            }
        }
    }
}