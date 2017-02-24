package ir.ac.iust.dml.kg.dbpediahelper.logic.triple

import ir.ac.iust.dml.kg.utils.ConfigReader
import ir.ac.iust.dml.kg.utils.PathWalker
import org.springframework.stereotype.Service
import java.nio.file.Files

@Service
class TripleImporter {

   @Throws(Exception::class)
   fun traverse() {
      val WIKI_DUMP_ARTICLE = "wiki.triple.input.folder"
      val config = ConfigReader.getConfig(mapOf(WIKI_DUMP_ARTICLE to "~/.pkg/data/triples"))
      val path = ConfigReader.getPath(config[WIKI_DUMP_ARTICLE]!! as String)
      Files.createDirectories(path.parent)
      if (!Files.exists(path)) {
         throw Exception("There is no file ${path.toAbsolutePath()} existed.")
      }

      val result = PathWalker.getPath(path, "infobox.json")
      for (p in result) {
         TripleJsonFileReader(p).use { reader ->
            while (reader.hasNext()) {
               val triple = reader.next()
               println(triple)
            }
         }
      }
   }
}