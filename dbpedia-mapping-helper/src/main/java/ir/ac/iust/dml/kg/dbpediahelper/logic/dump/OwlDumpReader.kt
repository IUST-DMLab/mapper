package ir.ac.iust.dml.kg.dbpediahelper.logic.dump

import org.apache.log4j.Logger
import java.io.BufferedReader
import java.io.Closeable
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.file.Path

class OwlDumpReader(path: Path) : Iterator<MutableList<Triple>>, Closeable {

    val logger = Logger.getLogger(this.javaClass)!!
    val reader = BufferedReader(InputStreamReader(FileInputStream(path.toFile()), "UTF8"))
    var lastTriples: MutableList<Triple>? = null

    init {
        lastTriples = fetchNextTriples()
    }

    val valueRegex = Regex("([^\"]*)\"([^\"]*)\"(.*)")
    val SPACE_KEEPER_DELIM = '~'
    private fun groups(text: String, regex: Regex, vararg groupNumbers: Int): String {
        val groups = regex.matchEntire(text)!!.groups
        val builder = StringBuilder()
        for (i in groupNumbers) builder.append(groups[i]!!.value.trim().replace(' ', SPACE_KEEPER_DELIM)).append(" ")
        if (builder.isNotEmpty()) builder.setLength(builder.length - 1)
        return builder.toString()
    }

    private fun fix(str: String) = str.replace(SPACE_KEEPER_DELIM, ' ')

    private fun fetchNextTriples(): MutableList<Triple>? {
        val buffer = StringBuffer()
        while (true) {
            val line = reader.readLine() ?: break
            if (line.startsWith("@")) continue
            if (line.startsWith("##")) continue
            buffer.append(line)
            if (line.endsWith(".")) {
                buffer.setLength(buffer.length - 1)
                val cleaned = buffer.trim().replace(Regex("\\s+"), " ")
                val triples = mutableListOf<Triple>()
                val semiColonParts = cleaned.split(";")
                for (i in semiColonParts.indices) {
                    var semiColonPart = semiColonParts[i]
                    while (semiColonPart.contains("\""))
                        semiColonPart = groups(semiColonPart, valueRegex, 1, 2, 3)
                    val parts = semiColonPart.trim().split(" ")
                    if (parts.size == 3)
                        triples.add(Triple(subject = fix(parts[0]),
                                predicate = fix(parts[1]), objekt = fix(parts[2])))
                    else if (parts.size == 2 && triples.isNotEmpty())
                        triples.add(Triple(subject = triples.first().subject,
                                predicate = fix(parts[0]), objekt = fix(parts[1])))
                    else assert(false)
                }
                return triples
            }
        }
        return null
    }

    override fun close() {
        try {
            reader.close()
        } catch (e: Throwable) {
            logger.error("ir couldn't close ontology dump file", e)
        }
    }

    override fun hasNext(): Boolean {
        return lastTriples != null
    }

    override fun next(): MutableList<Triple> {
        val oldTriples = lastTriples!!
        lastTriples = fetchNextTriples()
        return oldTriples
    }
}