package io.github.composefluent.winrt.gallery.basicinput

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.controls.primitives.*
import microsoft.ui.xaml.media.SolidColorBrush
import microsoft.ui.xaml.shapes.Rectangle
import microsoft.ui.text.MarkerType
import windows.foundation.Uri

@GalleryPage(route = "ToggleSplitButton", title = "ToggleSplitButton", group = "BasicInput", order = 6)
internal fun toggleSplitButtonPage() = ExamplePage {
    val editor = RichEditBox().apply { width = 240.0; minHeight = 96.0; placeholderText = "Type something here" }
    example("A ToggleSplitButton for a bulleted list.", toggleSplitButtonBulletsSample(editor), editor)
}

@GallerySample(route = "ToggleSplitButton", title = "A ToggleSplitButton for a bulleted list.")
internal fun toggleSplitButtonBulletsSample(editor: RichEditBox) = ToggleSplitButton().apply {
    var marker = MarkerType.Bullet
    val icon = SymbolIcon(Symbol.List)
    val toggle = this
    content = icon; named(this, "Bullets")
    val menu = Flyout()
    menu.content = StackPanel().apply { this.spacing = 8.0; this.orientation = Orientation.Horizontal; listOf(Symbol.List to MarkerType.Bullet, Symbol.Bullets to MarkerType.UppercaseRoman).forEach { (symbol, type) ->
            children.add(Button().apply {
                content = SymbolIcon(symbol)
                named(this, if (type == MarkerType.Bullet) "Bullets" else "Roman Numerals")
                click.add { _, _ ->
                    marker = type; icon.symbol = symbol; toggle.isChecked = true
                    editor.document!!.selection!!.paragraphFormat!!.listType = marker
                    menu.hide(); editor.focus(FocusState.Keyboard)
                }
            })
        } }
    flyout = menu
    isCheckedChanged.add { _, _ -> editor.document!!.selection!!.paragraphFormat!!.listType = if (isChecked) marker else MarkerType.None }
}
