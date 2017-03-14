package ir.ac.iust.dml.kg.utils

data class PagedData<T>(var data: MutableList<T>,
                        var page: Int = 0, var pageSize: Int,
                        var pageCount: Long, var rowCount: Long)