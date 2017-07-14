package ir.ac.iust.dml.kg.mapper.logic.export

import com.google.gson.GsonBuilder
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.bind.JAXBContext

object DataExporter {
    enum class ExportTypes {
        json, xml
    }

    private val gson = GsonBuilder().setPrettyPrinting().create()

    @Throws(Exception::class)
    fun <T> export(type: ExportTypes, pathKey: String, pathDefaultValue: String,
                   data: Any, clazz: Class<T>) {
        val path = ConfigReader.getPath(pathKey, pathDefaultValue)
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
        if (type == ExportTypes.json)
            stream.write(gson.toJson(data).toByteArray(Charset.forName("UTF-8")))
        else
            BufferedWriter(OutputStreamWriter(stream, "UTF-8")).use { writer ->
                val jaxbMarshaller = JAXBContext.newInstance(clazz).createMarshaller()
                jaxbMarshaller.marshal(data, writer)
                writer.newLine()
            }
    }
}