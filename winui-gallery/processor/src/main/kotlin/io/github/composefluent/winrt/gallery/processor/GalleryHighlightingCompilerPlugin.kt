@file:OptIn(org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi::class,
    org.jetbrains.kotlin.fir.symbols.SymbolInternals::class,
    org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI::class)

package io.github.composefluent.winrt.gallery.processor

import io.github.composefluent.winrt.gallery.code.KotlinCodeKind
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.KtRealSourceElementKind
import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.*
import org.jetbrains.kotlin.fir.analysis.checkers.expression.*
import org.jetbrains.kotlin.fir.analysis.checkers.type.*
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrGetEnumValueImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.com.intellij.openapi.util.Ref
import org.jetbrains.kotlin.com.intellij.lang.LighterASTNode
import java.io.File

/** Uses the *actual* JVM/Native compilation's resolved FIR, before WinRT call-site lowering.
 * No secondary compiler, classpath approximation, or runtime analyzer is involved.
 * Categories follow IDEA 262's KotlinHighlightingColors and semantic analyzers.
 */
class GalleryHighlightingCompilerPlugin : CompilerPluginRegistrar() {
    override val pluginId = "io.github.composefluent.gallery.semantic-highlighting"
    override val supportsK2 = true
    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val index = SemanticHighlightIndex()
        FirExtensionRegistrarAdapter.registerExtension(object : FirExtensionRegistrar() {
            override fun ExtensionRegistrarContext.configurePlugin() {
                +{ session: FirSession -> GalleryHighlightCheckers(session, index) }
            }
        })
        IrGenerationExtension.registerExtension(GalleryHighlightIr(index))
    }
}

internal class SemanticHighlightIndex {
    private val files = mutableMapOf<String, MutableMap<Pair<Int, Int>, KotlinCodeKind>>()
    private val paths = mutableMapOf<String, String>()
    private fun path(value: String) = paths.getOrPut(value) { File(value).canonicalPath.replace('\\', '/') }
    fun record(file: String, start: Int, end: Int, kind: KotlinCodeKind) {
        files.getOrPut(path(file)) { mutableMapOf() }[start to end] = kind
    }
    fun file(file: String): Map<Pair<Int, Int>, KotlinCodeKind> {
        val normalized = file.replace('\\', '/')
        // Generated source maps use repository-relative paths for reproducibility.
        val matches = if (File(file).isAbsolute) listOfNotNull(files[path(file)]) else
            files.filterKeys { it.endsWith("/$normalized") }.values.toList()
        return checkNotNull(matches.singleOrNull()) {
            "Gallery semantic highlighting requires exactly one original source in this compilation: $file"
        }
    }
}

private fun symbolKind(symbol: FirBasedSymbol<*>, declaration: Boolean = false): KotlinCodeKind? = when (symbol) {
    is FirValueParameterSymbol -> KotlinCodeKind.Parameter
    is FirEnumEntrySymbol -> KotlinCodeKind.StaticProperty
    is FirPropertySymbol -> when {
        symbol.fir.isLocal -> KotlinCodeKind.Variable
        symbol is FirSyntheticPropertySymbol || symbol.fir.receiverParameter != null || symbol.callableId?.classId == null -> KotlinCodeKind.StaticProperty
        else -> KotlinCodeKind.Property
    }
    is FirBackingFieldSymbol -> KotlinCodeKind.Variable
    is FirFieldSymbol -> if (symbol.fir.status.isStatic) KotlinCodeKind.StaticProperty else KotlinCodeKind.Property
    is FirConstructorSymbol -> KotlinCodeKind.Constructor
    is FirNamedFunctionSymbol -> when {
        declaration -> KotlinCodeKind.Function
        symbol.fir.status.isSuspend -> KotlinCodeKind.FunctionCall
        symbol.isLocal -> KotlinCodeKind.FunctionCall
        symbol.callableId.asSingleFqName().asString() == "kotlin.suspend" -> KotlinCodeKind.Keyword
        symbol.fir.receiverParameter != null || symbol.callableId.classId == null -> KotlinCodeKind.StaticFunctionCall
        else -> KotlinCodeKind.FunctionCall
    }
    is FirTypeParameterSymbol -> KotlinCodeKind.TypeParameter
    is FirRegularClassSymbol -> if (symbol.fir.classKind == ClassKind.ANNOTATION_CLASS) KotlinCodeKind.Annotation else KotlinCodeKind.Type
    is FirClassLikeSymbol<*> -> KotlinCodeKind.Type
    else -> null
}

private class GalleryHighlightCheckers(session: FirSession, private val index: SemanticHighlightIndex) : FirAdditionalCheckersExtension(session) {
    context(context: CheckerContext)
    private fun recordTypeName(source: KtSourceElement?, name: String, kind: KotlinCodeKind) {
        if (source == null) return
        // K2 wraps authored property references and implicit-invoke receivers in
        // these source kinds. Other synthetic expressions must not recolor source.
        if (source.kind != KtRealSourceElementKind &&
            source.kind != KtFakeSourceElementKind.ReferenceInAtomicQualifiedAccess &&
            source.kind != KtFakeSourceElementKind.ImplicitInvokeCall) return
        val file = context.containingFile?.path ?: return
        val tree = source.treeStructure
        fun visit(node: LighterASTNode) {
            if (node.tokenType == KtTokens.IDENTIFIER && tree.toString(node).toString().trim('`') == name) {
                index.record(file, tree.getStartOffset(node), tree.getEndOffset(node), kind)
                return
            }
            val ref = Ref<Array<LighterASTNode>>()
            val count = tree.getChildren(node, ref)
            val children = ref.get() ?: return
            try { children.take(count).forEach(::visit) } finally { tree.disposeChildren(children, count) }
        }
        visit(source.lighterASTNode)
    }

