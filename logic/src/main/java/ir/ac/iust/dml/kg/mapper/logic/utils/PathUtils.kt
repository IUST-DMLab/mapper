/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.utils

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

  fun getInterLinkPath() = getPath("wiki.folder.inter.link", "~/.pkg/data/lang_links/en.json")

  fun getTriplesPath() = getPath("wiki.folder.tuples", "~/.pkg/data/tuples")

  fun getTriplesTestPath() = getTriplesPath().resolve("test")!!

  fun getCategoryTriplesPath() = getPath("wiki.folder.category.tuples", "~/.pkg/data/category_tuples")

  fun getWithoutInfoboxPath() = getPath("wiki.folder.without.info.box", "~/.pkg/data/without_infobox")

  fun getWithInfoboxPath() = getPath("wiki.folder.with.info.box", "~/.pkg/data/with_infobox")
}