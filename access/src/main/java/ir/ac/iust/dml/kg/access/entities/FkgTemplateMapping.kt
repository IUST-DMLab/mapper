package ir.ac.iust.dml.kg.access.entities

import org.hibernate.annotations.Index
import javax.persistence.*

@Entity
@Table(name = "fkg_mapping_template_to_class")
data class FkgTemplateMapping(
      @Id
      @Column(name = "id")
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      var id: Long? = null,
      @Index(name = "ttc_template_name")
      @Column(name = "template_name")
      var templateName: String? = null,
      @Index(name = "ttc_class_name")
      @Column(name = "class_name")
      var ontologyClass: String? = null,
      @Index(name = "ttc_language")
      @Column(name = "language")
      var language: String? = null,
      @Column(name = "approved")
      var approved: Boolean? = null,
      @Index(name = "ttc_update_epoch")
      @Column(name = "update_epoch")
      var updateEpoch: Long? = null)