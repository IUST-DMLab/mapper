package ir.ac.iust.dml.kg.dbpediahelper.access.entities

import org.hibernate.annotations.Index
import javax.persistence.*

@Entity
@Table(name = "wiki_template_mapping_from_redirects")
data class WikipediaTemplateRedirect(
      @Id
      @Column(name = "id")
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      var id: Long? = null,
      @Index(name = "wetm_tfnf")
      @Column(name = "template_full_name_fa")
      var fullNameFa: String? = null,
      @Index(name = "wetm_tfne")
      @Column(name = "template_full_name_en")
      var fullNameEn: String? = null,
      @Index(name = "wetm_tnf")
      @Column(name = "template_name_fa")
      var nameFa: String? = null,
      @Index(name = "wetm_tne")
      @Column(name = "template_name_en")
      var nameEn: String? = null,
      @Index(name = "wetm_ttf")
      @Column(name = "template_type_fa")
      var typeFa: String? = null,
      @Index(name = "wetm_tte")
      @Column(name = "template_type_en")
      var typeEn: String? = null)