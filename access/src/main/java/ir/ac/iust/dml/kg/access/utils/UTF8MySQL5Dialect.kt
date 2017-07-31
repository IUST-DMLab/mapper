package ir.ac.iust.dml.kg.access.utils

import org.hibernate.dialect.MySQL5Dialect

class UTF8MySQL5Dialect : MySQL5Dialect() {
  override fun getTableTypeString(): String {
    return "ENGINE=InnoDB DEFAULT CHARSET=utf8"
  }
}