package ir.ac.iust.dml.kg.mapper.logic.viewer

import ir.ac.iust.dml.kg.raw.utils.URIs

object PropertyFilter {

  val propertyLabelCache = mutableMapOf<String, String?>(
      URIs.prefixedToUri("name")!! to "نام",
      URIs.prefixedToUri("order")!! to "عنوان",
      URIs.prefixedToUri("spouse")!! to "همسر",
      URIs.prefixedToUri("country")!! to "کشور",
      URIs.prefixedToUri("child")!! to "فرزندان",
      URIs.prefixedToUri("children")!! to "فرزندان",
      URIs.prefixedToUri("years")!! to "سال‌ها",
      URIs.prefixedToUri("activeYears")!! to "سال‌های فعالیت",
      URIs.prefixedToUri("activeYearsEndDate")!! to "سال پایان فعالیت",
      URIs.prefixedToUri("activeYearsStartDate")!! to "سال شروع فعالیت",
      URIs.prefixedToUri("vicepresident")!! to "نایب رییس",
      URIs.prefixedToUri("religion")!! to "دین",
      URIs.prefixedToUri("residence")!! to "محل اقامت",
      URIs.prefixedToUri("occupation")!! to "شغل",
      URIs.prefixedToUri("successor")!! to "بعدی",
      URIs.prefixedToUri("predecessor")!! to "قبلی",
      URIs.prefixedToUri("deathDate")!! to "زادمرگ",
      URIs.prefixedToUri("deathDate")!! to "زادمرگ",
      URIs.prefixedToUri("deathPlace")!! to "محل مرگ",
      URIs.prefixedToUri("birthDate")!! to "زادروز",
      URIs.prefixedToUri("birthPlace")!! to "محل تولد",
      URIs.prefixedToUri("almaMater")!! to "محل آموزش",
      URIs.prefixedToUri("award")!! to "جوایز",
      URIs.prefixedToUri("nationality")!! to "ملیت",
      URIs.prefixedToUri("homepage")!! to "صفحه اینترنتی"
  )

  val filteredPredicates = listOf(
      Regex(URIs.prefixedToUri(URIs.fkgNotMappedPropertyPrefix + ":")!!.replace(".", "\\.") + "[\\d\\w]*"),
      Regex(URIs.fkgOntologyPrefixUrl.replace(".", "\\.") + ".*[yY]ear.*"),
      Regex(URIs.getFkgOntologyPropertyUri("wiki").replace(".", "\\.") + ".*"),
      Regex(URIs.getFkgOntologyPropertyUri("ویکی").replace(".", "\\.") + ".*"),
      Regex(URIs.picture.replace(".", "\\.")),
      Regex(URIs.instanceOf.replace(".", "\\.")),
      Regex(URIs.type.replace(".", "\\.")),
      Regex(URIs.abstract.replace(".", "\\.")),
      Regex(URIs.label.replace(".", "\\.")),
      Regex(URIs.prefixedToUri("foaf:homepage")!!.replace(".", "\\.")),
      Regex(URIs.prefixedToUri("fkgo:predecessor")!!.replace(".", "\\.")),
      Regex(URIs.prefixedToUri("fkgo:successor")!!.replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("categoryMember").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("activeYears").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("source").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("data").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("fontSize").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("imageSize").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("depictionDescription").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("nameData").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("quotation").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("quote").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("quoted").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("signature").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("width").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("sourceAlign").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("sourceAlignment").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("align").replace(".", "\\.")),
      Regex(URIs.getFkgOntologyPropertyUri("alignment").replace(".", "\\."))
  )
}