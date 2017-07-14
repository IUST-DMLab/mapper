package ir.ac.iust.dml.kg.mapper.logic

import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import java.nio.file.Files
import java.nio.file.Path

object PathUtils {
  fun getPath(key: String, defaultValue: String): Path {
    val path = ConfigReader.getPath(key, defaultValue)
    if (!Files.exists(path.parent)) Files.createDirectories(path.parent)
    if (!Files.exists(path)) {
      throw Exception("There is no file ${path.toAbsolutePath()} existed.")
    }
    return path
  }

  fun getAbstractPath() = getPath("wiki.folder.abstracts", "~/.pkg/data/abstracts")

  fun getTriplesPath() = getPath("wiki.folder.tuples", "~/.pkg/data/tuples")

  fun getWithoutInfoboxPath() = getPath("wiki.folder.without.info.box", "~/.pkg/data/without_infobox")

  fun getWithInfoboxPath() = getPath("wiki.folder.with.info.box", "~/.pkg/data/with_infobox")
}