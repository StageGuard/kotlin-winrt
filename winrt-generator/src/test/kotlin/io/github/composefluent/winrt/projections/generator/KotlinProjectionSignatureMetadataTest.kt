package io.github.composefluent.winrt.projections.generator

import io.github.composefluent.winrt.metadata.*
import io.github.composefluent.winrt.runtime.Guid
import org.junit.Assert.assertTrue
import org.junit.Test

class KotlinProjectionSignatureMetadataTest {
    @Test
    fun runtime_class_declaration_carries_its_closed_winmd_signature() {
        // CsWinRT GuidGenerator.GetSignature uses the declared runtime class and default IID
        // when constructing a closed generic delegate IID. This fact must travel with the type.
        val iid = "11111111-2222-3333-4444-555555555555"
        val model = WinRTMetadataModel(namespaces = listOf(WinRTNamespace("Sample", listOf(
            WinRTTypeDefinition(namespace = "Sample", name = "IWidget", kind = WinRTTypeKind.Interface,
                iid = Guid(iid)),
            WinRTTypeDefinition(namespace = "Sample", name = "Widget", kind = WinRTTypeKind.RuntimeClass,
                defaultInterfaceName = "Sample.IWidget",
                implementedInterfaces = listOf(WinRTInterfaceImplementationDefinition("Sample.IWidget", isDefault = true))),
        ))))
        val files = KotlinProjectionGenerator().generate(model).associateBy { it.relativePath.substringAfterLast('/') }
        val source = files.getValue("Widget.kt").contents
        assertTrue(source, source.contains("@WindowsRuntimeType(guidSignature ="))
        assertTrue(source, source.contains("\"rc(Sample.Widget;{$iid})\""))
    }
}
