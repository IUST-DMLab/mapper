package ir.ac.iust.dml.kg.dbpediahelper.logic.store

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.dao.knowldegestore.KnowledgeStoreFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.dao.virtuoso.VirtuosoFkgTripleDaoImpl
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.dbpediahelper.logic.RedirectLogic
import ir.ac.iust.dml.kg.dbpediahelper.logic.type.StoreType
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PathWalker
import ir.ac.iust.dml.kg.raw.utils.PrefixService
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.file.Files

@Service
class AmbiguityLogic {

    val logger = Logger.getLogger(this.javaClass)!!
    @Autowired private lateinit var tripleDao: FkgTripleDao

  fun write(storeType: StoreType = StoreType.knowledgeStore) {

        val disambiguationFolder = ConfigReader.getPath("extractor.disambiguations.folder", "~/.pkg/data/disambiguations")
        Files.createDirectories(disambiguationFolder.parent)
        if (!Files.exists(disambiguationFolder)) {
            throw Exception("There is no file ${disambiguationFolder.toAbsolutePath()} existed.")
        }

        val store = when (storeType) {
          StoreType.mysql -> tripleDao
          StoreType.virtuoso -> VirtuosoFkgTripleDaoImpl()
            else -> KnowledgeStoreFkgTripleDaoImpl()
        }

        val gson = Gson()

        val maxNumberOfDisambiguation = ConfigReader.getInt("test.mode.max.disambiguation", "10000000")
        val DISAMBIGUATED_FROM = PrefixService.prefixToUri(PrefixService.DISAMBIGUATED_FROM_URI)

        val type = object : TypeToken<List<RedirectLogic.Ambiguity>>() {}.type
        val files = PathWalker.getPath(disambiguationFolder)
        var i = 0
        files.forEach {
            try {
                BufferedReader(InputStreamReader(FileInputStream(it.toFile()), "UTF8")).use { reader ->
                    val map: List<RedirectLogic.Ambiguity> = gson.fromJson(reader, type)
                    map.forEach { a ->
                        a.field.forEach { f ->
                            i++
                            if (i < maxNumberOfDisambiguation) {
                                if (i % 1000 == 0) logger.info("writing disambiguation $i: $a to $f")
                                store.save(FkgTriple(
                                        subject = PrefixService.getFkgResourceUrl(f),
                                        predicate = DISAMBIGUATED_FROM,
                                        objekt = if (a.title!!.contains("(ابهام زدایی)")) a.title!!.substringBefore("(ابهام زدایی)")
                                        else a.title
                                ), null, true)
                            }
                        }
                    }
                }
            } catch (th: Throwable) {
                logger.error(th)
            }
        }
    }
}