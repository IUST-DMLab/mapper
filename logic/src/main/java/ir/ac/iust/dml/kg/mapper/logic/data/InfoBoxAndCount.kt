package ir.ac.iust.dml.kg.mapper.logic.data

data class InfoBoxAndCount(val infoBox: String, val propertyCount: Int, var tree: List<String> = listOf<String>())