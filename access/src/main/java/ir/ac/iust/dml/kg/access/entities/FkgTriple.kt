/*
 * Farsi Knowledge Graph Project
 * Iran University of Science and Technology (Year 2017)
 * Developed by Majid Asgari.
 */

package ir.ac.iust.dml.kg.access.entities

import com.google.gson.annotations.Expose
import ir.ac.iust.dml.kg.knowledge.core.ValueType
import org.hibernate.annotations.Index
import javax.persistence.*

@Entity
@org.hibernate.annotations.Table(appliesTo = "fkg_triple",
    indexes = [(Index(
        name = "t_source_predicate_object",
        columnNames = arrayOf("subject", "predicate", "object")
    ))])
@Table(name = "fkg_triple")
class FkgTriple(
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "source")
    @Expose
    var source: String? = null,
    @Column(name = "subject")
    @Expose
    var subject: String? = null,
    @Column(name = "predicate")
    @Expose
    var predicate: String? = null,
    @Column(name = "object")
    @Expose
    var objekt: String? = null,
    @Index(name = "t_module")
    @Column(name = "module")
    @Expose
    var module: String? = null,
    @Index(name = "t_version")
    @Column(name = "version_number")
    @Expose
    var version: Int? = null,
    @Index(name = "t_extraction_time")
    @Column(name = "extraction_time")
    @Expose
    var extractionTime: Long? = null,
    @Column(name = "raw_text")
    @Expose
    var rawText: String? = null,
    @Column(name = "accuracy")
    @Expose
    var accuracy: Double? = null,
    @Column(name = "language")
    @Expose
    var language: String? = null,
    @Column(name = "valueType")
    @Expose
    var valueType: ValueType? = null,
    @Column(name = "dataType")
    @Expose
    var dataType: String? = null,
    @Column(name = "approved")
    @Expose
    var approved: Boolean? = null,
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parent")
    @Expose
    var properties: MutableList<FkgTripleProperty> = mutableListOf()
)