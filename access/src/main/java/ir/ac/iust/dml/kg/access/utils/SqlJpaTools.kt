/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.access.utils

import ir.ac.iust.dml.kg.raw.utils.PagedData
import org.hibernate.Criteria
import org.hibernate.Session
import org.hibernate.criterion.Criterion
import org.hibernate.criterion.Order
import org.hibernate.criterion.Projections

object SqlJpaTools {

  @Suppress("UNCHECKED_CAST")
  fun <T> page(clazz: Class<T>, page: Int, pageSize: Int, c: Criteria): PagedData<T> {
    c.setFirstResult(page * pageSize).setMaxResults(pageSize)
    val data = c.list() as MutableList<T>
    val count = c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
        .setProjection(Projections.rowCount()).uniqueResult() as Long
    return PagedData(data, page, pageSize, count / pageSize.toLong(), count)
  }

  @Suppress("UNCHECKED_CAST")
  fun <T> page(clazz: Class<T>, page: Int, pageSize: Int,
               session: Session, orders: List<Order>?, vararg criteria: Criterion): PagedData<T> {
    val c = session.createCriteria(clazz)
    for (criterion in criteria) c.add(criterion)
    c.setFirstResult(page * pageSize)
    if (pageSize > 0) c.setMaxResults(pageSize)
    if (orders != null) for (order in orders) c.addOrder(order)
    val data = c.list() as MutableList<T>
    val count = c.setFirstResult(0).setMaxResults(1)
        .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
        .setProjection(Projections.rowCount()).uniqueResult() as Long
    if (pageSize == 0)
      return PagedData(data, page, pageSize, 1, count)
    else
      return PagedData(data, page, pageSize, count / pageSize.toLong(), count)
  }

  fun conditionalCriteria(vararg conditionAndCriterion: Any): Array<Criterion> {
    var index = 0
    val list = mutableListOf<Criterion>()
    while (index < conditionAndCriterion.size) {
      val condition = conditionAndCriterion[index++] as Boolean
      val criterion = conditionAndCriterion[index++] as Criterion
      if (condition) list.add(criterion)
    }
    return list.toTypedArray()
  }
}