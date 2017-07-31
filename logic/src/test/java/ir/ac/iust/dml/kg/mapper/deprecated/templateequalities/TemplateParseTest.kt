package ir.ac.iust.dml.kg.mapper.deprecated.templateequalities

import ir.ac.iust.dml.kg.mapper.deprecated.templateequalities.logic.wiki.InfoboxTemplateReader
import ir.ac.iust.dml.kg.mapper.deprecated.templateequalities.logic.wiki.MappingDiscoveryListener
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.dump.wiki.WikiArticle
import java.nio.file.Files

fun main(args: Array<String>) {
  val path = ConfigReader.getPath("wiki.template.dump.article", "~/.pkg/data/just_templates.xml")
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