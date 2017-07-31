package ir.ac.iust.dml.kg.mapper.deprecated.templateequalities.logic.wiki

import ir.ac.iust.dml.kg.raw.utils.dump.wiki.WikiArticle

interface MappingDiscoveryListener {
  fun discovered(article: WikiArticle, mappings: MutableMap<String, String>)
}