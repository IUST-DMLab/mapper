package ir.ac.iust.dml.kg.access.dao

import ir.ac.iust.dml.kg.access.entities.FkgPropertyMapping
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.raw.utils.PagedData
import ir.ac.iust.dml.kg.raw.utils.Transformers
import ir.ac.iust.dml.kg.raw.utils.URIs

abstract class FkgTripleDao {

  private val transformers = Transformers()

  abstract fun newVersion(module: String): Int

  abstract fun flush()

  abstract fun save(t: FkgTriple, module: String, version: Int, mapping: FkgPropertyMapping?,
                    approved: Boolean = false)

  abstract fun delete(subject: String, predicate: String, `object`: String)

  abstract fun deleteAll()

  abstract fun list(pageSize: Int = 20, page: Int = 10): PagedData<FkgTriple>

  abstract fun read(subject: String? = null, predicate: String? = null, objekt: String? = null,
                    status: MappingStatus? = null): MutableList<FkgTriple>

  fun convertAndSave(source: String, subject: String, objeck: String, property: String,
                     module: String, version: Int) {
    this.save(FkgTriple(source = source, subject = subject,
        predicate = URIs.convertToNotMappedFkgPropertyUri(property),
        objekt = URIs.prefixedToUri(objeck)), module, version, null)
  }

  fun save(source: String, subject: String, objeck: String, property: String, module: String, version: Int) {
    this.save(FkgTriple(source = source, subject = subject, predicate = property, objekt = objeck),
        module, version, null)
  }

  fun save(source: String, subject: String, objeck: String, module: String,
           property: String, rawText: String? = null, accuracy: Double? = null, extractionTime: Long? = null,
           version: Int) {
    this.save(FkgTriple(source = source, subject = subject, predicate = property, objekt = objeck,
        accuracy = accuracy, rawText = rawText, extractionTime = extractionTime),
        module, version, null)
  }
}