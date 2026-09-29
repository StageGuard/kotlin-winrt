package io.github.composefluent.winrt.compiler.xaml

import io.github.composefluent.winrt.compiler.authoring.IndexedWinRTType
import io.github.composefluent.winrt.compiler.authoring.resolveIndexedWinRTTypeByProjectedName
import io.github.composefluent.winrt.metadata.*
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.isNullable

/** Application schema follows CsWinRT WinRTTypeWriter.AddPropertyDeclaration's accessor visibility.
 * It is deliberately separate from component ABI export and generated x:Name fields.
 */
@OptIn(UnsafeDuringIrConstructionAPI::class)
internal fun xamlApplicationProperties(
    klass: IrClass,
    types: Map<String, IndexedWinRTType>,
    applicationTypes: Set<String>,
): WinRTXamlApplicationTypeMembers {
    fun visible(function: IrSimpleFunction?) = function != null &&
        function.visibility in setOf(DescriptorVisibilities.PUBLIC, DescriptorVisibilities.INTERNAL)

    fun resolve(type: IrType): WinRTTypeRef {
        val name = requireNotNull(type.classFqName?.asString()) { "XAML property requires a concrete type: $type" }
        val arguments = (type as? IrSimpleType)?.arguments.orEmpty().map {
            resolve(requireNotNull(it.typeOrNull) { "XAML property cannot use a star-projected type: $type" })
        }
        if (name == "kotlin.Array") return WinRTTypeRef.array(arguments.single())
        val primitive = winRTFundamentalTypeForName(name.removePrefix("kotlin."))
        val indexed = resolveIndexedWinRTTypeByProjectedName(name, types)
        val metadataName = when {
            primitive != null -> primitive.toKotlinProjectionTypeName()
            name == "kotlin.Any" -> "System.Object"
            name in applicationTypes -> name
            indexed != null -> indexed.qualifiedName.substringBefore('`') +
                if (arguments.isEmpty()) "" else "`${arguments.size}"
            else -> error("XAML property type $name has no WinRT metadata projection")
        }
        val result = WinRTTypeRef.named(metadataName, arguments)
        val isValueType = primitive?.isWinRTValueType == true ||
            indexed?.kind in setOf(WinRTTypeKind.Enum.name, WinRTTypeKind.Struct.name)
        return if (type.isNullable() && isValueType)
            WinRTTypeRef.named("Windows.Foundation.IReference`1", listOf(result)) else result
    }

    return WinRTXamlApplicationTypeMembers(properties = klass.declarations.filterIsInstance<IrProperty>()
        .filter { it.origin == IrDeclarationOrigin.DEFINED && visible(it.getter) &&
            it.getter?.dispatchReceiverParameter != null &&
            it.getter!!.parameters.none { parameter ->
                parameter.kind == IrParameterKind.ExtensionReceiver || parameter.kind == IrParameterKind.Context
            } }
        .sortedBy { it.name.asString() }
        .map { property ->
            WinRTXamlApplicationProperty(property.name.asString(), resolve(property.getter!!.returnType),
                isReadOnly = !visible(property.setter))
        })
}
