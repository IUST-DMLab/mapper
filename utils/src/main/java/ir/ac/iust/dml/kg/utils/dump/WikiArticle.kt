package ir.ac.iust.dml.kg.utils.dump

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "page")
data class WikiArticle(
        @XmlElement var title: String? = null,
        @XmlElement var ns: Int = 0,
        @XmlElement var id: Long = 0,
        @XmlElement var revision: Revision? = null) {
}