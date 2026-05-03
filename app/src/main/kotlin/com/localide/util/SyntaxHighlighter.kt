package com.localide.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.localide.ui.theme.*

data class SyntaxRule(
    val pattern: Regex,
    val color: Color
)

object SyntaxHighlighter {

    private val kotlinRules = listOf(
        SyntaxRule(Regex("""//[^\n]*"""), SyntaxComment),
        SyntaxRule(Regex("""/\*[\s\S]*?\*/"""), SyntaxComment),
        SyntaxRule(Regex(""""([^"\\]|\\.)*"|'([^'\\]|\\.)*'|"""""""[\s\S]*?"""""""), SyntaxString),
        SyntaxRule(Regex("""\b(fun|class|object|interface|val|var|if|else|when|for|while|do|return|import|package|data|sealed|abstract|open|override|private|protected|public|internal|companion|by|in|is|as|null|true|false|this|super|constructor|init|lateinit|lazy|suspend|coroutine|inline|reified|crossinline|noinline|typealias|enum|annotation|throw|try|catch|finally|break|continue)\b"""), SyntaxKeyword),
        SyntaxRule(Regex("""\b(Int|Long|Double|Float|Boolean|String|Char|Unit|Any|Nothing|List|Map|Set|Array|Pair|Triple|MutableList|MutableMap|MutableSet)\b"""), SyntaxType),
        SyntaxRule(Regex("""@\w+"""), SyntaxAnnotation),
        SyntaxRule(Regex("""\b\d+\.?\d*[fFLl]?\b"""), SyntaxNumber),
        SyntaxRule(Regex("""\b([a-zA-Z_]\w*)\s*(?=\()"""), SyntaxFunction),
    )

    private val javaRules = listOf(
        SyntaxRule(Regex("""//[^\n]*"""), SyntaxComment),
        SyntaxRule(Regex("""/\*[\s\S]*?\*/"""), SyntaxComment),
        SyntaxRule(Regex(""""([^"\\]|\\.)*"|'([^'\\]|\\.)*'"""), SyntaxString),
        SyntaxRule(Regex("""\b(public|private|protected|static|final|abstract|class|interface|extends|implements|new|return|if|else|for|while|do|switch|case|break|continue|throw|throws|try|catch|finally|import|package|this|super|void|null|true|false|instanceof)\b"""), SyntaxKeyword),
        SyntaxRule(Regex("""\b(int|long|double|float|boolean|String|char|byte|short|Object|Integer|Long|Double|Float|Boolean|Character|List|Map|Set|ArrayList|HashMap|HashSet)\b"""), SyntaxType),
        SyntaxRule(Regex("""@\w+"""), SyntaxAnnotation),
        SyntaxRule(Regex("""\b\d+\.?\d*[fFLl]?\b"""), SyntaxNumber),
        SyntaxRule(Regex("""\b([a-zA-Z_]\w*)\s*(?=\()"""), SyntaxFunction),
    )

    private val jsRules = listOf(
        SyntaxRule(Regex("""//[^\n]*"""), SyntaxComment),
        SyntaxRule(Regex("""/\*[\s\S]*?\*/"""), SyntaxComment),
        SyntaxRule(Regex(""""([^"\\]|\\.)*"|'([^'\\]|\\.)*'|`([^`\\]|\\.)*`"""), SyntaxString),
        SyntaxRule(Regex("""\b(const|let|var|function|return|if|else|for|while|do|switch|case|break|continue|class|extends|new|this|super|import|export|default|from|async|await|try|catch|finally|throw|typeof|instanceof|in|of|null|undefined|true|false|void|delete|yield)\b"""), SyntaxKeyword),
        SyntaxRule(Regex("""\b\d+\.?\d*\b"""), SyntaxNumber),
        SyntaxRule(Regex("""\b([a-zA-Z_]\w*)\s*(?=\()"""), SyntaxFunction),
    )

    private val pythonRules = listOf(
        SyntaxRule(Regex("""#[^\n]*"""), SyntaxComment),
        SyntaxRule(Regex("""'''[\s\S]*?'''|\"\"\"[\s\S]*?\"\"\""""), SyntaxComment),
        SyntaxRule(Regex(""""([^"\\]|\\.)*"|'([^'\\]|\\.)*'"""), SyntaxString),
        SyntaxRule(Regex("""\b(def|class|return|if|elif|else|for|while|with|as|import|from|pass|break|continue|try|except|finally|raise|and|or|not|in|is|lambda|yield|async|await|True|False|None|global|nonlocal|del)\b"""), SyntaxKeyword),
        SyntaxRule(Regex("""\b(int|str|float|bool|list|dict|set|tuple|bytes|type|object|print|len|range|enumerate|zip|map|filter|sorted|reversed|open|super|property)\b"""), SyntaxType),
        SyntaxRule(Regex("""@\w+"""), SyntaxAnnotation),
        SyntaxRule(Regex("""\b\d+\.?\d*\b"""), SyntaxNumber),
        SyntaxRule(Regex("""\b([a-zA-Z_]\w*)\s*(?=\()"""), SyntaxFunction),
    )

    private val htmlRules = listOf(
        SyntaxRule(Regex("""<!--[\s\S]*?-->"""), SyntaxComment),
        SyntaxRule(Regex(""""([^"\\]|\\.)*"|'([^'\\]|\\.)*'"""), SyntaxString),
        SyntaxRule(Regex("""</?\w[\w-]*"""), SyntaxKeyword),
        SyntaxRule(Regex("""\b[\w-]+=(?=")"""), SyntaxAnnotation),
        SyntaxRule(Regex("""&\w+;"""), SyntaxNumber),
    )

    private val cssRules = listOf(
        SyntaxRule(Regex("""/\*[\s\S]*?\*/"""), SyntaxComment),
        SyntaxRule(Regex(""""([^"\\]|\\.)*"|'([^'\\]|\\.)*'"""), SyntaxString),
        SyntaxRule(Regex("""[.#][\w-]+|:[\w-]+"""), SyntaxKeyword),
        SyntaxRule(Regex("""[\w-]+\s*(?=:)"""), SyntaxFunction),
        SyntaxRule(Regex("""#[0-9a-fA-F]{3,8}|\b\d+\.?\d*(px|em|rem|%|vh|vw|pt|s|ms)?\b"""), SyntaxNumber),
        SyntaxRule(Regex("""\b(var|calc|rgb|rgba|hsl|hsla|linear-gradient|radial-gradient)\b"""), SyntaxType),
    )

    private val shellRules = listOf(
        SyntaxRule(Regex("""#[^\n]*"""), SyntaxComment),
        SyntaxRule(Regex(""""([^"\\]|\\.)*"|'([^'\\]|\\.)*'"""), SyntaxString),
        SyntaxRule(Regex("""\b(if|then|else|elif|fi|for|while|do|done|case|esac|function|return|export|local|readonly|shift|source|echo|exit|break|continue)\b"""), SyntaxKeyword),
        SyntaxRule(Regex("""\$\{?[\w@#?*!-]+\}?"""), SyntaxType),
        SyntaxRule(Regex("""\b\d+\b"""), SyntaxNumber),
    )

    private val jsonRules = listOf(
        SyntaxRule(Regex(""""([^"\\]|\\.)*"\s*:"""), SyntaxFunction),
        SyntaxRule(Regex(""":\s*"([^"\\]|\\.)*""""), SyntaxString),
        SyntaxRule(Regex("""\b(true|false|null)\b"""), SyntaxKeyword),
        SyntaxRule(Regex("""\b-?\d+\.?\d*([eE][+-]?\d+)?\b"""), SyntaxNumber),
    )

    fun highlight(code: String, extension: String): AnnotatedString {
        val rules = when (extension.lowercase()) {
            "kt", "kts" -> kotlinRules
            "java" -> javaRules
            "js", "jsx", "ts", "tsx" -> jsRules
            "py" -> pythonRules
            "html", "htm" -> htmlRules
            "css", "scss", "sass" -> cssRules
            "sh", "bash", "zsh" -> shellRules
            "json" -> jsonRules
            else -> return buildAnnotatedString { append(code) }
        }

        return buildAnnotatedString {
            append(code)
            val covered = BooleanArray(code.length)
            for (rule in rules) {
                for (match in rule.pattern.findAll(code)) {
                    val start = match.range.first
                    val end = match.range.last + 1
                    if (covered.slice(start until end).any { it }) continue
                    addStyle(SpanStyle(color = rule.color), start, end)
                    for (i in start until end) covered[i] = true
                }
            }
        }
    }
}
