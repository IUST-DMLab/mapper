package ir.ac.iust.dml.kg.templateequalities

import ir.ac.iust.dml.kg.utils.ConfigReader
import ir.ac.iust.dml.kg.utils.dump.WikiDumpReader
import ir.ac.iust.dml.kg.utils.dump.WikiDumpWriter
import java.nio.file.Files

/**
 * It's not a real standard test!
 */

fun main(args: Array<String>) {
    val WIKI_DUMP_ARTICLE = "wiki.dump.article"
    val config = ConfigReader.getConfig(mapOf(WIKI_DUMP_ARTICLE to "~/pkg/data/fawiki-latest-pages-articles.xml"))
    val path = ConfigReader.getPath(config[WIKI_DUMP_ARTICLE]!! as String)
    Files.createDirectories(path.parent)
    if (!Files.exists(path)) {
        throw Exception("There is no file ${path.toAbsolutePath()} existed.")
    }

    var count = 0
    val startTime = System.currentTimeMillis()
    WikiDumpReader(path).use {
        reader ->
        WikiDumpWriter(path.parent.resolve("just_templates.xml")).use {
            writer ->
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