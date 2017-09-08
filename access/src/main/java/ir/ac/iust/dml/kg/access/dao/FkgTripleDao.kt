package ir.ac.iust.dml.kg.access.dao

import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.raw.utils.PagedData
import ir.ac.iust.dml.kg.raw.utils.URIs

abstract class FkgTripleDao {

  abstract fun newVersion(module: String): Int

  abstract fun flush()

  abstract fun save(t: FkgTriple)

  abstract fun delete(subject: String, predicate: String, `object`: String)

  abstract fun deleteAll()

  abstract fun list(pageSize: Int = 20, page: Int = 10): PagedData<FkgTriple>

  abstract fun read(subject: String? = null, predicate: String? = null,
                    objekt: String? = null): MutableList<FkgTriple>

  fun convertAndSave(source: String, subject: String, property: String, `object`: String,
                     module: String, version: Int) {
    this.save(FkgTriple(source = source, subject = subject,
        predicate = URIs.convertToNotMappedFkgPropertyUri(property),
        objekt = URIs.prefixedToUri(`object`),
        module = module, version = version))
  }

  fun save(subject: String, predicate: String, `object`: String, module: String, version: Int) {
    this.save(FkgTriple(source = subject, subject = subject, predicate = predicate, objekt = `object`,
        module = module, version = version))
  }

  fun save(source: String, subject: String, predicate: String, `object`: String, module: String, version: Int) {
    this.save(FkgTriple(source = source, subject = subject, predicate = predicate, objekt = `object`,
        module = module, version = version))
  }

  fun save(source: String, subject: String, predicate: String, `object`: String,
           module: String, version: Int,
           rawText: String? = null, accuracy: Double? = null, extractionTime: Long? = null) {
    this.save(FkgTriple(source = source, subject = subject, predicate = predicate, objekt = `object`,
        accuracy = accuracy, rawText = rawText, extractionTime = extractionTime,
        module = module, version = version))
  }
}