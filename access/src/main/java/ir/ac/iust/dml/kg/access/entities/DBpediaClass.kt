package ir.ac.iust.dml.kg.access.entities

import org.hibernate.annotations.Index
import javax.persistence.*

@Entity
@Table(name = "dbpedia_ontology_classes")
data class DBpediaClass(
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Index(name = "class_name")
    @Column(name = "class_name")
    var name: String? = null,
    @Column(name = "en_label")
    var enLabel: String? = null,
    @Index(name = "dboc_parent_id")
    @Column(name = "parent_id")
    var parentId: Long? = null,
    @Column(name = "comment")
    var comment: String? = null)