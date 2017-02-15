package ir.ac.iust.dml.kg.utils.dump

import javax.xml.bind.annotation.XmlElement

data class Contributor(
        @XmlElement var username: String? = null,
        @XmlElement var id: Long? = null)