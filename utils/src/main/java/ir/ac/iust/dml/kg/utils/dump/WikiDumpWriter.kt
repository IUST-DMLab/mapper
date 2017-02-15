package ir.ac.iust.dml.kg.utils.dump

import org.apache.log4j.Logger
import java.io.BufferedWriter
import java.io.Closeable
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.file.Path
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller

class WikiDumpWriter(path: Path) : Closeable {

    val logger = Logger.getLogger(this.javaClass)!!
    var jaxbMarshaller = JAXBContext.newInstance(WikiArticle::class.java).createMarshaller()
    val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(path.toFile()), "UTF8"))

    init {
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        writer.write("<mediawiki xmlns=\"http://www.mediawiki.org/xml/export-0.10/\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xsi:schemaLocation=\"http://www.mediawiki.org/xml/export-0.10/ " +
                "http://www.mediawiki.org/xml/export-0.10.xsd\" " +
                "version=\"0.10\" xml:lang=\"fa\">")
        writer.newLine()
        writer.flush()
    }

    override fun close() {
        try {
            writer.write("</mediawiki>")
            writer.close()
        } catch (e: Throwable) {
            logger.error("i couldn't close wiki dump file", e)
        }
    }

    fun write(article: WikiArticle) {
        jaxbMarshaller.marshal(article, writer)
        writer.newLine()
    }
}