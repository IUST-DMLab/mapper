package ir.ac.iust.dml.kg.templateequalities.logic

import ir.ac.iust.dml.kg.templateequalities.access.dao.TemplatePropertyMappingDao
import ir.ac.iust.dml.kg.templateequalities.access.entities.WikipediaPropertyTranslation
import ir.ac.iust.dml.kg.templateequalities.logic.wiki.InfoboxTemplateReader
import ir.ac.iust.dml.kg.templateequalities.logic.wiki.MappingDiscoveryListener
import ir.ac.iust.dml.kg.utils.ConfigReader
import ir.ac.iust.dml.kg.utils.LanguageChecker
import ir.ac.iust.dml.kg.utils.dump.WikiArticle
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Files

@Service
class Loader {

    @Autowired
    lateinit var dao: TemplatePropertyMappingDao
    val logger = Logger.getLogger(this.javaClass)!!

    @Throws(Exception::class)
    fun load() {
        val WIKI_DUMP_ARTICLE = "wiki.dump.article"
       val config = ConfigReader.getConfig(mapOf(WIKI_DUMP_ARTICLE to "~/.pkg/data/fawiki-latest-pages-articles.xml"))
        val path = ConfigReader.getPath(config[WIKI_DUMP_ARTICLE]!! as String)
        Files.createDirectories(path.parent)
        if (!Files.exists(path)) {
            throw Exception("There is no file ${path.toAbsolutePath()} existed.")
        }

        InfoboxTemplateReader.read(path, object : MappingDiscoveryListener {
            override fun discovered(article: WikiArticle, mappings: MutableMap<String, String>) {
                val name = article.title!!.substringAfter("الگو:").trim().substringAfter("Infobox").trim()
                for ((key, value) in mappings) {
                    if (value.length < 20 && key.length < 20)
                       dao.save(WikipediaPropertyTranslation(type = name, enProperty = value, faProperty = key,
                                notTranslated = LanguageChecker.isEnglish(key)))
                }
                println("${article.title}: ${mappings.size} mappings")
            }
        })
    }
}