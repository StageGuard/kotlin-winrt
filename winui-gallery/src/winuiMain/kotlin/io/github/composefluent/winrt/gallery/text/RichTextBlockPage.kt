package io.github.composefluent.winrt.gallery.text

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.documents.*
import windows.foundation.Uri
import windows.ui.text.FontStyle
import windows.ui.text.FontWeight

@GalleryPage(route = "RichTextBlock", title = "RichTextBlock", group = "Text", order = 4)
internal fun richTextBlockPage() = ExamplePage {
    example("A simple RichTextBlock.", richTextBlockSimpleRichTextBlockSample())
    example("A RichTextBlock with custom selection highlighting.", richTextBlockRichTextBlockWithCustomSelectionHighlightingSample1())
    example("RichTextBlock overflow.", richTextBlockRichTextBlockOverflowSample2())
    val highlighted = richTextBlockCustomTextHighlightingSample3()
    fun highlight(color: UInt) {
        highlighted.textHighlighters.clear()
        highlighted.textHighlighters.add(TextHighlighter().apply { background = brush(color); ranges.add(TextRange(28, 11)) })
    }
    highlight(0xFFFF00u)
    example("Custom text highlighting.", highlighted, select("Text highlighting color", listOf("Yellow", "Red", "Blue")) { highlight(listOf(0xFFFF00u, 0xFF0000u, 0x0000FFu)[it]) })
}

@GallerySample(route = "RichTextBlock", title = "A simple RichTextBlock.")
internal fun richTextBlockSimpleRichTextBlockSample() = RichTextBlock().apply {
    blocks.add(Paragraph().apply { inlines.add(Run().apply { text = "I am a RichTextBlock." }) })
}

@GallerySample(route = "RichTextBlock", title = "A RichTextBlock with custom selection highlighting.")
internal fun richTextBlockRichTextBlockWithCustomSelectionHighlightingSample1() = RichTextBlock().apply {
        selectionHighlightColor = brush(0x008000u)
        blocks.add(Paragraph().apply {
            inlines.add(Run().apply { text = "RichTextBlock provides a rich text display container that supports " })
            inlines.add(Run().apply {
                text = "formatted text"
                fontStyle = FontStyle.Italic
                fontWeight = FontWeight(700u)
            })
            inlines.add(Run().apply { text = ", " })
            inlines.add(Hyperlink().apply {
                navigateUri = Uri("https://learn.microsoft.com/windows/windows-app-sdk/api/winrt/microsoft.ui.xaml.documents.hyperlink")
                inlines.add(Run().apply { text = "hyperlinks" })
            })
            inlines.add(Run().apply { text = ", inline images, and other rich content." })
        })
        blocks.add(Paragraph().apply {
            inlines.add(Run().apply { text = "RichTextBlock also supports a built-in overflow model." })
        })
    }

@GallerySample(route = "RichTextBlock", title = "RichTextBlock overflow.")
internal fun richTextBlockRichTextBlockOverflowSample2() = Grid().apply {
        height = 300.0; repeat(3) { columnDefinitions.add(column(1.0, GridUnitType.Star)) }
        val first = RichTextBlockOverflow().apply { Grid.setColumn(this, 1); margin = Thickness(12.0, 0.0, 12.0, 0.0) }
        val second = RichTextBlockOverflow().apply { Grid.setColumn(this, 2); margin = Thickness(12.0, 0.0, 12.0, 0.0) }
        first.overflowContentTarget = second
        children.add(RichTextBlock().apply {
            margin = Thickness(12.0, 0.0, 12.0, 0.0); textAlignment = TextAlignment.Justify; overflowContentTarget = first
            blocks.add(Paragraph().apply {
                inlines.add(Run().apply {
                    text = "Linked text containers allow text which does not fit in one element to overflow into a different element on the page. Creative use of linked text containers enables basic multicolumn support and other advanced page layouts."
                })
            })
            blocks.add(Paragraph().apply {
                inlines.add(Run().apply {
                    text = "Duis sed nulla metus, id hendrerit velit. Curabitur dolor purus, bibendum eu cursus lacinia, interdum vel augue. Aenean euismod eros et sapien vehicula dictum. Duis ullamcorper, turpis nec feugiat tincidunt, dui erat luctus risus, aliquam accumsan lacus est vel quam. Nunc lacus massa, varius eget accumsan id, congue sed orci. Duis dignissim hendrerit egestas. Proin ut turpis magna, sit amet porta erat. Nunc semper metus nec magna imperdiet nec vestibulum dui fringilla. Sed sed ante libero, nec porttitor mi. Ut luctus, neque vitae placerat egestas, urna leo auctor magna, sit amet ultricies ipsum felis quis sapien. Proin eleifend varius dui, at vestibulum nunc consectetur nec. Mauris nulla elit, ultrices a sodales non, aliquam ac est. Quisque sit amet risus nulla. Quisque vestibulum posuere velit, vitae vestibulum eros scelerisque sit amet. In in risus est, at laoreet dolor. Nullam aliquet pellentesque convallis. Ut vel tincidunt nulla. Mauris auctor tincidunt auctor. Aenean orci ante, vulputate ac sagittis sit amet, consequat at mi. Morbi elementum purus consectetur nisi adipiscing vitae blandit sapien placerat. Aliquam adipiscing tortor non sem lobortis consectetur mattis felis rhoncus. Nunc eu nunc rhoncus arcu sollicitudin ultrices. In vulputate eros in mauris aliquam id dignissim nisl laoreet."
                })
            })
        }); children.add(first); children.add(second)
    }

@GallerySample(route = "RichTextBlock", title = "Custom text highlighting.")
internal fun richTextBlockCustomTextHighlightingSample3() = RichTextBlock().apply {
    blocks.add(Paragraph().apply {
        inlines.add(Run().apply {
            text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua"
        })
    })
}
