package ir.ac.iust.dml.kg.templateequalities.logic

import ir.ac.iust.dml.kg.access.dao.WikipediaPropertyTranslationDao
import ir.ac.iust.dml.kg.access.entities.WikipediaPropertyTranslation
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.LanguageChecker
import ir.ac.iust.dml.kg.raw.utils.dump.wiki.WikiArticle
import ir.ac.iust.dml.kg.templateequalities.logic.wiki.InfoboxTemplateReader
import ir.ac.iust.dml.kg.templateequalities.logic.wiki.MappingDiscoveryListener
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Files

@Service
class Loader {

   @Autowired
   lateinit var dao: WikipediaPropertyTranslationDao
   val logger = Logger.getLogger(this.javaClass)!!

   @Throws(Exception::class)
   fun load() {
      val path = ConfigReader.getPath("wiki.dump.article", "~/.pkg/data/fawiki-latest-pages-articles.xml")
      Files.createDirectories(path.parent)
      if (!Files.exists(path)) {
         throw Exception("There is no file ${path.toAbsolutePath()} existed.")
      }

      InfoboxTemplateReader.read(path, object : MappingDiscoveryListener {
         override fun discovered(article: WikiArticle, mappings: MutableMap<String, String>) {
            val name = article.title!!.substringAfter("الگو:").trim().toLowerCase()
            for ((key, value) in mappings) {
               if (value.length < 20 && key.length < 20)
                  dao.save(WikipediaPropertyTranslation(templateName = name, enProperty = value, faProperty = key,
                        notTranslated = LanguageChecker.isEnglish(key)))
            }
            println("${article.title}: ${mappings.size} mappings")
         }
      })
   }
}