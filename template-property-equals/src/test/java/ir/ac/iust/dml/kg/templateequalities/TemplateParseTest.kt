package ir.ac.iust.dml.kg.templateequalities

import ir.ac.iust.dml.kg.templateequalities.logic.wiki.InfoboxTemplateReader
import ir.ac.iust.dml.kg.templateequalities.logic.wiki.MappingDiscoveryListener
import ir.ac.iust.dml.kg.utils.ConfigReader
import ir.ac.iust.dml.kg.utils.dump.WikiArticle
import java.nio.file.Files

fun main(args: Array<String>) {

    val WIKI_DUMP_ARTICLE = "wiki.template.dump.article"
    val config = ConfigReader.getConfig(mapOf(WIKI_DUMP_ARTICLE to "~/pkg/data/just_templates.xml"))
    val path = ConfigReader.getPath(config[WIKI_DUMP_ARTICLE]!! as String)
    Files.createDirectories(path.parent)
    if (!Files.exists(path)) {
        throw Exception("There is no file ${path.toAbsolutePath()} existed.")
    }

    val startTime = System.currentTimeMillis()
    InfoboxTemplateReader.read(path, object : MappingDiscoveryListener {
        override fun discovered(article: WikiArticle, mappings: MutableMap<String, String>) {
            println("${article.title}: ${mappings.size} mappings")
        }
    })

    println("running time: ${(System.currentTimeMillis() - startTime) / 1000} seconds")
}