package com.kneelawk.packvulcan.ui.util.markdown

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.kneelawk.packvulcan.model.IconSource
import com.kneelawk.packvulcan.ui.util.LocalCatchingUriHandler
import com.kneelawk.packvulcan.ui.util.widgets.AsyncIcon
import com.vladsch.flexmark.ast.*
import com.vladsch.flexmark.ast.Paragraph
import com.vladsch.flexmark.util.ast.Node
import java.awt.Cursor

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

fun AnnotatedString.Builder.appendMarkdownChildren(parent: Node, ctx: MDContext) {
    var child = parent.firstChild

    while (child != null) {
        appendMarkdownChild(child, ctx)

        child = child.next
    }
}

fun AnnotatedString.Builder.appendMarkdownChild(child: Node, ctx: MDContext) {
    when (child) {
        is Paragraph -> appendMarkdownChildren(child, ctx)
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
            appendMarkdownChildren(child, ctx)
            pop()
        }
        is StrongEmphasis -> {
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            appendMarkdownChildren(child, ctx)
            pop()
        }
        is Link -> {
            pushStyle(SpanStyle(ctx.pvColors.linkColor, textDecoration = TextDecoration.Underline))
            pushStringAnnotation(TAG_URL, child.url.unescape())
            appendMarkdownChildren(child, ctx)
            pop()
            pop()
        }
        is LinkRef -> {
            pushStyle(SpanStyle(ctx.pvColors.linkColor, textDecoration = TextDecoration.Underline))
            val url = child.getReferenceNode(child.document)?.url?.unescape()
            if (url != null) {
                pushStringAnnotation(TAG_URL, url)
            }
            appendMarkdownChildren(child, ctx)
            if (url != null) {
                pop()
            }
            pop()
        }
        is Code -> {
            pushStyle(
                SpanStyle(
                    fontFamily = FontFamily.Monospace, background = ctx.colors.secondary
                )
            )
            append(child.text.unescape())
            pop()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
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
    val uriHandler = LocalCatchingUriHandler.current
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    var pointerIcon by remember { mutableStateOf(PointerIcon(Cursor.getDefaultCursor())) }
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
    }.pointerHoverIcon(pointerIcon, false).onPointerEvent(PointerEventType.Move) {
        val pos = it.changes.first().position
        val layout = layoutResult.value
        pointerIcon = if (layout != null) {
            val position = layout.getOffsetForPosition(pos)
            val sa = text.getStringAnnotations(position, position).firstOrNull()
            if (sa != null) {
                if (sa.tag == TAG_URL || sa.tag == TAG_IMAGE_URL) {
                    PointerIcon(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
                } else {
                    PointerIcon(Cursor.getDefaultCursor())
                }
            } else {
                PointerIcon(Cursor.getDefaultCursor())
            }
        } else {
            PointerIcon(Cursor.getDefaultCursor())
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
