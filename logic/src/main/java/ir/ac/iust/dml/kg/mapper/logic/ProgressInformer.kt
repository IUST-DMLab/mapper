/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.mapper.logic

import org.apache.log4j.Logger

/**
 * Knowledge Runner is using system out to check the progress of each tasks.
 * This class write each mapping tasks on console
 */
class ProgressInformer(private val numberOfSteps: Int) {
  private val LOGGER = Logger.getLogger(this.javaClass)!!

  fun stepDone(step: Int) {
    val percent = step * 100 / numberOfSteps
    println("#progress $percent")
    LOGGER.info("progress has been reached to step $step from $numberOfSteps ($percent%)")
  }

  fun done() {
    println("#progress 100")
    LOGGER.info("task finished!")
  }
}