package ir.ac.iust.dml.kg.access.dao

import ir.ac.iust.dml.kg.access.entities.FkgPropertyMapping
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import ir.ac.iust.dml.kg.raw.utils.PagedData
import ir.ac.iust.dml.kg.raw.utils.PrefixService
import ir.ac.iust.dml.kg.raw.utils.Transformers

abstract class FkgTripleDao {

  private val transformers = Transformers()

  abstract fun save(t: FkgTriple, mapping: FkgPropertyMapping?, approved: Boolean = false)

  abstract fun deleteAll()

  abstract fun list(pageSize: Int = 20, page: Int = 10): PagedData<FkgTriple>

  abstract fun read(subject: String? = null, predicate: String? = null, objekt: String? = null,
                    status: MappingStatus? = null): MutableList<FkgTriple>

  fun saveRawTriple(source: String, subject: String, objeck: String, property: String) {
    this.save(FkgTriple(source = source, subject = subject,
        predicate = PrefixService.convertFkgProperty(property),
        objekt = PrefixService.prefixToUri(objeck)), null)
  }
}