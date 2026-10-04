package morningentree.morphe.codegen.pairip

import kotlinx.serialization.Serializable

@Serializable
data class JavaField(val name: String, val value: String)

@Serializable
data class JavaCodegenData(
    val strings: Map<String, List<JavaField>>,
    val methods: Map<String, List<JavaField>>
)
