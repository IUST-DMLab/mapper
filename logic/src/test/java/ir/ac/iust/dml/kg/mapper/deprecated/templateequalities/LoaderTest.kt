/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.deprecated.templateequalities

import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.dump.wiki.WikiDumpReader
import ir.ac.iust.dml.kg.raw.utils.dump.wiki.WikiDumpWriter
import java.nio.file.Files

/**
 * It's not a real standard test!
 */

fun main(args: Array<String>) {
  val path = ConfigReader.getPath("wiki.dump.article", "~/.pkg/data/fawiki-latest-pages-articles.xml")
  Files.createDirectories(path.parent)
  if (!Files.exists(path)) {
    throw Exception("There is no file ${path.toAbsolutePath()} existed.")
  }

  var count = 0
  val startTime = System.currentTimeMillis()
  WikiDumpReader(path).use { reader ->
    WikiDumpWriter(path.parent.resolve("just_templates.xml")).use { writer ->
      while (reader.hasNext()) {
        val article = reader.next()
        if (count % 10000 == 0) println(count)
        if (article.ns == 10
            && (article.title!!.startsWith("الگو:جعبه") || article.title!!.startsWith("الگو:Infobox"))
            && article.revision!!.text!!.contains("data1"))
          writer.write(article)
        count++
      }
    }
  }

  println("running time: ${(System.currentTimeMillis() - startTime) / 1000} seconds")
}