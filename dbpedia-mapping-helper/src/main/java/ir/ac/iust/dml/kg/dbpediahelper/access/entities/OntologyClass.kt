package ir.ac.iust.dml.kg.dbpediahelper.access.entities

import javax.persistence.*

@Entity
@Table(name = "ontology_class")
data class OntologyClass(
        @Id
        @Column(name = "id")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,
        @Column var title: String
)