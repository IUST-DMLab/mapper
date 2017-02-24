package ir.ac.iust.dml.kg.dbpediahelper.access.entities

import org.hibernate.annotations.Index
import javax.persistence.*

@Entity
@Table(name = "dbpedia_property_mapping")
data class DBpediaPropertyMapping(
      @Id
      @Column(name = "id")
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      var id: Long? = null,
      @Index(name = "dbpm_language")
      @Column(name = "language")
      var language: String? = null,
      @Index(name = "dbpm_type")
      @Column(name = "type")
      var type: String? = null,
      @Index(name = "dbpm_class")
      @Column(name = "class")
      var clazz: String? = null,
      @Index(name = "dbpm_template_property")
      @Column(name = "template_property")
      var templateProperty: String? = null,
      @Index(name = "dbpm_ontology_property")
      @Column(name = "ontology_property")
      var ontologyProperty: String? = null,
      @Index(name = "dbpm_status")
      @Enumerated
      @Column(name = "status")
      var status: MappingStatus? = null)