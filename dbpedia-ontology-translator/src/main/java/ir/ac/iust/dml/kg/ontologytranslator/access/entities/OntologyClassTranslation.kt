package ir.ac.iust.dml.kg.ontologytranslator.access.entities

import org.hibernate.annotations.Index
import javax.persistence.*

@Entity
@Table(name = "ontology_classes_translation")
data class OntologyClassTranslation(
      @Id
      @Column(name = "id")
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      var id: Long? = null,
      @Index(name = "oct_class_name")
      @Column(name = "class_name")
      var name: String? = null,
      @Column(name = "en_label")
      var enLabel: String? = null,
      @Index(name = "oct_parent_id")
      @Column(name = "parent_id")
      var parentId: Long? = null,
      @Column(name = "fa_label")
      var faLabel: String? = null,
      @Column(name = "fa_other_labels")
      var faOtherLabels: String? = null,
      @Column(name = "note", length = 5000)
      var note: String? = null,
      @Column(name = "approved")
      var approved: Boolean? = null,
      @Column(name = "comment", length = 5000)
      var comment: String? = null)