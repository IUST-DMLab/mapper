package ir.ac.iust.dml.kg.dbpediahelper.access.entities

import javax.persistence.*

@Entity
@Table(name = "logical_source")
data class LogicalSource(
        @Id
        @Column(name = "id")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,
        @Column var address: String? = null,
        @Enumerated @Column var iterator: LogicalSourceIterator,
        @Enumerated @Column var referenceFormulation: ReferenceFormulation)