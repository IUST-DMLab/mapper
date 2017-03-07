package ir.ac.iust.dml.kg.dbpediahelper.access.entities

import org.hibernate.annotations.Index
import javax.persistence.*

@Entity
@Table(name = "triple_stats")
data class TripleStatistics(
      @Id
      @Column(name = "id")
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      var id: Long? = null,
      @Index(name = "ts_type")
      @Column(name = "template_type")
      var templateType: String? = null,
      @Index(name = "ts_property")
      @Column(name = "property")
      var property: String? = null,
      @Index(name = "ts_entity")
      @Column(name = "entity")
      var entity: String? = null,
      @Index(name = "ts_status")
      @Enumerated
      @Column(name = "count_type")
      var countType: TripleStatisticsType? = null,
      @Column(name = "count")
      var count: Int? = null
)