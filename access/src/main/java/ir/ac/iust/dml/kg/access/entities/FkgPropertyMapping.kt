package ir.ac.iust.dml.kg.access.entities

import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import org.hibernate.annotations.Index
import javax.persistence.*

@Entity
@Table(name = "fkg_mapping_property")
data class FkgPropertyMapping(
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Index(name = "dbpm_language")
    @Column(name = "language")
    var language: String? = null,
    @Index(name = "dbpm_type")
    @Column(name = "type")
    var templateName: String? = null,
    @Index(name = "dbpm_class")
    @Column(name = "class")
    var ontologyClass: String? = null,
    @Index(name = "dbpm_template_property")
    @Column(name = "template_property")
    var templateProperty: String? = null,
    @Index(name = "dbpm_ontology_property")
    @Column(name = "ontology_property")
    var ontologyProperty: String? = null,
    @Index(name = "dbpm_status")
    @Enumerated
    @Column(name = "status")
    var status: MappingStatus? = null,
    @Column(name = "approved")
    var approved: Boolean? = null,
    @Index(name = "dbpm_update_epoch")
    @Column(name = "update_epoch")
    var updateEpoch: Long? = null,
    @Index(name = "dbpm_count")
    @Column(name = "count")
    var tupleCount: Long? = null,
    @Index(name = "dbpm_temp_prop_lang")
    @Column(name = "temp_prop_lang")
    var templatePropertyLanguage: String? = null)