/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.access.dao.none

import ir.ac.iust.dml.kg.access.dao.FkgTripleDao
import ir.ac.iust.dml.kg.access.entities.FkgTriple
import ir.ac.iust.dml.kg.raw.utils.PagedData

class EmptyFkgTripleDaoImpl : FkgTripleDao() {

  override fun newVersion(module: String) = 1

  override fun activateVersion(module: String, version: Int) = true

  override fun flush() {
  }

  override fun save(t: FkgTriple) {
  }

  override fun delete(subject: String, predicate: String, `object`: String) {
  }

  override fun deleteAll() {
  }

  override fun list(pageSize: Int, page: Int): PagedData<FkgTriple> {
    // TODO not implemented
    return PagedData(mutableListOf(), 0, 0, 0, 0)
  }

  override fun read(subject: String?, predicate: String?, objekt: String?): MutableList<FkgTriple> {
    return mutableListOf()
  }

}