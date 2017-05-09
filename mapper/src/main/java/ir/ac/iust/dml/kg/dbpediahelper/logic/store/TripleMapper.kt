package ir.ac.iust.dml.kg.dbpediahelper.logic.store

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TripleMapper {

  @Autowired lateinit var holder: KSMappingHolder

  fun writeTriples() {
    holder.writeToKS()
    holder.loadFromKS()
  }
}