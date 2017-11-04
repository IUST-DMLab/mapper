/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.access.dao.virtuoso

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import ir.ac.iust.dml.kg.raw.utils.PagedData
import ir.ac.iust.dml.kg.raw.utils.URIs
import ir.ac.iust.dml.kg.virtuoso.connector.VirtuosoConnector

class VirtuosoFkgTripleDaoImpl : FkgTripleDao() {

  override fun newVersion(module: String) = 1

  override fun activateVersion(module: String, version: Int) = true

  private val connector = VirtuosoConnector(ConfigReader.getString("virtuoso.graph",
      URIs.defaultContext))

  override fun save(t: FkgTriple) {
    if (t.objekt == null || t.objekt!!.trim().isEmpty()) {
      println("short triple here: ${t.subject} ${t.predicate} ${t.objekt}")
      return
    }
    if (t.objekt!!.contains("://") && !t.objekt!!.contains(' '))
      connector.addResource(t.subject, t.predicate, t.objekt)
    else connector.addLiteral(t.subject, t.predicate, t.objekt)
  }

  override fun flush() {
    connector.close()
  }

  override fun delete(subject: String, predicate: String, `object`: String) {
    if (`object`.trim().isEmpty()) {
      println("short triple here: $subject $predicate $`object`")
      return
    }
    if (`object`.contains("://") && !`object`.contains(' '))
      connector.removeResource(subject, predicate, `object`)
    else connector.removeLiteral(subject, predicate, `object`)
  }

  override fun deleteAll() {
    connector.clear()
  }

  override fun list(pageSize: Int, page: Int): PagedData<FkgTriple> {
    // TODO not implemented
    return PagedData(mutableListOf(), 0, 0, 0, 0)
  }

  override fun read(subject: String?, predicate: String?, objekt: String?): MutableList<FkgTriple> {
    // TODO not implemented
    return mutableListOf()
  }

}
