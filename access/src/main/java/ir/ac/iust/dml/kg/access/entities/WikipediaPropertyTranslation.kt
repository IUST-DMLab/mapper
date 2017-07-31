package ir.ac.iust.dml.kg.access.entities

import org.hibernate.annotations.Index
import javax.persistence.*

@Entity
@Table(name = "wiki_property_translation")
data class WikipediaPropertyTranslation(
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Index(name = "tpm_type")
    @Column(name = "template_name")
    var templateName: String? = null,
    @Index(name = "tpm_fa_property")
    @Column(name = "fa_property")
    var faProperty: String? = null,
    @Index(name = "tpm_en_property")
    @Column(name = "en_property")
    var enProperty: String? = null,
    @Column(name = "not_translated")
    var notTranslated: Boolean? = null)