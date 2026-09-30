package io.github.composefluent.winrt.compiler.xaml

import io.github.composefluent.winrt.compiler.authoring.IndexedWinRTType
import io.github.composefluent.winrt.compiler.authoring.resolveIndexedWinRTTypeByProjectedName
import io.github.composefluent.winrt.metadata.*
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.isNullable

/** Public property visibility follows CsWinRT WinRTTypeWriter.AddPropertyDeclaration.
 * The temporary schema also describes representable private x:Bind inputs; generated
 * runtime registration filters them out. Component ABI export remains separate.
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
        val collection = winRTCollectionAbiNameForKotlinType(name)
        val metadataName = when {
            primitive != null -> primitive.toKotlinProjectionTypeName()
            name == "kotlin.Any" -> "System.Object"
            name in applicationTypes -> name
            collection != null -> "$collection`${arguments.size}"
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

    val properties = klass.declarations.filterIsInstance<IrProperty>()
        .filter { it.origin == IrDeclarationOrigin.DEFINED && it.getter != null &&
            it.getter?.dispatchReceiverParameter != null &&
            it.getter!!.parameters.none { parameter ->
                parameter.kind == IrParameterKind.ExtensionReceiver || parameter.kind == IrParameterKind.Context
            } }
        .sortedBy { it.name.asString() }
        .mapNotNull { property ->
            val public = visible(property.getter)
            val type = if (public) resolve(property.getter!!.returnType) else
                runCatching { resolve(property.getter!!.returnType) }.getOrNull() ?: return@mapNotNull null
            WinRTXamlApplicationProperty(property.name.asString(), type,
                isReadOnly = property.setter == null || (public && !visible(property.setter)), isPublic = public)
        }
    val events = klass.declarations.filterIsInstance<IrSimpleFunction>()
        .filter { visible(it) && it.overriddenSymbols.isEmpty() && it.name.asString().startsWith("add") &&
            it.name.asString().length > 3 && it.name.asString()[3].isUpperCase() }
        .mapNotNull { add ->
            val name = add.name.asString().removePrefix("add")
            val parameter = add.parameters.singleOrNull { it.kind == IrParameterKind.Regular } ?: return@mapNotNull null
            val remove = klass.declarations.filterIsInstance<IrSimpleFunction>().singleOrNull {
                visible(it) && it.name.asString() == "remove$name" &&
                    it.parameters.singleOrNull { p -> p.kind == IrParameterKind.Regular }?.type == parameter.type
            } ?: return@mapNotNull null
            WinRTXamlApplicationEvent(name, resolve(parameter.type))
        }
    return WinRTXamlApplicationTypeMembers(properties = properties, events = events)
}
