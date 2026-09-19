package io.github.composefluent.winrt.gallery.accessibility

import io.github.composefluent.winrt.gallery.*
import microsoft.ui.xaml.*
import microsoft.ui.xaml.controls.*
import microsoft.ui.xaml.automation.AutomationProperties
import microsoft.ui.xaml.automation.peers.*
import windows.ui.text.FontStyle

@GalleryPage(route = "AccessibilityScreenReader", title = "Screen Reader", group = "AccessibilityItem", order = 2)
internal fun screenReaderPage() = ExamplePage {
    spacing = 12.0
    children.add(label("Accessibility is about building experiences that make your Windows application usable by people of all abilities. For more information about designing accessible apps:"))
    children.add(referenceLink("Accessibility overview", "https://learn.microsoft.com/windows/apps/design/accessibility/accessibility-overview"))
    children.add(label("Screen readers, such as Narrator, convert text into spoken words to help blind or low vision users. Screen readers use the UI Automation (UIA) names of each control to report their name, role and content."))
    children.add(referenceLink("Narrator", "https://support.microsoft.com/windows/complete-guide-to-narrator-e4397a0d-ef4f-b386-d8ae-c172f109bdb1"))
    children.add(accessibleHeading("Accessible names"))
    children.add(label("An accessible name is a short, descriptive text string that a screen reader uses to describe a UI element.\n\nTypically, the accessible name should be short and match the visual label of the control. Screen reader users will hear this name every time they navigate to that control.\n\nIf the control's content can be converted to a string, an accessible name is automatically determined from the visible text. However, elements such as images or input fields need to have a custom accessible name."))
    children.add(referenceLink("Expose basic accessibility information: Accessible name", "https://learn.microsoft.com/windows/apps/design/accessibility/basic-accessibility-information#accessible-name"))
    children.add(accessibleHeading("Getting an accessible name automatically", 3))
    children.add(label("For most controls, WinUI automatically sets an accessible name from the control's content (if the content is a string)."))
    example("Name from content.", accessibilityScreenReaderNameFromContentSample())
    example("Name from header and placeholder.", accessibilityScreenReaderNameFromHeaderAndPlaceholderSample1())
    children.add(accessibleHeading("Setting an accessible name manually", 3))
    children.add(label("Controls without stringable content will not get an accessible name automatically."))
    example("Name on a ListView.", accessibilityScreenReaderNameOnAListViewSample2())
    example("Name on an Image.", accessibilityScreenReaderNameOnAnImageSample3())
    example("Name from a label.", accessibilityScreenReaderNameFromALabelSample4())
    children.add(accessibleHeading("Common accessibility properties"))
    children.add(label("Besides accessible name, common accessibility properties include:\n\n- Description and help text\n- Position in set\n- Headings and landmarks (see below)"))
    children.add(referenceLink("Expose basic accessibility information", "https://learn.microsoft.com/windows/apps/design/accessibility/basic-accessibility-information"))
    example("Description and help text.", accessibilityScreenReaderDescriptionAndHelpTextSample5())
    example("Position in a set.", accessibilityScreenReaderPositionInASetSample6())
    children.add(accessibleHeading("Visual tree"))
    children.add(label("UIA exposes multiple views of the UI tree: Control, Content, and Raw.\n\nMost accessibility tools use the Control or Content views, so you can effectively hide redundant or unhelpful controls from screen readers by putting them in the Raw view."))
    children.add(referenceLink("Influencing the UI Automation tree views", "https://learn.microsoft.com/windows/apps/design/accessibility/basic-accessibility-information#influencing-the-ui-automation-tree-views"))
    example("Remove a control from the content visual tree.", accessibilityScreenReaderRemoveAControlFromTheContentVisualTreeSample7())
    children.add(accessibleHeading("Landmarks and headings"))
    children.add(label("Landmarks and headings indicate, or label, different sections of a user interface for screen readers and other Assistive Technologies (ATs), just like visible headings do for visual users. Marking up your content with landmarks and headings lets screen reader users skim content similarly to sighted users."))
    children.add(referenceLink("Landmarks and headings", "https://learn.microsoft.com/windows/apps/design/accessibility/landmarks-and-headings"))
    children.add(accessibleHeading("Landmarks", 3))
    children.add(label("Landmarks typically identify big sections of your UI, like search, main content, or navigation. You can also add landmarks with custom names."))
    example("Landmarks.", accessibilityScreenReaderLandmarksSample8())
    children.add(accessibleHeading("Headings", 3))
    children.add(label("Headings typically identify smaller groups of content. They usually correspond to the visual headings in your UI — the text that labels sections in your UI visually.\n\nFor example, all of the section headings on this page are accessible headings."))
    example("Headings.", accessibilityScreenReaderHeadingsSample9())
    children.add(accessibleHeading("Associating smaller groups of controls", 3))
    children.add(label("You can also group controls together manually, even if the controls don't have a visible heading or landmark, by adding an accessible name to the parent container. When entering a region with a name or a landmark, Narrator will read it out as Context. Narrator users can control their Context level in Settings.\n\nThis is helpful in complicated UI with many similar elements, where the grouping might be obvious to visual users, but not screen reader users."))
    example("Control groups.", accessibilityScreenReaderControlGroupsSample10())
}

