package ir.ac.iust.dml.kg.dbpediahelper.access.entities

import javax.persistence.*

@Entity
@Table(name = "triples_map")
data class TriplesMap(
        @Id
        @Column(name = "id")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,
        @Column var title: String//,
//        @ManyToOne
//        @JoinColumn(name = "logical_source_id", referencedColumnName = "id", nullable = false)
//        @Column var logicalSource: LogicalSource,
//        @ManyToOne
//        @JoinColumn(name = "subject_map_id", referencedColumnName = "id", nullable = false)
//        @Column var subjectMap: SubjectMap
)