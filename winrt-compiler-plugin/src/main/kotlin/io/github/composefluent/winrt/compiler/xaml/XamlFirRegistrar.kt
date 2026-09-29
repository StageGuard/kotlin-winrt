package io.github.composefluent.winrt.compiler.xaml

import io.github.composefluent.winrt.metadata.*
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirClassLikeDeclaration
import org.jetbrains.kotlin.fir.extensions.*
import org.jetbrains.kotlin.fir.plugin.*
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.name.*

internal object XamlDeclarationKey : GeneratedDeclarationKey()
internal val xamlConnectorId = ClassId.topLevel(FqName("microsoft.ui.xaml.markup.IComponentConnector"))
internal val xamlStateId = ClassId.topLevel(FqName("io.github.composefluent.winrt.runtime.WinRTXamlLoadState"))
internal val xamlComponentId = ClassId.topLevel(FqName("io.github.composefluent.winrt.runtime.WinRTXamlComponent"))
internal val xamlStateName = Name.identifier("_kotlinXamlState")
internal val xamlConstructionStateName = Name.identifier("_kotlinXamlConstructionState")
internal val xamlConstructionName = Name.identifier("_kotlinXamlCompleteConstruction")
internal val xamlLoadName = Name.identifier("_kotlinXamlLoad")
internal val xamlInitializeName = Name.identifier("_kotlinXamlInitialize")
internal val xamlConnectName = Name.identifier("connect")
internal val xamlBindingName = Name.identifier("getBindingConnector")

// Same namespace casing contract as KotlinProjectionTypeResolver.projectionClassNameForQualifiedName.
internal fun xamlProjectionClassId(name: String): ClassId = ClassId(
    FqName(name.substringBeforeLast('.', "").lowercase()), Name.identifier(name.substringAfterLast('.')),
)

internal class XamlFirRegistrar(index: WinRTXamlDeclarationIndex) : FirExtensionRegistrar() {
    private val pages = index.pages.associateBy { ClassId.topLevel(FqName(it.className)) }
    override fun ExtensionRegistrarContext.configurePlugin() {
        +FirDeclarationGenerationExtension.Factory { XamlDeclarations(it, pages) }
        +FirSupertypeGenerationExtension.Factory { XamlSupertypes(it, pages.keys) }
    }
}

private class XamlDeclarations(session: FirSession, private val pages: Map<ClassId, WinRTXamlPageDeclaration>) :
    FirDeclarationGenerationExtension(session) {
    override fun getCallableNamesForClass(classSymbol: FirClassSymbol<*>, context: MemberGenerationContext): Set<Name> {
        val page = pages[classSymbol.classId] ?: return emptySet()
        return page.connections.mapNotNull { it.fieldName?.let(Name::identifier) }.toSet() +
            setOf(xamlStateName, xamlConstructionStateName, xamlConstructionName, xamlLoadName, xamlInitializeName, xamlConnectName, xamlBindingName)
    }

    override fun generateProperties(callableId: CallableId, context: MemberGenerationContext?): List<FirPropertySymbol> {
        val owner = context?.owner ?: return emptyList()
        val page = pages[owner.classId] ?: return emptyList()
        val name = callableId.callableName
        val element = page.connections.singleOrNull { it.fieldName == name.asString() }
        val state = name == xamlStateName || name == xamlConstructionStateName
        val type = if (state) xamlStateId else element?.typeName?.let(::xamlProjectionClassId) ?: return emptyList()
        return listOf(createMemberProperty(owner, XamlDeclarationKey, name, type.createConeType(session)) {
            if (state) visibility = Visibilities.Private
        }.symbol)
    }

    override fun generateFunctions(callableId: CallableId, context: MemberGenerationContext?): List<FirNamedFunctionSymbol> {
        val owner = context?.owner ?: return emptyList()
        if (owner.classId !in pages) return emptyList()
        val name = callableId.callableName
        if (name !in setOf(xamlLoadName, xamlInitializeName, xamlConstructionName, xamlConnectName, xamlBindingName)) return emptyList()
        val result = if (name == xamlBindingName) xamlConnectorId.createConeType(session, nullable = true)
            else session.builtinTypes.unitType.coneType
        return listOf(createMemberFunction(owner, XamlDeclarationKey, name, result) {
            if (name == xamlLoadName) visibility = Visibilities.Private
            if (name == xamlInitializeName || name == xamlConstructionName) status { isOverride = true }
            if (name == xamlConnectName || name == xamlBindingName) {
                status { isOverride = true }
                valueParameter(Name.identifier("connectionId"), session.builtinTypes.intType.coneType)
                valueParameter(Name.identifier("target"), session.builtinTypes.nullableAnyType.coneType)
            }
        }.symbol)
    }
}

private class XamlSupertypes(session: FirSession, private val pages: Set<ClassId>) : FirSupertypeGenerationExtension(session) {
    override fun needTransformSupertypes(declaration: FirClassLikeDeclaration) = declaration.symbol.classId in pages
    override fun computeAdditionalSupertypes(classLikeDeclaration: FirClassLikeDeclaration,
        resolvedSupertypes: List<FirResolvedTypeRef>, typeResolver: TypeResolveService): List<ConeKotlinType> =
        listOf(xamlConnectorId, xamlComponentId)
            .filterNot { id -> resolvedSupertypes.any { it.coneType.classId == id } }
            .map { it.createConeType(session) }
}
