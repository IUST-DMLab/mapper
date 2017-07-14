package ir.ac.iust.dml.kg.dbpediahelper.logic.test

import ir.ac.iust.dml.kg.raw.utils.ConfigReader

object TestUtils {
  fun getMaxTuples() = ConfigReader.getInt("test.mode.max.tuples", "20000000")
  fun getMaxFiles() = ConfigReader.getInt("test.mode.max.files", "1000")
}