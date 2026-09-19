package io.github.composefluent.winrt.gallery.processor

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.com.intellij.lang.LighterASTNode
import org.jetbrains.kotlin.com.intellij.lang.impl.PsiBuilderFactoryImpl
import org.jetbrains.kotlin.com.intellij.openapi.util.Ref
import org.jetbrains.kotlin.lexer.KotlinLexer
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.parsing.KotlinLightParser
import org.jetbrains.kotlin.parsing.KotlinParserDefinition

/** Extracts the code belonging to each example call from one annotated sample function. */
internal class KotlinExampleExtractor {
    data class Example(val titles: List<String>, val fragment: KotlinSourceFragment) {
        val source: String get() = fragment.source
    }
    private data class Call(val start: Int, val end: Int, val titles: List<String>)

    fun extract(source: String, functionName: String): List<Example> {
        val builder = PsiBuilderFactoryImpl().createBuilder(KotlinParserDefinition(), KotlinLexer(), source)
        val tree = KotlinLightParser.parse(builder, isScript = false)
        val calls = mutableListOf<Call>()
        var functionStart = -1
        var functionEnd = -1
        val stringLiteral = Regex("\"([^\"\\\\]*)\"")

        fun <T> withChildren(node: LighterASTNode, action: (List<LighterASTNode>) -> T): T {
            val ref = Ref<Array<LighterASTNode?>>()
            val count = tree.getChildren(node, ref)
            val children = ref.get()
            return try { action(children?.take(count)?.filterNotNull().orEmpty()) }
            finally { if (children != null) tree.disposeChildren(children, count) }
        }

        fun titles(arguments: LighterASTNode): List<String> = withChildren(arguments) { children ->
            val first = children.firstOrNull { it.tokenType == KtNodeTypes.VALUE_ARGUMENT } ?: return@withChildren emptyList()
            val argument = source.substring(first.startOffset, first.endOffset)
            stringLiteral.findAll(argument).map { it.groupValues[1] }.toList()
        }

        fun visit(node: LighterASTNode, withinFunction: Boolean = false) {
            withChildren(node) { children ->
                val isTarget = node.tokenType == KtNodeTypes.FUN && children.any {
                    it.tokenType == KtTokens.IDENTIFIER && source.substring(it.startOffset, it.endOffset) == functionName
                }
                if (isTarget) {
                    functionStart = node.startOffset
                    functionEnd = node.endOffset
                }
                val inside = withinFunction || isTarget
                if (inside && node.tokenType == KtNodeTypes.CALL_EXPRESSION && children.any {
                    it.tokenType == KtNodeTypes.REFERENCE_EXPRESSION && source.substring(it.startOffset, it.endOffset) == "example"
                }) {
                    val arguments = children.firstOrNull { it.tokenType == KtNodeTypes.VALUE_ARGUMENT_LIST }
                    calls += Call(node.startOffset, node.endOffset, arguments?.let(::titles).orEmpty())
                }
                children.forEach { visit(it, inside) }
            }
        }
        visit(tree.root)
        if (functionStart < 0) return emptyList()
        if (calls.isEmpty()) return listOf(Example(emptyList(), KotlinSourceFragment.from(
            source, functionStart, functionEnd, source.substring(functionStart, functionEnd).trim())))

        fun lineEnd(offset: Int): Int = source.indexOf('\n', offset).let { if (it < 0) source.length else it }
        val sorted = calls.sortedBy { it.start }
        val openingBrace = source.indexOf('{', functionStart).takeIf { it in functionStart until sorted.first().start }
        var start = openingBrace?.let { lineEnd(it) + 1 } ?: functionStart
        return sorted.map { call ->
            val end = lineEnd(call.end)
            val snippet = source.substring(start.coerceAtMost(end), end).trimIndent().trim('\n', '\r')
            val fragment = KotlinSourceFragment.from(source, start.coerceAtMost(end), end, snippet)
            start = (end + 1).coerceAtMost(source.length)
            Example(call.titles, fragment)
        }
    }
}
