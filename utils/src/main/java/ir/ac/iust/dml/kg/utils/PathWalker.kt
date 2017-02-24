package ir.ac.iust.dml.kg.utils

import java.nio.file.Files
import java.nio.file.Path

object PathWalker {
   fun getPath(path: Path, pattern: String): MutableList<Path> {
      val result = mutableListOf<Path>()
      walk(path, pattern, result)
      return result
   }

   private fun walk(path: Path, pattern: String, list: MutableList<Path>) {
      Files.newDirectoryStream(path).use { stream ->
         val it = stream.iterator()
         while (it.hasNext()) {
            val p = it.next()
            if (Files.isDirectory(p)) walk(p, pattern, list)
            else if (p.fileName.toString().startsWith(pattern)) list.add(p)
         }
      }
   }
}