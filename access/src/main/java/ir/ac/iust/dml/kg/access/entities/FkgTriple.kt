package ir.ac.iust.dml.kg.access.entities

import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import org.hibernate.annotations.Index
import javax.persistence.*

@Entity
@org.hibernate.annotations.Table(appliesTo = "fkg_triple",
        indexes = arrayOf(Index(
                name = "t_source_predicate_object",
                columnNames = arrayOf("subject", "predicate", "object")
        )))
@Table(name = "fkg_triple")
class FkgTriple(
        @Id
        @Column(name = "id")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,
        @Column(name = "source")
        var source: String? = null,
        @Column(name = "subject")
        var subject: String? = null,
        @Column(name = "predicate")
        var predicate: String? = null,
        @Column(name = "object")
        var objekt: String? = null,
        @Column(name = "template_type")
        var templateName: String? = null,
        @Column(name = "raw_property")
        var rawProperty: String? = null,
        @Index(name = "t_status")
        @Enumerated
        @Column(name = "status")
        var status: MappingStatus? = null,
        @Column(name = "language")
        var language: String? = null
)