private fun contacts() = ListView().apply {
    width = 300.0; horizontalAlignment = HorizontalAlignment.Left
    listOf("Nathan Quinn", "Jessica Lamber", "Carl Bond", "Jessica Russel").forEach { items.add(it) }
}

@GallerySample(route = "AccessibilityScreenReader", title = "Name from content.")
internal fun accessibilityScreenReaderNameFromContentSample() = stack {
        children.add(Button().apply { content = "Download survey" })
        children.add(TextBlock().apply { this.text = "Screen readers will read this button as Download survey. The name is automatically derived from the Button's Content."; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
    }

@GallerySample(route = "AccessibilityScreenReader", title = "Name from header and placeholder.")
internal fun accessibilityScreenReaderNameFromHeaderAndPlaceholderSample1() = stack {
        children.add(TextBox().apply { width = 200.0; horizontalAlignment = HorizontalAlignment.Left; header = "Name" })
        children.add(TextBox().apply { width = 200.0; horizontalAlignment = HorizontalAlignment.Left; placeholderText = "Nickname" })
        children.add(TextBox().apply { minWidth = 200.0; horizontalAlignment = HorizontalAlignment.Left; header = "Email"; placeholderText = "test@example.com" })
        children.add(TextBlock().apply { this.text = "Screen readers will read these TextBoxes as Name, Nickname and Email. The names are automatically derived from their headers or placeholders.\n\nWhen both Header and PlaceholderText are present, Header is used as name and PlaceholderText is used as description."; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
    }

@GallerySample(route = "AccessibilityScreenReader", title = "Name on a ListView.")
internal fun accessibilityScreenReaderNameOnAListViewSample2() = stack {
        children.add(contacts().apply { AutomationProperties.setName(this, "Contacts") })
        children.add(TextBlock().apply { this.text = "Screen readers will read this ListView as Contacts, while each item will be read using its respective text content."; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
    }

@GallerySample(route = "AccessibilityScreenReader", title = "Name on an Image.")
internal fun accessibilityScreenReaderNameOnAnImageSample3() = stack {
        children.add(Border().apply { cornerRadius = corners(4.0); child = Image().apply { this.width = Double.NaN; this.height = Double.NaN; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri("ms-appx:///Assets/SampleMedia/grapes.jpg") ) }.apply { height = 150.0; horizontalAlignment = HorizontalAlignment.Left; AutomationProperties.setName(this, "Grapes") } })
        children.add(TextBlock().apply { this.text = "The image above will be read out as Grapes. To navigate in Narrator through elements that aren't focusable, use Caps + Left/Right arrow."; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
    }

@GallerySample(route = "AccessibilityScreenReader", title = "Name from a label.")
internal fun accessibilityScreenReaderNameFromALabelSample4() = stack {
        val inputLabel = TextBlock().apply { this.text = "Searching Photos:"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }
        children.add(inputLabel)
        children.add(TextBox().apply { width = 200.0; horizontalAlignment = HorizontalAlignment.Left; AutomationProperties.setLabeledBy(this, inputLabel) })
        children.add(TextBlock().apply { this.text = "The TextBox above is labeled by the TextBlock and will be read as Searching Photos."; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
    }

@GallerySample(route = "AccessibilityScreenReader", title = "Description and help text.")
internal fun accessibilityScreenReaderDescriptionAndHelpTextSample5() = StackPanel().apply { this.spacing = 8.0; val description = "Deletes all cached items when closing the browser. This includes cookies, images, and browsing history."
        children.add(CheckBox().apply { content = "Clear cache on exit"; AutomationProperties.setFullDescription(this, description) })
        children.add(TextBlock().apply { this.text = description; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { foreground = GalleryTheme.brush("TextFillColorSecondaryBrush"); AutomationProperties.setAccessibilityView(this, AccessibilityView.Raw) })
        children.add(Button().apply { content = "Cancel RSS subscriptions"; AutomationProperties.setHelpText(this, "Launch the cancellation wizard"); ToolTipService.setToolTip(this, "Launch the cancellation wizard") }) }

@GallerySample(route = "AccessibilityScreenReader", title = "Position in a set.")
internal fun accessibilityScreenReaderPositionInASetSample6() = StackPanel().apply { this.spacing = 0.0; val students = TextBlock().apply { this.text = "Students"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { AutomationProperties.setAccessibilityView(this, AccessibilityView.Raw) }
        children.add(students); children.add(contacts().apply { AutomationProperties.setLabeledBy(this, students) })
        children.add(StackPanel().apply { this.spacing = 8.0; this.orientation = Orientation.Horizontal; listOf("View", "Rename", "Delete").forEachIndexed { index, title -> children.add(Button().apply { content = title; AutomationProperties.setPositionInSet(this, index + 1); AutomationProperties.setSizeOfSet(this, 3) }) } }) }

@GallerySample(route = "AccessibilityScreenReader", title = "Remove a control from the content visual tree.")
internal fun accessibilityScreenReaderRemoveAControlFromTheContentVisualTreeSample7() = StackPanel().apply { this.spacing = 0.0; this.orientation = Orientation.Horizontal; children.add(Border().apply { cornerRadius = corners(4.0); child = Image().apply { this.width = Double.NaN; this.height = Double.NaN; this.source = microsoft.ui.xaml.media.imaging.BitmapImage(windows.foundation.Uri("ms-appx:///Assets/SampleMedia/treetops.jpg") ) }.apply { height = 40.0; verticalAlignment = VerticalAlignment.Top; AutomationProperties.setAccessibilityView(this, AccessibilityView.Raw) } })
        children.add(TextBlock().apply { this.text = "This is some demo text. The image on the right is just for decoration and serves no informational purpose. To prevent Narrator or other screen readers from reading out the image, we set the accessibility view to Raw which removes it from the content visual tree."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { maxWidth = 400.0; margin = Thickness(8.0, -4.0, 0.0, 0.0) }) }

@GallerySample(route = "AccessibilityScreenReader", title = "Landmarks.")
internal fun accessibilityScreenReaderLandmarksSample8() = StackPanel().apply { this.spacing = 0.0; children.add(TextBlock().apply { this.text = "The sample below showcases landmarks. To navigate landmarks in Narrator, press D or Shift+D while in Scan mode."; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { margin = Thickness(0.0, 0.0, 0.0, 10.0) })
        children.add(Grid().apply {
            columnDefinitions.add(column(200.0, GridUnitType.Pixel)); columnDefinitions.add(column(1.0, GridUnitType.Star)); columnDefinitions.add(column(200.0, GridUnitType.Pixel))
            children.add(StackPanel().apply { this.spacing = 8.0; padding = inset(6.0); background = GalleryTheme.brush("CardStrokeColorDefaultBrush"); cornerRadius = corners(4.0); AutomationProperties.setLandmarkType(this, AutomationLandmarkType.Navigation)
                children.add(AutoSuggestBox().apply { placeholderText = "Search"; AutomationProperties.setLandmarkType(this, AutomationLandmarkType.Search) })
                children.add(Button().apply { content = "Open settings" }) })
            children.add(StackPanel().apply { this.spacing = 0.0; Grid.setColumn(this, 1); padding = inset(6.0); AutomationProperties.setLandmarkType(this, AutomationLandmarkType.Main); children.add(TextBlock().apply { this.text = sampleLorem; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }) })
            children.add(StackPanel().apply { this.spacing = 8.0; Grid.setColumn(this, 2); padding = inset(6.0); background = GalleryTheme.brush("CardStrokeColorDefaultBrush"); cornerRadius = corners(4.0)
                AutomationProperties.setLandmarkType(this, AutomationLandmarkType.Custom); AutomationProperties.setLocalizedLandmarkType(this, "Current viewers")
                children.add(TextBlock().apply { this.text = "Current viewers"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { AutomationProperties.setHeadingLevel(this, AutomationHeadingLevel.Level1) })
                children.add(TextBlock().apply { this.text = "(No other users viewing)"; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { fontStyle = FontStyle.Italic }) })
        }) }

@GallerySample(route = "AccessibilityScreenReader", title = "Headings.")
internal fun accessibilityScreenReaderHeadingsSample9() = StackPanel().apply { this.spacing = 0.0; horizontalAlignment = HorizontalAlignment.Left
        children.add(TextBlock().apply { this.text = "The sample below showcases headings. To navigate headings in Narrator, press H or Shift+H while in Scan mode."; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { margin = Thickness(0.0, 0.0, 0.0, 10.0) })
        children.add(StackPanel().apply { this.spacing = 0.0; maxWidth = 500.0; horizontalAlignment = HorizontalAlignment.Left
            fun heading(text: String, level: Int, size: Double) = TextBlock().apply { this.text = text; this.fontSize = size; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }.apply { AutomationProperties.setHeadingLevel(this, listOf(AutomationHeadingLevel.Level1, AutomationHeadingLevel.Level2, AutomationHeadingLevel.Level3)[level - 1]) }
            children.add(heading("Lorem ipsums", 1, 26.0)); children.add(heading("Lorem ipsum", 2, 22.0))
            children.add(TextBlock().apply { this.text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque feugiat velit pulvinar, vehicula nisi at, molestie risus. Duis consequat auctor libero vitae consectetur. Nullam efficitur euismod lacinia."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
            children.add(heading("Cat ipsum", 2, 22.0)); children.add(heading("Standard", 3, 18.0))
            children.add(TextBlock().apply { this.text = "Mice litter kitter kitty litty little kitten big roar roar feed me but i will ruin the couch with my claws and hunt by meowing loudly at 5am next to human."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
            children.add(heading("Cat breeds", 3, 18.0)); children.add(TextBlock().apply { this.text = "Tabby abyssinian for jaguar. Thai russian blue and ragdoll, ocicat. Mouser puma so american bobtail for donskoy balinese . Scottish fold manx so siamese."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
            children.add(heading("Bacon ipsum", 2, 22.0)); children.add(TextBlock().apply { this.text = "Bacon ipsum dolor amet meatball nulla labore, tempor sirloin chicken frankfurter tail drumstick ex cupim ground round."; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }) }) }

@GallerySample(route = "AccessibilityScreenReader", title = "Control groups.")
internal fun accessibilityScreenReaderControlGroupsSample10() = StackPanel().apply { this.spacing = 8.0; children.add(TextBlock().apply { this.text = "The sample below groups items using accessible names. Screen readers will read this as users navigate between different groups. To force read the current context in Narrator, press Caps+/."; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
        children.add(TextBlock().apply { this.text = "Note how Narrator reads My albums or Shared with me if you tab between the ListViews, and reads the additional context Album browser when Caps+/ is pressed."; this.fontSize = 12.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap })
        children.add(StackPanel().apply { this.spacing = 0.0; AutomationProperties.setName(this, "Album browser")
            listOf("My albums" to listOf("Trip to Redmond", "Visiting Ben"), "Shared with me" to listOf("Valeria's cat", "Paul's winter vacation", "Cool street photography")).forEach { (title, albums) -> children.add(StackPanel().apply { this.spacing = 0.0; AutomationProperties.setName(this, title); children.add(TextBlock().apply { this.text = title; this.fontSize = 14.0; this.textWrapping = microsoft.ui.xaml.TextWrapping.Wrap }); children.add(ListView().apply { albums.forEach { items.add(it) } }) }) } }) }
