# Gallery XAML migration

This matrix preserves the existing navigation routes and source ownership while the UI moves to adjacent XAML files. A route is complete only after its static UI, required upstream capabilities, source display, and interactions have been validated. The XamlSupport tooling-validation route is additional and does not count as migration of an existing page.

The JVM XamlSupport route has passed native validation with the published
`0.1.0-preview.2` compiler: XBF/PRI loading, controlling-outer connector identity,
independent named fields on two instances, idempotent initialization, and two real
ButtonAutomationPeer invocations reaching the private Kotlin handler. This does
not establish visual, Native, IDE, or existing-page migration acceptance.

Button now uses four `ControlExample` XAML instances and four independent
`SampleDefinition` text files. The shared control retains the original header,
error placeholder, three-column example/output/options grid, source expander,
and adaptive layout. CommunityToolkit animations and Gallery-private presenters
remain outside this adaptation. The wrapping sample uses Gallery's implicit
`ControlExample` content syntax after sorting authored WinMD attributes by parent;
the native load now calls all four `Example` setters. One native run bound the
styles source presenter to the preceding image sample; the mismatch has not
reproduced consistently, so named-instance identity needs continued observation.
CheckBox, RepeatButton, ToggleButton, and ToggleSwitch also use the shared
control with their own separate sample files. All five routes passed their
existing JVM native interaction checks after migration.
HyperlinkButton now follows the original Gallery's two-example XAML layout
with separate sample files. Its `x:Bind` enabled-state expression is handled
by a Kotlin click handler; the JVM native route loads both examples and
activates the window, while the hyperlink interactions remain to be checked.
RadioButton also keeps the original two-example layout, including implicit
`ControlExample` content and `x:String` items. The original substitution
presenters are not yet part of the shared control. Its JVM native route loads
both examples and activates the window; interactions remain to be checked.
DropDownButton uses the [upstream WinUI Gallery files](https://github.com/microsoft/WinUI-Gallery/tree/0451d6181395b46439f0d7492b2f36b70b484837/WinUIGallery/Samples/DropDownButton):
its XAML differs only in the Kotlin class and controls namespace, and both
sample text files are byte-for-byte copies. The JVM native route loads both
examples and activates the window; flyout interactions remain to be checked.
AppBarSeparator follows the same upstream-preserving pattern: its XAML changes
only the Kotlin class and controls namespace, and its one sample text file is
unchanged. The JVM native route loads its CommandBar example and activates the
window; visual and Native checks remain.
Pivot likewise preserves the upstream XAML apart from its Kotlin type names and
keeps the original sample text unchanged. Its JVM native route loads the Pivot
example and activates the window; visual and Native checks remain.
CalendarDatePicker also preserves the upstream one-example XAML and sample text
apart from the Kotlin type names. Its JVM native route loads the picker and
activates the window; visual and Native checks remain.
ToolTip preserves the upstream three-example XAML and all three sample text
files apart from Kotlin type names. Its JVM native route loads all three
examples and activates the window; tooltip interaction, visual and Native
checks remain.
Flyout preserves the upstream one-example XAML and unchanged sample text apart
from Kotlin type names. Its confirmation button calls the Kotlin handler to
hide the Flyout. Its JVM native route loads the example and activates the
window; confirmation interaction, visual and Native checks remain.
TextBox preserves the upstream four-example XAML and all four unchanged sample
texts apart from Kotlin type names. Its JVM native route connects all four
examples and activates the window; text interaction, visual and Native checks
remain.
PasswordBox preserves the upstream three-example XAML apart from Kotlin type
names. Its two purely declarative sample files are unchanged, while the C#
handler excerpt in the reveal-mode sample becomes Kotlin. The original
`OutputTextBlockStyle` is now available from the app's shared XAML resources.
Its JVM native route connects all three examples and activates the window;
password and reveal interactions, visual and Native checks remain.
DatePicker preserves the upstream two-example XAML and unchanged sample text
apart from Kotlin type names. The current Gallery constructs pages directly,
so the original navigation-time default date and year range are set after
`initializeComponent()` loads the named picker. Its JVM native route connects
both examples and activates the window; picker interaction, visual and Native
checks remain.
XamlStyles preserves the upstream two-example XAML and both unchanged sample
texts apart from Kotlin type names. Its JVM native route loads the explicit
and implicit style examples and activates the window; visual and Native checks
remain.

| Route | Group | Kotlin source | Existing sample factories | Migration |
| --- | --- | --- | ---: | --- |
| Home |  | [MainWindow.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/MainWindow.kt) | 0 | Pending |
| SystemBackdrops | Styles | [SystemBackdropsPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/styles/SystemBackdropsPage.kt) | 3 | Pending |
| AccessibilityColorContrast | AccessibilityItem | [AccessibilityColorContrastPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/accessibility/AccessibilityColorContrastPage.kt) | 0 | Pending |
| AccessibilityKeyboard | AccessibilityItem | [AccessibilityKeyboardPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/accessibility/AccessibilityKeyboardPage.kt) | 6 | Pending |
| AccessibilityScreenReader | AccessibilityItem | [AccessibilityScreenReaderPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/accessibility/AccessibilityScreenReaderPage.kt) | 11 | Pending |
| Button | BasicInput | [ButtonPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/basicinput/ButtonPage.kt) | 4 | Four shared XAML examples with separate source files; JVM native interaction/source checks passed; original template, visual and Native checks pending |
| CheckBox | BasicInput | [CheckBoxPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/basicinput/CheckBoxPage.kt) | 3 | Three shared XAML examples with separate source files; JVM native state/event/source checks passed; visual and Native checks pending |
| ColorPicker | BasicInput | [ColorPickerPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/basicinput/ColorPickerPage.kt) | 1 | Pending |
| ComboBox | BasicInput | [ComboBoxPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/basicinput/ComboBoxPage.kt) | 3 | Pending |
| DropDownButton | BasicInput | [DropDownButtonPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/basicinput/DropDownButtonPage.kt) | 2 | Original XAML and sample files preserved apart from namespace mapping; JVM native page load passed; flyout, visual and Native checks pending |
| HyperlinkButton | BasicInput | [HyperlinkButtonPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/basicinput/HyperlinkButtonPage.kt) | 2 | Two shared XAML examples with separate source files; JVM native page load passed; interaction, visual and Native checks pending |
| RadioButton | BasicInput | [RadioButtonPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/basicinput/RadioButtonPage.kt) | 2 | Two original-layout XAML examples with separate source files; JVM native page load passed; interaction, visual and Native checks pending |
| RatingControl | BasicInput | [RatingControlPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/basicinput/RatingControlPage.kt) | 2 | Pending |
| RepeatButton | BasicInput | [RepeatButtonPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/basicinput/RepeatButtonPage.kt) | 1 | Shared XAML example with separate source file; JVM native click/disable checks passed; visual and Native checks pending |
| Slider | BasicInput | [SliderPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/basicinput/SliderPage.kt) | 4 | Pending |
| SplitButton | BasicInput | [SplitButtonPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/basicinput/SplitButtonPage.kt) | 2 | Pending |
| ToggleButton | BasicInput | [ToggleButtonPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/basicinput/ToggleButtonPage.kt) | 1 | Shared XAML example with separate source file; JVM native state/event/disable checks passed; visual and Native checks pending |
| ToggleSplitButton | BasicInput | [ToggleSplitButtonPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/basicinput/ToggleSplitButtonPage.kt) | 1 | Pending |
| ToggleSwitch | BasicInput | [ToggleSwitchPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/basicinput/ToggleSwitchPage.kt) | 2 | Two shared XAML examples with separate source files; JVM native state/event/source checks passed; visual and Native checks pending |
| FlipView | Collections | [FlipViewPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/collections/FlipViewPage.kt) | 3 | Pending |
| GridView | Collections | [GridViewPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/collections/GridViewPage.kt) | 3 | Pending |
| ItemsRepeater | Collections | [ItemsRepeaterPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/collections/ItemsRepeaterPage.kt) | 6 | Pending |
| ItemsView | Collections | [ItemsViewPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/collections/ItemsViewPage.kt) | 3 | Pending |
| ListBox | Collections | [ListBoxPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/collections/ListBoxPage.kt) | 2 | Pending |
| ListView | Collections | [ListViewPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/collections/ListViewPage.kt) | 10 | Pending |
| PullToRefresh | Collections | [PullToRefreshPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/collections/PullToRefreshPage.kt) | 2 | Pending |
| TreeView | Collections | [TreeViewPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/collections/TreeViewPage.kt) | 4 | Pending |
| CalendarDatePicker | DateAndTime | [CalendarDatePickerPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/dateandtime/CalendarDatePickerPage.kt) | 1 | Original XAML and sample text preserved apart from namespace mapping; JVM native page load passed; visual and Native checks pending |
| CalendarView | DateAndTime | [CalendarViewPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/dateandtime/CalendarViewPage.kt) | 1 | Pending |
| DatePicker | DateAndTime | [DatePickerPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/dateandtime/DatePickerPage.kt) | 2 | Original XAML and two sample texts preserved apart from namespace mapping; JVM native page load passed; interaction, visual and Native checks pending |
| TimePicker | DateAndTime | [TimePickerPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/dateandtime/TimePickerPage.kt) | 3 | Pending |
| Color | DesignItem | [ColorPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/design/ColorPage.kt) | 0 | Pending |
| Geometry | DesignItem | [GeometryPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/design/GeometryPage.kt) | 1 | Pending |
| Iconography | DesignItem | [IconographyPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/design/IconographyPage.kt) | 0 | Pending |
| Spacing | DesignItem | [SpacingPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/design/SpacingPage.kt) | 0 | Pending |
| Typography | DesignItem | [TypographyPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/design/TypographyPage.kt) | 1 | Pending |
| ContentDialog | DialogsAndFlyouts | [ContentDialogPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/dialogsandflyouts/ContentDialogPage.kt) | 2 | Pending |
| Flyout | DialogsAndFlyouts | [FlyoutPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/dialogsandflyouts/FlyoutPage.kt) | 1 | Original XAML and sample text preserved apart from namespace mapping; JVM native page load passed; interaction, visual and Native checks pending |
| Popup | DialogsAndFlyouts | [PopupPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/dialogsandflyouts/PopupPage.kt) | 1 | Pending |
| TeachingTip | DialogsAndFlyouts | [TeachingTipPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/dialogsandflyouts/TeachingTipPage.kt) | 3 | Pending |
| Binding | FundamentalsItem | [BindingPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/fundamentals/BindingPage.kt) | 7 | Pending |
| CustomUserControls | FundamentalsItem | [CustomUserControlsPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/fundamentals/CustomUserControlsPage.kt) | 3 | Pending |
| CustomXamlConditionals | FundamentalsItem | [CustomXamlConditionalsPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/fundamentals/CustomXamlConditionalsPage.kt) | 3 | Pending |
| ScratchPad | FundamentalsItem | [ScratchPadPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/fundamentals/ScratchPadPage.kt) | 0 | Pending |
| Templates | FundamentalsItem | [TemplatesPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/fundamentals/TemplatesPage.kt) | 3 | Pending |
| XamlResources | FundamentalsItem | [XamlResourcesPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/fundamentals/XamlResourcesPage.kt) | 3 | Pending |
| XamlStyles | FundamentalsItem | [XamlStylesPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/fundamentals/XamlStylesPage.kt) | 2 | Original XAML and two sample texts preserved apart from namespace mapping; JVM native page load passed; visual and Native checks pending |
| Border | Layout | [BorderPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/layout/BorderPage.kt) | 1 | Pending |
| Canvas | Layout | [CanvasPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/layout/CanvasPage.kt) | 1 | Pending |
| Expander | Layout | [ExpanderPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/layout/ExpanderPage.kt) | 2 | Pending |
| Grid | Layout | [GridPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/layout/GridPage.kt) | 1 | Pending |
| RelativePanel | Layout | [RelativePanelPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/layout/RelativePanelPage.kt) | 1 | Pending |
| SplitView | Layout | [SplitViewPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/layout/SplitViewPage.kt) | 1 | Pending |
| StackPanel | Layout | [StackPanelPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/layout/StackPanelPage.kt) | 1 | Pending |
| VariableSizedWrapGrid | Layout | [VariableSizedWrapGridPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/layout/VariableSizedWrapGridPage.kt) | 1 | Pending |
| Viewbox | Layout | [ViewboxPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/layout/ViewboxPage.kt) | 1 | Pending |
| AnimatedVisualPlayer | Media | [AnimatedVisualPlayerPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/media/AnimatedVisualPlayerPage.kt) | 1 | Pending |
| CaptureElementPreview | Media | [CaptureElementPreviewPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/media/CaptureElementPreviewPage.kt) | 1 | Pending |
| Image | Media | [ImagePage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/media/ImagePage.kt) | 6 | Pending |
| MapControl | Media | [MapControlPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/media/MapControlPage.kt) | 1 | Pending |
| MediaPlayerElement | Media | [MediaPlayerElementPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/media/MediaPlayerElementPage.kt) | 2 | Pending |
| PersonPicture | Media | [PersonPicturePage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/media/PersonPicturePage.kt) | 1 | Pending |
| Sound | Media | [SoundPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/media/SoundPage.kt) | 3 | Pending |
| WebView2 | Media | [WebView2Page.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/media/WebView2Page.kt) | 1 | Pending |
| AppBarButton | MenusAndToolbars | [AppBarButtonPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/menusandtoolbars/AppBarButtonPage.kt) | 6 | Pending |
| AppBarSeparator | MenusAndToolbars | [AppBarSeparatorPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/menusandtoolbars/AppBarSeparatorPage.kt) | 1 | Original XAML and sample text preserved apart from namespace mapping; JVM native page load passed; visual and Native checks pending |
| AppBarToggleButton | MenusAndToolbars | [AppBarToggleButtonPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/menusandtoolbars/AppBarToggleButtonPage.kt) | 4 | Pending |
| CommandBar | MenusAndToolbars | [CommandBarPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/menusandtoolbars/CommandBarPage.kt) | 1 | Pending |
| CommandBarFlyout | MenusAndToolbars | [CommandBarFlyoutPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/menusandtoolbars/CommandBarFlyoutPage.kt) | 1 | Pending |
| MenuBar | MenusAndToolbars | [MenuBarPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/menusandtoolbars/MenuBarPage.kt) | 3 | Pending |
| MenuFlyout | MenusAndToolbars | [MenuFlyoutPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/menusandtoolbars/MenuFlyoutPage.kt) | 7 | Pending |
| StandardUICommand | MenusAndToolbars | [StandardUICommandPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/menusandtoolbars/StandardUICommandPage.kt) | 1 | Pending |
| SwipeControl | MenusAndToolbars | [SwipeControlPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/menusandtoolbars/SwipeControlPage.kt) | 5 | Pending |
| XamlUICommand | MenusAndToolbars | [XamlUICommandPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/menusandtoolbars/XamlUICommandPage.kt) | 1 | Pending |
| ConnectedAnimation | Motion | [ConnectedAnimationPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/motion/ConnectedAnimationPage.kt) | 4 | Pending |
| EasingFunction | Motion | [EasingFunctionPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/motion/EasingFunctionPage.kt) | 4 | Pending |
| ImplicitTransition | Motion | [ImplicitTransitionPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/motion/ImplicitTransitionPage.kt) | 6 | Pending |
| PageTransition | Motion | [PageTransitionPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/motion/PageTransitionPage.kt) | 1 | Pending |
| ParallaxView | Motion | [ParallaxViewPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/motion/ParallaxViewPage.kt) | 2 | Pending |
| ThemeTransition | Motion | [ThemeTransitionPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/motion/ThemeTransitionPage.kt) | 5 | Pending |
| XamlCompInterop | Motion | [XamlCompInteropPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/motion/XamlCompInteropPage.kt) | 5 | Pending |
| AppWindow | MultipleWindows | [AppWindowPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/multiplewindows/AppWindowPage.kt) | 7 | Pending |
| AppWindowTitleBar | MultipleWindows | [AppWindowTitleBarPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/multiplewindows/AppWindowTitleBarPage.kt) | 3 | Pending |
| CreateMultipleWindows | MultipleWindows | [CreateMultipleWindowsPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/multiplewindows/CreateMultipleWindowsPage.kt) | 1 | Pending |
| TitleBar | MultipleWindows | [TitleBarPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/multiplewindows/TitleBarPage.kt) | 3 | Pending |
| BreadcrumbBar | Navigation | [BreadcrumbBarPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/navigation/BreadcrumbBarPage.kt) | 2 | Pending |
| NavigationView | Navigation | [NavigationViewPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/navigation/NavigationViewPage.kt) | 8 | Pending |
| Pivot | Navigation | [PivotPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/navigation/PivotPage.kt) | 1 | Original XAML and sample text preserved apart from namespace mapping; JVM native page load passed; visual and Native checks pending |
| SelectorBar | Navigation | [SelectorBarPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/navigation/SelectorBarPage.kt) | 3 | Pending |
| TabView | Navigation | [TabViewPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/navigation/TabViewPage.kt) | 10 | Pending |
| AnnotatedScrollBar | Scrolling | [AnnotatedScrollBarPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/scrolling/AnnotatedScrollBarPage.kt) | 1 | Pending |
| PipsPager | Scrolling | [PipsPagerPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/scrolling/PipsPagerPage.kt) | 2 | Pending |
| ScrollView | Scrolling | [ScrollViewPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/scrolling/ScrollViewPage.kt) | 3 | Pending |
| ScrollViewer | Scrolling | [ScrollViewerPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/scrolling/ScrollViewerPage.kt) | 1 | Pending |
| SemanticZoom | Scrolling | [SemanticZoomPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/scrolling/SemanticZoomPage.kt) | 1 | Pending |
| AppNotification | Shell | [AppNotificationPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/shell/AppNotificationPage.kt) | 5 | Pending |
| BadgeNotificationManager | Shell | [BadgeNotificationManagerPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/shell/BadgeNotificationManagerPage.kt) | 2 | Pending |
| JumpList | Shell | [JumpListPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/shell/JumpListPage.kt) | 2 | Pending |
| InfoBadge | StatusAndInfo | [InfoBadgePage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/statusandinfo/InfoBadgePage.kt) | 4 | Pending |
| InfoBar | StatusAndInfo | [InfoBarPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/statusandinfo/InfoBarPage.kt) | 3 | Pending |
| ProgressBar | StatusAndInfo | [ProgressBarPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/statusandinfo/ProgressBarPage.kt) | 2 | Pending |
| ProgressRing | StatusAndInfo | [ProgressRingPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/statusandinfo/ProgressRingPage.kt) | 2 | Pending |
| ToolTip | StatusAndInfo | [ToolTipPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/statusandinfo/ToolTipPage.kt) | 3 | Original XAML and three sample texts preserved apart from namespace mapping; JVM native page load passed; tooltip interaction, visual and Native checks pending |
| Acrylic | Styles | [AcrylicPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/styles/AcrylicPage.kt) | 3 | Pending |
| AnimatedIcon | Styles | [AnimatedIconPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/styles/AnimatedIconPage.kt) | 2 | Pending |
| CompactSizing | Styles | [CompactSizingPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/styles/CompactSizingPage.kt) | 1 | Pending |
| IconElement | Styles | [IconElementPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/styles/IconElementPage.kt) | 6 | Pending |
| Line | Styles | [LinePage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/styles/LinePage.kt) | 4 | Pending |
| RadialGradientBrush | Styles | [RadialGradientBrushPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/styles/RadialGradientBrushPage.kt) | 1 | Pending |
| Shape | Styles | [ShapePage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/styles/ShapePage.kt) | 3 | Pending |
| SystemBackdropElement | Styles | [SystemBackdropElementPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/styles/SystemBackdropElementPage.kt) | 1 | Pending |
| ThemeShadow | Styles | [ThemeShadowPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/styles/ThemeShadowPage.kt) | 1 | Pending |
| Clipboard | System | [ClipboardPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/system/ClipboardPage.kt) | 10 | Pending |
| ContentIsland | System | [ContentIslandPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/system/ContentIslandPage.kt) | 1 | Pending |
| StoragePickers | System | [StoragePickersPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/system/StoragePickersPage.kt) | 5 | Pending |
| AutoSuggestBox | Text | [AutoSuggestBoxPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/text/AutoSuggestBoxPage.kt) | 2 | Pending |
| NumberBox | Text | [NumberBoxPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/text/NumberBoxPage.kt) | 3 | Pending |
| PasswordBox | Text | [PasswordBoxPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/text/PasswordBoxPage.kt) | 3 | Original XAML preserved apart from namespace mapping; three separate samples and Kotlin handlers; JVM native page load passed; interaction, visual and Native checks pending |
| RichEditBox | Text | [RichEditBoxPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/text/RichEditBoxPage.kt) | 5 | Pending |
| RichTextBlock | Text | [RichTextBlockPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/text/RichTextBlockPage.kt) | 4 | Pending |
| TextBlock | Text | [TextBlockPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/text/TextBlockPage.kt) | 5 | Pending |
| TextBox | Text | [TextBoxPage.kt](src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/text/TextBoxPage.kt) | 4 | Original XAML and four sample texts preserved apart from namespace mapping; JVM native page load passed; interaction, visual and Native checks pending |

## Shared surfaces

| Surface | Ownership | Migration |
| --- | --- | --- |
| Application resources and startup | Main.kt / GalleryApplication | Pending |
| Main window and navigation | MainWindow.kt / GalleryNavigationHost.kt | Pending |
| Page headers and sample frames | ControlExample.xaml / ControlExample.kt / GalleryTheme.kt | Eight BasicInput routes plus AppBarSeparator, Pivot, CalendarDatePicker, DatePicker, ToolTip, Flyout, TextBox, PasswordBox and XamlStyles use shared XAML control; original private components and other routes pending |
| Settings and All routes | GallerySettingsPage.kt / navigation host | Pending |
| Example source display and copy | processor / code-document / code UI | SampleDefinition selects separate XAML/Kotlin files on seventeen routes; remaining routes pending |
| Styles, dictionaries and templates | shared UI and relevant individual pages | Pending |

This is the source inventory (122 existing annotated routes including Home). Visual baselines, feature prerequisites, and interaction acceptance remain to be captured before marking any route complete.
