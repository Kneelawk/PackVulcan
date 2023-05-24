package com.kneelawk.packvulcan.ui.util.markdown

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.kneelawk.packvulcan.model.IconSource
import com.kneelawk.packvulcan.ui.theme.PackVulcanColors
import com.kneelawk.packvulcan.ui.util.widgets.AsyncIcon
import com.vladsch.flexmark.ast.*
import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.util.ast.Node

private const val TAG_IMAGE_URL = "TAG_IMAGE_URL"
private const val TAG_URL = "TAG_URL"

class MDText(private val text: AnnotatedString, private val style: TextStyle) : MDNode {
    @Composable
    override fun render() {
        MarkdownText(text, style = style)
    }

    override fun toString(): String {
        return "MDText(text=$text)"
    }
}

fun AnnotatedString.Builder.toMDText(style: TextStyle) = MDText(toAnnotatedString(), style)

fun AnnotatedString.Builder.appendMarkdownChildren(parent: Node, pvColors: PackVulcanColors) {
    var child = parent.firstChild

    while (child != null) {
        appendMarkdownChild(child, pvColors)

        child = child.next
    }
}

fun AnnotatedString.Builder.appendMarkdownChild(child: Node, pvColors: PackVulcanColors) {
    when (child) {
        is Paragraph -> appendMarkdownChildren(child, pvColors)
        is Text -> {
            append(child.chars.unescape())
        }
        is Image -> {
            val unescaped = child.url.unescape()
            if (unescaped.isNotBlank()) {
                appendInlineContent(TAG_IMAGE_URL, unescaped)
            }
        }
        is Emphasis -> {
            pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
            appendMarkdownChildren(child, pvColors)
            pop()
        }
        is StrongEmphasis -> {
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            appendMarkdownChildren(child, pvColors)
            pop()
        }
        is Link -> {
            pushStyle(SpanStyle(pvColors.linkColor, textDecoration = TextDecoration.Underline))
            pushStringAnnotation(TAG_URL, child.url.unescape())
            appendMarkdownChildren(child, pvColors)
            pop()
            pop()
        }
    }
}

@Composable
fun MarkdownText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.body1.copy(color = MaterialTheme.colors.onSurface),
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val pressIndicator = Modifier.pointerInput(Unit) {
        detectTapGestures { pos ->
            layoutResult.value?.let { layoutResult ->
                val position = layoutResult.getOffsetForPosition(pos)
                text.getStringAnnotations(position, position).firstOrNull()?.let { sa ->
                    if (sa.tag == TAG_URL) {
                        uriHandler.openUri(sa.item)
                    }
                    if (sa.tag == TAG_IMAGE_URL) {
                        uriHandler.openUri(sa.item)
                    }
                }
            }
        }
    }

    BasicText(
        text = text,
        modifier = modifier.then(pressIndicator),
        style = style,
        softWrap = softWrap,
        overflow = overflow,
        maxLines = maxLines,
        onTextLayout = {
            layoutResult.value = it
            onTextLayout(it)
        },
        inlineContent = mapOf(
            TAG_IMAGE_URL to InlineTextContent(
                Placeholder(
                    style.fontSize, style.fontSize,
                    PlaceholderVerticalAlign.Bottom
                )
            ) {
                AsyncIcon(source = IconSource.Url(it), modifier = Modifier.width(IntrinsicSize.Max))
            }
        )
    )
}
