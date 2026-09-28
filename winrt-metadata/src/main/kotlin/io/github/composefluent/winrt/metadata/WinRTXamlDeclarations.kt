package io.github.composefluent.winrt.metadata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

/** Versioned view of XamlCompiler's DOM/harvester, before Kotlin semantic analysis. */
@Serializable
data class WinRTXamlDeclarationIndex(
    @SerialName("SchemaVersion") val schemaVersion: Int,
    @SerialName("Pages") val pages: List<WinRTXamlPageDeclaration>,
    @SerialName("Resources") val resources: List<String>,
)

@Serializable
data class WinRTXamlPageDeclaration(
    @SerialName("ClassName") val className: String,
    @SerialName("ResourcePath") val resourcePath: String,
    @SerialName("BaseTypeName") val baseTypeName: String,
    @SerialName("IsApplication") val isApplication: Boolean,
    @SerialName("Features") val features: List<String>,
    @SerialName("Connections") val connections: List<WinRTXamlConnectionDeclaration>,
)

@Serializable
data class WinRTXamlConnectionDeclaration(
    @SerialName("Id") val id: Int,
    @SerialName("TypeName") val typeName: String,
    @SerialName("FieldName") val fieldName: String?,
    @SerialName("Location") val location: WinRTXamlSourceLocation,
    @SerialName("Events") val events: List<WinRTXamlEventDeclaration>,
)

@Serializable
data class WinRTXamlEventDeclaration(
    @SerialName("Name") val name: String,
    @SerialName("HandlerName") val handlerName: String,
    @SerialName("DeclaringTypeName") val declaringTypeName: String,
    @SerialName("DelegateTypeName") val delegateTypeName: String,
    @SerialName("Location") val location: WinRTXamlSourceLocation,
)

@Serializable
data class WinRTXamlSourceLocation(
    @SerialName("Line") val line: Int,
    @SerialName("Column") val column: Int,
)

object WinRTXamlDeclarations {
    const val SCHEMA_VERSION = 1
    private val json = Json { encodeDefaults = true }
    private val supportedFeatures = setOf("named-elements", "events")

    /** Never accept a partial/stale index from a compiler invocation that reported an error. */
    fun readCompilerOutput(path: Path): WinRTXamlDeclarationIndex {
        val output = json.parseToJsonElement(Files.readString(path)).jsonObject
        val errors = output["MSBuildLogEntries"]?.jsonArray.orEmpty().filter {
            it.jsonObject["Type"]?.jsonPrimitive?.intOrNull == 2
        }
        require(errors.isEmpty()) { "XamlCompiler failed: " + errors.joinToString("; ") {
            it.jsonObject["Message"]?.jsonPrimitive?.content.orEmpty()
        } }
        val index = output["KotlinDeclarations"]
        require(index != null && index != JsonNull) { "XamlCompiler did not produce Kotlin declarations: $path" }
        return parse(index.toString())
    }

    fun parse(text: String): WinRTXamlDeclarationIndex {
        val element = json.parseToJsonElement(text)
        require(element.jsonObject["SchemaVersion"]?.jsonPrimitive?.intOrNull == SCHEMA_VERSION) {
            "Unsupported Kotlin XAML declaration schema; expected $SCHEMA_VERSION."
        }
        return json.decodeFromJsonElement<WinRTXamlDeclarationIndex>(element).also(::validate)
    }

    fun canonicalText(index: WinRTXamlDeclarationIndex): String {
        validate(index)
        return json.encodeToString(index.copy(
            resources = index.resources.sorted(),
            pages = index.pages.sortedBy { it.className }.map { page -> page.copy(
                features = page.features.sorted(),
                connections = page.connections.sortedBy { it.id }.map { connection -> connection.copy(
                    events = connection.events.sortedBy { it.name },
                ) },
            ) },
        ))
    }

    fun fingerprint(index: WinRTXamlDeclarationIndex): String = MessageDigest.getInstance("SHA-256")
        .digest(canonicalText(index).toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    private fun validate(index: WinRTXamlDeclarationIndex) {
        require(index.schemaVersion == SCHEMA_VERSION) { "Unsupported Kotlin XAML schema ${index.schemaVersion}." }
        require(index.pages.map { it.className }.distinct().size == index.pages.size) { "Duplicate x:Class." }
        val paths = index.resources + index.pages.map { it.resourcePath }
        paths.forEach(::validateResourcePath)
        require(paths.map { it.lowercase(java.util.Locale.ROOT) }.distinct().size == paths.size) { "Duplicate XAML resource path." }
        for (page in index.pages) {
            require(page.className.isNotBlank() && page.baseTypeName.isNotBlank()) { "Missing XAML class or base type." }
            require(page.features.all { it in supportedFeatures }) { "Unsupported Kotlin XAML features: ${page.features - supportedFeatures}" }
            require(page.connections.map { it.id }.distinct().size == page.connections.size) { "Duplicate connection ID in ${page.resourcePath}." }
            val fields = page.connections.mapNotNull { it.fieldName }
            require(fields.distinct().size == fields.size) { "Duplicate x:Name in ${page.resourcePath}." }
            require(fields.isEmpty() || "named-elements" in page.features) { "Missing named-elements feature." }
            require(page.connections.all { it.events.isEmpty() } || "events" in page.features) { "Missing events feature." }
            for (connection in page.connections) {
                require(connection.id > 0 && connection.typeName.isNotBlank()) { "Invalid XAML connection in ${page.resourcePath}." }
                require(connection.fieldName == null || connection.fieldName.isNotBlank()) { "Empty x:Name." }
                validateLocation(connection.location)
                require(connection.events.map { it.name }.distinct().size == connection.events.size) { "Duplicate XAML event." }
                for (event in connection.events) {
                    require(listOf(event.name, event.handlerName, event.declaringTypeName, event.delegateTypeName).all(String::isNotBlank)) { "Incomplete XAML event in ${page.resourcePath}." }
                    validateLocation(event.location)
                }
            }
        }
    }

    private fun validateLocation(location: WinRTXamlSourceLocation) {
        require(location.line > 0 && location.column > 0) { "Missing XAML source location." }
    }

    private fun validateResourcePath(path: String) {
        require(path.isNotBlank() && !path.startsWith('/') && ':' !in path && '\\' !in path &&
            path.split('/').none { it.isEmpty() || it == "." || it == ".." }) { "Invalid relative XAML resource path: $path" }
    }
}
