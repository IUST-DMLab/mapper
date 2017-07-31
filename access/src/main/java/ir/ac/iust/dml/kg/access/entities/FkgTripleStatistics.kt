package ir.ac.iust.dml.kg.access.entities

import ir.ac.iust.dml.kg.access.entities.enumerations.TripleStatisticsType
import org.hibernate.annotations.Index
import javax.persistence.*

@Entity
@Table(name = "fkg_triple_stats")
data class FkgTripleStatistics(
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Index(name = "ts_template_name")
    @Column(name = "template_name")
    var templateName: String? = null,
    @Index(name = "ts_property")
    @Column(name = "property")
    var property: String? = null,
    @Index(name = "ts_entity")
    @Column(name = "entity")
    var entity: String? = null,
    @Index(name = "ts_count_type")
    @Enumerated
    @Column(name = "count_type")
    var countType: TripleStatisticsType? = null,
    @Index(name = "ts_count")
    @Column(name = "count")
    var count: Int? = null
)