package io.github.composefluent.winrt.gallery.processor

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.com.intellij.lang.LighterASTNode
import org.jetbrains.kotlin.com.intellij.lang.impl.PsiBuilderFactoryImpl
import org.jetbrains.kotlin.com.intellij.openapi.util.Ref
import org.jetbrains.kotlin.lexer.KotlinLexer
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.parsing.KotlinLightParser
import org.jetbrains.kotlin.parsing.KotlinParserDefinition

/** Reads the expression body of an annotated sample factory, excluding its page layout. */
internal class KotlinSampleSourceExtractor {
    fun extract(source: String, functionName: String): String = extractFragment(source, functionName).source

    fun extractFragment(source: String, functionName: String): KotlinSourceFragment {
        val builder = PsiBuilderFactoryImpl().createBuilder(KotlinParserDefinition(), KotlinLexer(), source)
        val tree = KotlinLightParser.parse(builder, isScript = false)

        fun <T> withChildren(node: LighterASTNode, action: (List<LighterASTNode>) -> T): T {
            val ref = Ref<Array<LighterASTNode?>>()
            val count = tree.getChildren(node, ref)
            val children = ref.get()
            return try { action(children?.take(count)?.filterNotNull().orEmpty()) }
            finally { if (children != null) tree.disposeChildren(children, count) }
        }

        fun find(node: LighterASTNode): KotlinSourceFragment? = withChildren(node) { parts ->
            if (node.tokenType == KtNodeTypes.FUN && parts.any {
                    it.tokenType == KtTokens.IDENTIFIER && source.substring(it.startOffset, it.endOffset) == functionName
                }) {
                val equals = parts.firstOrNull { it.tokenType == KtTokens.EQ }
                    ?: error("@$functionName sample must use an expression body")
                return@withChildren KotlinSourceFragment.from(source, equals.endOffset, node.endOffset,
                    source.substring(equals.endOffset, node.endOffset).trim().trimIndent())
            }
            parts.firstNotNullOfOrNull(::find)
        }

        return requireNotNull(find(tree.root)) { "Could not locate sample function $functionName" }
    }
}
