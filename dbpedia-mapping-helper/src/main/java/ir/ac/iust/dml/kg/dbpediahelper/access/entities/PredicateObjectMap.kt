package ir.ac.iust.dml.kg.dbpediahelper.access.entities

import javax.persistence.*

@Entity
@Table(name = "subject_map")
data class PredicateObjectMap(
        @Id
        @Column(name = "id")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null
)