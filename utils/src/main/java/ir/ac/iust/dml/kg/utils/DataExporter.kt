package ir.ac.iust.dml.kg.utils

import com.google.gson.GsonBuilder
import org.apache.log4j.Logger
import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.bind.JAXBContext

object DataExporter {
    enum class ExportTypes {
        json, xml
    }

    private val logger = Logger.getLogger(this.javaClass)!!
    private val gson = GsonBuilder().setPrettyPrinting().create()

    @Throws(Exception::class)
    fun <T> export(type: ExportTypes, pathKey: String, pathDefaultValue: String,
                   data: Any, clazz: Class<T>) {
        val config = ConfigReader.getConfig(mapOf(pathKey to pathDefaultValue))
        val path = ConfigReader.getPath(config[pathKey]!! as String)
        Files.createDirectories(path.parent)
        if (!Files.exists(path)) {
            throw Exception("There is no file ${path.toAbsolutePath()} existed.")
        }
        export(type, data, FileOutputStream(path.toFile()), clazz)
    }

    @Throws(Exception::class)
    fun <T> export(type: ExportTypes, path: Path, data: Any, clazz: Class<T>) {
        export(type, data, FileOutputStream(path.toFile()), clazz)
    }

    @Throws(Exception::class)
    fun <T> export(type: ExportTypes, data: Any, stream: OutputStream, clazz: Class<T>) {
        BufferedWriter(OutputStreamWriter(stream, "UTF-8")).use { writer ->
            if (type == ExportTypes.json)
                gson.toJson(data, writer)
            else {
                val jaxbMarshaller = JAXBContext.newInstance(clazz).createMarshaller()
                jaxbMarshaller.marshal(data, writer)
            }
            writer.newLine()
        }
    }
}