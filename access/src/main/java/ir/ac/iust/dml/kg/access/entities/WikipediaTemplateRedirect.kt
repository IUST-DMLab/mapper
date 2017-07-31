package ir.ac.iust.dml.kg.access.entities

import javax.persistence.*

@Entity
@Table(name = "wiki_template_mapping")
data class WikipediaTemplateRedirect(
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    //      @Index(name = "wetm_tnf")
    @Column(name = "template_name_fa")
    var nameFa: String? = null,
    //      @Index(name = "wetm_tne")
    @Column(name = "template_name_en")
    var nameEn: String? = null)