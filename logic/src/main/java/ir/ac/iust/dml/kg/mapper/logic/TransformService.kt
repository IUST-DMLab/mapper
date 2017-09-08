package ir.ac.iust.dml.kg.mapper.logic

import ir.ac.iust.dml.kg.knowledge.core.TypedValue
import ir.ac.iust.dml.kg.knowledge.core.transforms.TransformScanner
import ir.ac.iust.dml.kg.knowledge.core.transforms.Transformer
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class TransformService {
  private val scanner = TransformScanner()

  @PostConstruct
  fun init() {
    scanner.scan("ir.ac.iust.dml.kg.knowledge.core.transforms.impl")
  }

  fun getTransformNames(): MutableSet<String> = scanner.availableTransformer!!

  data class TransformAndDescription(var transform: String, var label: String)

  fun getTransforms(): MutableList<TransformAndDescription> {
    val all = scanner.availableTransformer
    val result = mutableListOf<TransformAndDescription>()
    for (transformName in all) {
      val transform = scanner.getTransformer(transformName)
      val annotation = transform.javaClass.getDeclaredAnnotation(Transformer::class.java)
      result.add(TransformAndDescription(transformName, annotation.description))
    }
    return result
  }

  fun transform(transformer: String, value: String, lang: String): TypedValue {
    val t = scanner.getTransformer(transformer)
    return t.transform(value, lang, null, null)
  }
}