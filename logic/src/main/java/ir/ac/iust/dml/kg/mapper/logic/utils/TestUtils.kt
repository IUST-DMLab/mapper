/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic.utils

import ir.ac.iust.dml.kg.raw.utils.ConfigReader
import java.lang.management.ManagementFactory

object TestUtils {
  fun isDebugMode() = ManagementFactory.getRuntimeMXBean().inputArguments.toString().indexOf("-agentlib:jdwp") > 0
  fun getMaxTuples() = ConfigReader.getInt("test.mode.max.tuples", "20000000")
  fun getMaxFiles() = ConfigReader.getInt("test.mode.max.files", "1000")
}