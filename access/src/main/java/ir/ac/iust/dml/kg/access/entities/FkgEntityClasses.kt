package ir.ac.iust.dml.kg.access.entities

import ir.ac.iust.dml.kg.access.entities.enumerations.MappingStatus
import org.hibernate.annotations.Index
import javax.persistence.*

@Entity
@Table(name = "fkg_entity_classes")
data class FkgEntityClasses(
        @Id
        @Column(name = "id")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,
        @Index(name = "dec_entity")
        @Column(name = "entity")
        var entity: String? = null,
        @Index(name = "dec_class_name")
        @Column(name = "class_name")
        var className: String? = null,
        @Column(name = "class_tree")
        var classTree: String? = null,
        @Index(name = "dbpm_status")
        @Enumerated
        @Column(name = "status")
        var status: MappingStatus? = null,
        @Column(name = "approved")
        var approved: Boolean? = null,
        @Index(name = "dbpm_update_epoch")
        @Column(name = "update_epoch")
        var updateEpoch: Long? = null)