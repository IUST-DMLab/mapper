package ir.ac.iust.dml.kg.dbpediahelper.access.entities

import org.hibernate.annotations.Index
import javax.persistence.*

@Entity
@Table(name = "triple")
class KnowledgeBaseTriple(
      @Id
      @Column(name = "id")
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      var id: Long? = null,
      //      @Index(name = "t_source")
      @Column(name = "source")
      var source: String? = null,
      //      @Index(name = "t_subject")
      @Column(name = "subject")
      var subject: String? = null,
      //      @Index(name = "t_predicate")
      @Column(name = "predicate")
      var predicate: String? = null,
      //      @Index(name = "t_object")
      @Column(name = "object")
      var objekt: String? = null,
      @Column(name = "template_type")
      var templateType: String? = null,
      @Column(name = "raw_property")
      var rawProperty: String? = null,
      @Index(name = "t_status")
      @Enumerated
      @Column(name = "status")
      var status: MappingStatus? = null,
      //      @Index(name = "t_language")
      @Column(name = "language")
      var language: String? = null
)