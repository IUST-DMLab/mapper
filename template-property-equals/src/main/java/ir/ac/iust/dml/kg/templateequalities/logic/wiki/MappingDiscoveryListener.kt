package ir.ac.iust.dml.kg.templateequalities.logic.wiki

import ir.ac.iust.dml.kg.utils.dump.WikiArticle

interface MappingDiscoveryListener {
    fun discovered(article: WikiArticle, mappings: MutableMap<String, String>)
}