    override val declarationCheckers = object : DeclarationCheckers() {
        override val basicDeclarationCheckers = setOf(object : FirDeclarationChecker<FirDeclaration>(MppCheckerKind.Common) {
            context(context: CheckerContext, reporter: DiagnosticReporter)
            override fun check(declaration: FirDeclaration) {
                val source = declaration.source ?: return
                val kind = symbolKind(declaration.symbol, declaration = true) ?: return
                if (source.kind != KtRealSourceElementKind) return
                val file = context.containingFile?.path ?: return
                // Declaration source includes its body; only its immediate name token is colored.
                val ref = Ref<Array<LighterASTNode>>()
                val tree = source.treeStructure
                val count = tree.getChildren(source.lighterASTNode, ref)
                val children = ref.get() ?: return
                try {
                    val parts = children.take(count)
                    val declarationKind = if (declaration is FirValueParameter && parts.any {
                        it.tokenType == KtTokens.VAL_KEYWORD || it.tokenType == KtTokens.VAR_KEYWORD
                    }) KotlinCodeKind.Property else kind
                    parts.firstOrNull { it.tokenType == KtTokens.IDENTIFIER }?.let {
                        index.record(file, tree.getStartOffset(it), tree.getEndOffset(it), declarationKind)
                    }
                } finally { tree.disposeChildren(children, count) }
            }
        })
    }

    override val expressionCheckers = object : ExpressionCheckers() {
        override val basicExpressionCheckers = setOf(object : FirExpressionChecker<FirStatement>(MppCheckerKind.Common) {
            context(context: CheckerContext, reporter: DiagnosticReporter)
            override fun check(expression: FirStatement) {
                val access = (expression as? FirQualifiedAccessExpression)
                    ?: ((expression as? FirVariableAssignment)?.lValue as? FirQualifiedAccessExpression)
                val reference = access?.calleeReference as? FirResolvedNamedReference
                if (reference != null && expression !is FirImplicitInvokeCall) {
                    symbolKind(reference.resolvedSymbol)?.let { kind ->
                        recordTypeName(reference.source, reference.name.asString(), kind)
                    }
                }
                if (expression is FirResolvedQualifier) expression.symbol?.let {
                    recordTypeName(expression.source, it.name.asString(), symbolKind(it) ?: KotlinCodeKind.Type)
                }
            }
        })
    }

    override val typeCheckers = object : TypeCheckers() {
        override val resolvedTypeRefCheckers = setOf(object : FirTypeChecker<FirResolvedTypeRef>(MppCheckerKind.Common) {
            context(context: CheckerContext, reporter: DiagnosticReporter)
            override fun check(typeRef: FirResolvedTypeRef) {
                val type = typeRef.coneType as? ConeTypeParameterType ?: return
                recordTypeName(typeRef.source, type.lookupTag.typeParameterSymbol.name.asString(), KotlinCodeKind.TypeParameter)
            }
        })
    }
}

private class GalleryHighlightIr(private val index: SemanticHighlightIndex) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.acceptChildrenVoid(object : IrVisitorVoid() {
            override fun visitElement(element: IrElement) = element.acceptChildrenVoid(this)
            override fun visitClass(declaration: IrClass) {
                val origin = declaration.annotations.firstOrNull {
                    it.symbol.owner.parentAsClass.fqNameWhenAvailable?.asString() ==
                        "io.github.composefluent.winrt.gallery.code.KotlinCodeOrigin"
                }
                if (origin == null) { super.visitClass(declaration); return }
                val file = (origin.arguments[0] as IrConst).value as String
                val offsets = (origin.arguments[1] as IrVararg).elements.map { (it as IrConst).value as Int }
                val original = index.file(file)
                val kinds = offsets.chunked(3).mapNotNull { (end, start, originalEnd) ->
                    original[start to originalEnd]?.let { end to it }
                }.toMap()
                declaration.acceptChildrenVoid(object : IrVisitorVoid() {
                    override fun visitElement(element: IrElement) = element.acceptChildrenVoid(this)
                    override fun visitConstructorCall(expression: IrConstructorCall) {
                        super.visitConstructorCall(expression)
                        if (expression.symbol.owner.parentAsClass.fqNameWhenAvailable?.asString() !=
                            "io.github.composefluent.winrt.gallery.code.KotlinCodeSpan") return
                        val end = (expression.arguments[0] as? IrConst)?.value as? Int ?: return
                        val kind = kinds[end] ?: return
                        val old = expression.arguments[1] as? IrGetEnumValue ?: return
                        val entry = old.symbol.owner.parentAsClass.declarations.filterIsInstance<IrEnumEntry>().single { it.name.asString() == kind.name }
                        expression.arguments[1] = IrGetEnumValueImpl(old.startOffset, old.endOffset, old.type, entry.symbol)
                    }
                })
            }
        })
    }
}
