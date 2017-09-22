package ir.ac.iust.dml.kg.access.entities

import ir.ac.iust.dml.kg.knowledge.core.ValueType
import javax.persistence.*

@Entity
@org.hibernate.annotations.Table(appliesTo = "fkg_triple_property")
@Table(name = "fkg_triple_property")
class FkgTripleProperty(
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triple_id", nullable = false)
    var parent: FkgTriple,
    @Column(name = "predicate")
    var predicate: String? = null,
    @Column(name = "object")
    var objekt: String? = null,
    @Column(name = "language")
    var language: String? = null,
    @Column(name = "valueType")
    var valueType: ValueType? = null
)