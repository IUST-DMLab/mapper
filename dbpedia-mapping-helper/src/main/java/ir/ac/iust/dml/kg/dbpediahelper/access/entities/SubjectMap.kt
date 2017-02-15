package ir.ac.iust.dml.kg.dbpediahelper.access.entities

import javax.persistence.*

@Entity
@Table(name = "subject_map")
data class SubjectMap(
        @Id
        @Column(name = "id")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,
        @Column var constant: String? = null,
        @ManyToOne
        @JoinColumn(name = "ontology_class_id", referencedColumnName = "id", nullable = false)
        @Column var ontologyClass: OntologyClass,
        @Enumerated @Column var termType: SubjectMapTermType
)