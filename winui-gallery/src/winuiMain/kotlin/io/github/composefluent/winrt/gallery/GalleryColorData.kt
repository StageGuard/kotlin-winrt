// Generated from MIT-licensed WinUI Gallery color documentation.
// Regenerate with tools/import-color-data.py <WinUI-Gallery checkout>.
package io.github.composefluent.winrt.gallery

internal data class PaletteTile(val name: String, val explanation: String, val key: String, val background: String, val foreground: String, val row: Int, val column: Int, val separator: Boolean, val backdrop: String, val comment: String)
internal data class PaletteBlock(val title: String = "", val description: String = "", val background: String = "", val foreground: String = "", val columns: Int = 1, val tiles: List<PaletteTile> = emptyList())
internal val galleryPalettes = listOf(
    listOf(
        PaletteBlock("Text", "For UI labels and static text.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 4, tiles = listOf(
            PaletteTile("Text / Primary", "Rest or Hover", "TextFillColorPrimaryBrush", "TextFillColorPrimaryBrush", "TextOnAccentFillColorPrimaryBrush", 0, 0, false, "", ""),
            PaletteTile("Text / Secondary", "Rest or Hover", "TextFillColorSecondaryBrush", "TextFillColorSecondaryBrush", "TextOnAccentFillColorPrimaryBrush", 0, 1, false, "", ""),
            PaletteTile("Text / Tertiary", "Pressed only (not accessible)", "TextFillColorTertiaryBrush", "TextFillColorTertiaryBrush", "TextOnAccentFillColorPrimaryBrush", 0, 2, false, "", ""),
            PaletteTile("Text / Disabled", "Disabled only (not accessible)", "TextFillColorDisabledBrush", "TextFillColorDisabledBrush", "TextFillColorPrimaryBrush", 0, 3, false, "", ""),
        )),
        PaletteBlock("Accent Text", "Recommended for links.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 4, tiles = listOf(
            PaletteTile("Accent Text / Primary", "Rest or Hover", "AccentTextFillColorPrimaryBrush", "AccentTextFillColorPrimaryBrush", "TextOnAccentFillColorPrimaryBrush", 0, 0, false, "", ""),
            PaletteTile("Accent Text / Secondary", "Rest or Hover", "AccentTextFillColorSecondaryBrush", "AccentTextFillColorSecondaryBrush", "TextOnAccentFillColorPrimaryBrush", 0, 1, false, "", ""),
            PaletteTile("Accent Text / Tertiary", "Pressed only (not accessible)", "AccentTextFillColorTertiaryBrush", "AccentTextFillColorTertiaryBrush", "TextOnAccentFillColorPrimaryBrush", 0, 2, false, "", ""),
            PaletteTile("Accent Text / Disabled", "Disabled only (not accessible)", "AccentTextFillColorDisabledBrush", "AccentTextFillColorDisabledBrush", "TextFillColorPrimaryBrush", 0, 3, false, "", ""),
        )),
        PaletteBlock("Text On Accent", "Used for text on accent colored controls or fills.", "AccentFillColorDefaultBrush", "TextOnAccentFillColorPrimaryBrush"),
        PaletteBlock(columns = 2, tiles = listOf(
            PaletteTile("Text on Accent / Primary", "Rest or Hover", "TextOnAccentFillColorPrimaryBrush", "TextOnAccentFillColorPrimaryBrush", "TextFillColorPrimaryBrush", 0, 0, true, "", ""),
            PaletteTile("Text on Accent / Secondary", "Pressed only (not accessible)", "TextOnAccentFillColorSecondaryBrush", "TextOnAccentFillColorSecondaryBrush", "TextFillColorPrimaryBrush", 0, 2, false, "", ""),
        )),
        PaletteBlock(columns = 2, tiles = listOf(
            PaletteTile("Text on Accent / Disabled", "Disabled only (not accessible)", "TextOnAccentFillColorDisabledBrush", "TextOnAccentFillColorDisabledBrush", "Black", 0, 0, true, "", ""),
            PaletteTile("Text on Accent / Selected Text", "For highlighted text in text entry experiences", "TextOnAccentFillColorSelectedTextBrush", "TextOnAccentFillColorSelectedTextBrush", "Black", 0, 2, false, "", ""),
        )),
    ),
    listOf(
        PaletteBlock("Control Fill", "Fill used for standard controls.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 4, tiles = listOf(
            PaletteTile("Control / Default", "Rest", "ControlFillColorDefaultBrush", "ControlFillColorDefaultBrush", "", 0, 0, true, "", ""),
            PaletteTile("Control / Secondary", "Hover", "ControlFillColorSecondaryBrush", "ControlFillColorSecondaryBrush", "", 0, 1, true, "", ""),
            PaletteTile("Control / Tertiary", "Pressed", "ControlFillColorTertiaryBrush", "ControlFillColorTertiaryBrush", "", 0, 2, true, "", ""),
            PaletteTile("Control / Quartenary", "Rest (Pill Button control)", "ControlFillColorQuarternaryBrush", "ControlFillColorQuarternaryBrush", "", 0, 3, false, "", ""),
        )),
        PaletteBlock(columns = 3, tiles = listOf(
            PaletteTile("Control / Disabled", "Disabled", "ControlFillColorDisabledBrush", "ControlFillColorDisabledBrush", "", 0, 0, true, "", ""),
            PaletteTile("Control / Transparent", "Rest", "ControlFillColorTransparentBrush", "ControlFillColorTransparentBrush", "", 0, 1, true, "", ""),
            PaletteTile("Control / Input Active", "Active/focused text input fields", "ControlFillColorInputActiveBrush", "ControlFillColorInputActiveBrush", "", 0, 2, false, "", ""),
        )),
        PaletteBlock("Control Alt Fill", "Fill used for the 'off' states of toggle controls.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 3, tiles = listOf(
            PaletteTile("Control Alt / Transparent", "", "ControlAltFillColorTransparentBrush", "ControlAltFillColorTransparentBrush", "", 0, 0, true, "", ""),
            PaletteTile("Control Alt / Secondary", "Rest", "ControlAltFillColorSecondaryBrush", "ControlAltFillColorSecondaryBrush", "", 0, 1, true, "", ""),
            PaletteTile("Control Alt / Tertiary", "Hover", "ControlAltFillColorTertiaryBrush", "ControlAltFillColorTertiaryBrush", "", 0, 2, false, "", ""),
        )),
        PaletteBlock(columns = 2, tiles = listOf(
            PaletteTile("Control Alt / Quarternary", "Pressed", "ControlAltFillColorQuarternaryBrush", "ControlAltFillColorQuarternaryBrush", "", 0, 0, true, "", ""),
            PaletteTile("Control Alt / Disabled", "Disabled", "ControlAltFillColorDisabledBrush", "ControlAltFillColorDisabledBrush", "", 0, 1, false, "", ""),
        )),
        PaletteBlock("Neutral Solid", "Fills used for Sliders thumb control to cover the track beneath it.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 1, tiles = listOf(
            PaletteTile("Control Solid / Default", "Rest", "ControlSolidFillColorDefaultBrush", "ControlSolidFillColorDefaultBrush", "", 0, 0, false, "", ""),
        )),
        PaletteBlock("Neutral Strong", "Used for controls that must meet contrast ratio requirements of 3:1.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 2, tiles = listOf(
            PaletteTile("Control Strong / Default", "Rest or hover", "ControlStrongFillColorDefaultBrush", "ControlStrongFillColorDefaultBrush", "TextFillColorInverseBrush", 0, 0, true, "", ""),
            PaletteTile("Control Strong / Disabled", "Disabled only (not accessible)", "ControlStrongFillColorDisabledBrush", "ControlStrongFillColorDisabledBrush", "TextFillColorPrimary", 0, 2, false, "", ""),
        )),
        PaletteBlock("Subtle Fill", "Used for list items and fills that are transparent at rest and appear upon interaction.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 4, tiles = listOf(
            PaletteTile("Subtle / Transparent ", "Rest", "SubtleFillColorTransparentBrush", "SubtleFillColorTransparentBrush", "TextFillColorPrimary", 0, 0, true, "", ""),
            PaletteTile("Subtle / Secondary", "Hover", "SubtleFillColorSecondaryBrush", "SubtleFillColorSecondaryBrush", "TextFillColorPrimary", 0, 1, true, "", ""),
            PaletteTile("Subtle / Tertiary ", "Pressed", "SubtleFillColorTertiaryBrush", "SubtleFillColorTertiaryBrush", "TextFillColorPrimary", 0, 2, true, "", ""),
            PaletteTile("Subtle / Disabled", "Disabled only (not accessible)", "SubtleFillColorDisabledBrush", "SubtleFillColorDisabledBrush", "TextFillColorPrimary", 0, 3, false, "", ""),
        )),
        PaletteBlock("Control On Image Fill", "Used for controls living on top of imagery.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 4, tiles = listOf(
            PaletteTile("Control On Image Fill Default", "Rest", "ControlOnImageFillColorDefaultBrush", "ControlOnImageFillColorDefaultBrush", "TextFillColorPrimary", 0, 0, true, "", ""),
            PaletteTile("Control On Image Fill Secondary", "Hover", "ControlOnImageFillColorSecondaryBrush", "ControlOnImageFillColorSecondaryBrush", "TextFillColorPrimary", 0, 1, true, "", ""),
            PaletteTile("Control On Image Fill Tertiary", "Pressed", "ControlOnImageFillColorTertiaryBrush", "ControlOnImageFillColorTertiaryBrush", "TextFillColorPrimary", 0, 2, true, "", ""),
            PaletteTile("Control On Image Fill Disabled", "Disabled only (not accessible)", "ControlOnImageFillColorDisabledBrush", "ControlOnImageFillColorDisabledBrush", "TextFillColorPrimary", 0, 3, false, "", ""),
        )),
        PaletteBlock("Accent Fill", "Used for accent fills on controls.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 3, tiles = listOf(
            PaletteTile("Accent / Default", "Rest", "AccentFillColorDefaultBrush", "AccentFillColorDefaultBrush", "TextOnAccentFillColorDefaultBrush", 0, 0, false, "", ""),
            PaletteTile("Accent / Secondary", "Hover", "AccentFillColorSecondaryBrush", "AccentFillColorSecondaryBrush", "TextOnAccentFillColorDefaultBrush", 0, 1, false, "", ""),
            PaletteTile("Accent / Tertiary", "Pressed", "AccentFillColorTertiaryBrush", "AccentFillColorTertiaryBrush", "TextOnAccentFillColorDefaultBrush", 0, 2, false, "", ""),
        )),
        PaletteBlock(columns = 2, tiles = listOf(
            PaletteTile("Accent / Disabled", "Disabled", "AccentFillColorDisabledBrush", "AccentFillColorDisabledBrush", "", 0, 0, false, "", ""),
            PaletteTile("Accent / Selected Text Background", "Highighted/selected text background", "AccentFillColorSelectedTextBackgroundBrush", "AccentFillColorSelectedTextBackgroundBrush", "TextOnAccentFillColorDefaultBrush", 0, 1, false, "", ""),
        )),
    ),
    listOf(
        PaletteBlock("Card Stroke", "Used for card and layer colors.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 2, tiles = listOf(
            PaletteTile("Card Stroke / Default", "Card layer and strokes", "CardStrokeColorDefaultBrush", "CardStrokeColorDefaultBrush", "", 0, 0, true, "", ""),
            PaletteTile("Card Stroke / Default Solid", "Solid equivalent of Card Stroke / Default. Used in command bar for expanded states", "CardStrokeColorDefaultSolidBrush", "CardStrokeColorDefaultSolidBrush", "", 0, 1, false, "", ""),
        )),
        PaletteBlock("Control Elevation (gradient strokes)", "Used for standard control strokes and stroke states.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 3, tiles = listOf(
            PaletteTile("Control / Border", "Rest", "ControlElevationBorderBrush", "ControlElevationBorderBrush", "", 0, 0, true, "", ""),
            PaletteTile("Circle / Border", "Rest", "CircleElevationBorderBrush", "CircleElevationBorderBrush", "", 0, 1, true, "", ""),
            PaletteTile("Text Control / Border", "Rest", "TextControlElevationBorderBrush", "TextControlElevationBorderBrush", "", 0, 2, false, "", ""),
        )),
        PaletteBlock(columns = 2, tiles = listOf(
            PaletteTile("Text Control / Border Focused", "Active text fields", "TextControlElevationBorderFocusedBrush", "TextControlElevationBorderFocusedBrush", "", 0, 0, true, "", ""),
            PaletteTile("Accent Control / Border", "Rest", "AccentControlElevationBorderBrush", "AccentControlElevationBorderBrush", "", 0, 1, false, "", ""),
        )),
        PaletteBlock("Control Stroke", "Used for gradient stops in elevation borders, and for control states.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 4, tiles = listOf(
            PaletteTile("Control Stroke / Default", "Used in Control Elevation Brushes. Pressed or Disabled", "ControlStrokeColorDefaultBrush", "ControlStrokeColorDefaultBrush", "", 0, 0, true, "", ""),
            PaletteTile("Control Stroke / Secondary", "Used in Control Elevation Brushes", "ControlStrokeColorSecondaryBrush", "ControlStrokeColorSecondaryBrush", "", 0, 1, true, "", ""),
            PaletteTile("Control Stroke / On Accent Default", "Used in Control Elevation Brushes. Pressed or Disabled", "ControlStrokeColorOnAccentDefaultBrush", "ControlStrokeColorOnAccentDefaultBrush", "", 0, 2, true, "", ""),
            PaletteTile("Control Stroke / On Accent Secondary", "Used in Control Elevation Brushes", "ControlStrokeColorOnAccentSecondaryBrush", "ControlStrokeColorOnAccentSecondaryBrush", "", 0, 3, false, "", ""),
        )),
        PaletteBlock(columns = 3, tiles = listOf(
            PaletteTile("Control Stroke / On Accent Tertiary", "Linework on Accent controls, ie: dividers", "ControlStrokeColorOnAccentTertiaryBrush", "ControlStrokeColorOnAccentTertiaryBrush", "", 0, 0, true, "", ""),
            PaletteTile("Control Stroke / On Accent Disabled", "Disabled", "ControlStrokeColorOnAccentDisabledBrush", "ControlStrokeColorOnAccentDisabledBrush", "", 0, 1, true, "", ""),
            PaletteTile("Control Stroke / For Strong Fill When On Image", "When used with a 'strong' fill color, ensures a 3:1 contrast on any background", "ControlStrokeColorForStrongFillWhenOnImageBrush", "ControlStrokeColorForStrongFillWhenOnImageBrush", "", 0, 2, false, "", ""),
        )),
        PaletteBlock("Control Strong Stroke", "Used for control strokes that must meet contrast ratio requirements of 3:1.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 2, tiles = listOf(
            PaletteTile("Control Strong Stroke / Default", "3:1 control border", "ControlStrongStrokeColorDefaultBrush", "ControlStrongStrokeColorDefaultBrush", "TextFillColorInverseBrush", 0, 0, true, "", ""),
            PaletteTile("Control Strong Stroke / Disabled", "Disabled", "ControlStrongStrokeColorDisabledBrush", "ControlStrongStrokeColorDisabledBrush", "", 0, 2, false, "", ""),
        )),
        PaletteBlock("Surface Stroke", "Used for strokes on background surfaces, ie: flyouts, windows, dialogs.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 2, tiles = listOf(
            PaletteTile("Surface Stroke / Default", "Window and dialog borders, theme inverse", "SurfaceStrokeColorDefaultBrush", "SurfaceStrokeColorDefaultBrush", "", 0, 0, true, "", ""),
            PaletteTile("Surface Stroke / Flyout", "Control flyouts, always dark", "SurfaceStrokeColorFlyoutBrush", "SurfaceStrokeColorFlyoutBrush", "", 0, 2, false, "", ""),
        )),
        PaletteBlock("Divider Stroke", "Used for divider and graphic lines.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 1, tiles = listOf(
            PaletteTile("Divider Stroke / Default", "Content dividers", "DividerStrokeColorDefaultBrush", "DividerStrokeColorDefaultBrush", "TextFillColorPrimary", 0, 0, false, "", ""),
        )),
        PaletteBlock("Focus Stroke", "Used for divider and graphic lines. Theme inverse; dark in light theme and light in dark theme.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 2, tiles = listOf(
            PaletteTile("Focus / Outer", "Outer stroke color", "FocusStrokeColorOuterBrush", "FocusStrokeColorOuterBrush", "TextFillColorInverseBrush", 0, 0, true, "", ""),
            PaletteTile("Focus / Inner", "Inner stroke color", "FocusStrokeColorInnerBrush", "FocusStrokeColorInnerBrush", "", 0, 1, false, "", ""),
        )),
    ),
    listOf(
        PaletteBlock("Card Background", "Used to create 'cards' - content blocks that live on page and layer backgrounds.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 3, tiles = listOf(
            PaletteTile("Card Background / Default", "Default card color", "CardBackgroundFillColorDefaultBrush", "CardBackgroundFillColorDefaultBrush", "", 0, 0, true, "", ""),
            PaletteTile("Card Background / Secondary", "Alternate card color: slightly darker", "CardBackgroundFillColorSecondaryBrush", "CardBackgroundFillColorSecondaryBrush", "", 0, 1, true, "", ""),
            PaletteTile("Card Background / Tertiary", "Default card hover and pressed color", "CardBackgroundFillColorTertiaryBrush", "CardBackgroundFillColorTertiaryBrush", "", 0, 2, false, "", ""),
        )),
        PaletteBlock("Smoke Background", "Used over windows and desktop to block them out as inaccessible.", "SmokeFillColorDefaultBrush", ""),
        PaletteBlock(columns = 1, tiles = listOf(
            PaletteTile("Smoke / Default", "Dims the background behind dialogs", "SmokeFillColorDefaultBrush", "SmokeFillColorDefaultBrush", "", 0, 0, false, "", ""),
        )),
        PaletteBlock("Layer", "Used on background colors of any material to create layering.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 2, tiles = listOf(
            PaletteTile("Layer / Default", "Content layer color", "LayerFillColorDefaultBrush", "LayerFillColorDefaultBrush", "", 0, 0, true, "", ""),
            PaletteTile("Layer / Alt", "Alternate content layer color", "LayerFillColorAltBrush", "LayerFillColorAltBrush", "", 0, 1, false, "", ""),
        )),
        PaletteBlock("Layer on Acrylic", "Used on background colors of any material to create layering.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 1, tiles = listOf(
            PaletteTile("Layer On Acrylic / Default", "Content layer color on acrylic surfaces", "LayerOnAcrylicFillColorDefaultBrush", "LayerOnAcrylicFillColorDefaultBrush", "", 0, 0, false, "Acrylic", ""),
        )),
        PaletteBlock("Layer on Mica Base Alt", "Used for fills on Tab control.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 2, tiles = listOf(
            PaletteTile("Layer On Mica Base Alt / Default", "Active Tab Rest, Content layer", "LayerOnMicaBaseAltFillColorDefaultBrush", "LayerOnMicaBaseAltFillColorDefaultBrush", "TextFillColorPrimary", 0, 0, true, "MicaAlt", ""),
            PaletteTile("Layer On Mica Base Alt / Tertiary", "Active Tab Drag", "LayerOnMicaBaseAltFillColorTertiaryBrush", "LayerOnMicaBaseAltFillColorTertiaryBrush", "TextFillColorPrimary", 0, 1, false, "MicaAlt", ""),
        )),
        PaletteBlock(columns = 2, tiles = listOf(
            PaletteTile("Layer On Mica Base Alt / Transparent", "Inactive Tab Rest", "LayerOnMicaBaseAltFillColorTransparentBrush", "LayerOnMicaBaseAltFillColorTransparentBrush", "TextFillColorPrimary", 0, 0, true, "MicaAlt", ""),
            PaletteTile("Layer On Mica Base Alt / Secondary", "Inactive Tab Hover", "LayerOnMicaBaseAltFillColorSecondaryBrush", "LayerOnMicaBaseAltFillColorSecondaryBrush", "TextFillColorPrimary", 0, 1, false, "MicaAlt", ""),
        )),
        PaletteBlock("Solid Background", "Solid background colors to place layers, cards or controls on.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 4, tiles = listOf(
            PaletteTile("Solid Background / Base", "Used for the bottom most layer of an experience", "SolidBackgroundFillColorBaseBrush", "SolidBackgroundFillColorBaseBrush", "TextFillColorPrimary", 0, 0, true, "", ""),
            PaletteTile("Solid Background / Base Alt", "Used for the bottom most layer of an experience", "SolidBackgroundFillColorBaseAltBrush", "SolidBackgroundFillColorBaseAltBrush", "TextFillColorPrimary", 0, 1, true, "", ""),
            PaletteTile("Solid Background / Secondary", "Alternate base color for those who need a darker background color", "SolidBackgroundFillColorSecondaryBrush", "SolidBackgroundFillColorSecondaryBrush", "TextFillColorPrimary", 0, 2, true, "", ""),
            PaletteTile("Solid Background / Tertiary", "Content layer color", "SolidBackgroundFillColorTertiaryBrush", "SolidBackgroundFillColorTertiaryBrush", "TextFillColorPrimary", 0, 3, false, "", ""),
        )),
        PaletteBlock(columns = 3, tiles = listOf(
            PaletteTile("Solid Background / Quarternary", "Alt content layer color", "SolidBackgroundFillColorQuarternaryBrush", "SolidBackgroundFillColorQuarternaryBrush", "TextFillColorPrimary", 0, 0, true, "", ""),
            PaletteTile("Solid Background / Quinary", "Used for solid default card colors", "SolidBackgroundFillColorQuinaryBrush", "SolidBackgroundFillColorQuinaryBrush", "TextFillColorPrimary", 0, 1, true, "", ""),
            PaletteTile("Solid Background / Senary", "Used for solid default card colors", "SolidBackgroundFillColorSenaryBrush", "SolidBackgroundFillColorSenaryBrush", "TextFillColorPrimary", 0, 2, false, "", ""),
        )),
        PaletteBlock("Mica Background", "Mica background colors to place layers, cards, or controls on.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 2, tiles = listOf(
            PaletteTile("Mica Background / Base", "Used for the bottom most layer of an experience", "", "", "", 0, 0, true, "Mica", "See SystemBackdrop and SystemBackdropElement"),
            PaletteTile("Mica Background / Base Alt", "Default tab band background color", "", "", "", 0, 1, false, "MicaAlt", "See SystemBackdrop and SystemBackdropElement"),
        )),
        PaletteBlock("Acrylic Background", "Acrylic background colors to place layers, cards, or controls on.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 2, tiles = listOf(
            PaletteTile("Acrylic Background / Base", "Used for the bottom most layer of an acrylic surface only when the surface will use layers", "AcrylicBackgroundFillColorBaseBrush", "AcrylicBackgroundFillColorBaseBrush", "", 0, 0, true, "Acrylic", ""),
            PaletteTile("Acrylic Background / Default", "Default acrylic recipe used for control flyouts and surfaces that live with in the context of an app", "AcrylicBackgroundFillColorDefaultBrush", "AcrylicBackgroundFillColorDefaultBrush", "", 0, 1, false, "Acrylic", ""),
        )),
        PaletteBlock("Accent Acrylic Background", "Acrylic background colors to place layers, cards, or controls on.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 2, tiles = listOf(
            PaletteTile("Accent Acrylic Background / Base", "Used for the bottom most layer of an acrylic surface only when the surface will use layers", "AccentAcrylicBackgroundFillColorBaseBrush", "AccentAcrylicBackgroundFillColorBaseBrush", "", 0, 0, true, "", ""),
            PaletteTile("Accent Acrylic Background / Default", "Default acrylic recipe used for control flyouts and surfaces that live with in the context of an app", "AccentAcrylicBackgroundFillColorDefaultBrush", "AccentAcrylicBackgroundFillColorDefaultBrush", "", 0, 1, false, "", ""),
        )),
    ),
    listOf(
        PaletteBlock("System", "Used for accent fills on controls.", "SolidBackgroundFillColorQuarternaryBrush", ""),
        PaletteBlock(columns = 3, tiles = listOf(
            PaletteTile("System / Success", "Badge", "SystemFillColorSuccessBrush", "SystemFillColorSuccessBrush", "TextFillColorInverseBrush", 0, 0, false, "", ""),
            PaletteTile("System / Caution", "Badge", "SystemFillColorCautionBrush", "SystemFillColorCautionBrush", "TextFillColorInverseBrush", 0, 1, false, "", ""),
            PaletteTile("System / Critical", "Badge", "SystemFillColorCriticalBrush", "SystemFillColorCriticalBrush", "TextFillColorInverseBrush", 0, 2, false, "", ""),
        )),
        PaletteBlock(columns = 3, tiles = listOf(
            PaletteTile("System / Success Background", "Infobar Background", "SystemFillColorSuccessBackgroundBrush", "SystemFillColorSuccessBackgroundBrush", "", 0, 0, false, "", ""),
            PaletteTile("System / Caution Background", "Infobar Background", "SystemFillColorCautionBackgroundBrush", "SystemFillColorCautionBackgroundBrush", "", 0, 1, false, "", ""),
            PaletteTile("System / Critical Background", "Infobar Background", "SystemFillColorCriticalBackgroundBrush", "SystemFillColorCriticalBackgroundBrush", "", 0, 2, false, "", ""),
        )),
        PaletteBlock(columns = 3, tiles = listOf(
            PaletteTile("System / Attention", "Badge", "SystemFillColorAttentionBrush", "SystemFillColorAttentionBrush", "TextFillColorInverseBrush", 0, 0, false, "", ""),
            PaletteTile("System / Neutral", "Badge", "SystemFillColorNeutralBrush", "SystemFillColorNeutralBrush", "TextFillColorInverseBrush", 0, 1, true, "", ""),
            PaletteTile("System / Solid Neutral", "Neutral badges over content", "SystemFillColorSolidNeutralBrush", "SystemFillColorSolidNeutralBrush", "TextFillColorInverseBrush", 0, 2, false, "", ""),
        )),
        PaletteBlock(columns = 3, tiles = listOf(
            PaletteTile("System / Attention Background", "Infobar Background", "SystemFillColorAttentionBackgroundBrush", "SystemFillColorAttentionBackgroundBrush", "", 0, 0, true, "", ""),
            PaletteTile("System / Neutral Background", "Infobar Background", "SystemFillColorNeutralBackgroundBrush", "SystemFillColorNeutralBackgroundBrush", "", 0, 1, true, "", ""),
            PaletteTile("System / Solid Neutral Background", "Neutral badges over content", "SystemFillColorSolidNeutralBackgroundBrush", "SystemFillColorSolidNeutralBackgroundBrush", "", 0, 2, false, "", ""),
        )),
        PaletteBlock(columns = 1, tiles = listOf(
            PaletteTile("System / Solid Attention Background", "", "SystemFillColorSolidAttentionBackgroundBrush", "SystemFillColorSolidAttentionBackgroundBrush", "", 0, 2, false, "", ""),
        )),
    ),
    listOf(
        PaletteBlock(title = "Below are the default high contrast themes shown. The brush names are the same, and the OS will choose the right colors based on the selected theme."),
        PaletteBlock(title = "Aquatic"),
        PaletteBlock(columns = 4, tiles = listOf(
            PaletteTile("Window Text Color", "Foreground / Text color for Headings, body copy, lists, placeholder text, app and window borders, any UI that can't be interacted with", "SystemColorWindowTextColor", "#FFFFFF", "#202020", 0, 0, false, "", ""),
            PaletteTile("Window Color", "Background of pages, panes, popups, and windows", "SystemColorWindowColor", "#202020", "#FFFFFF", 1, 0, false, "", ""),
            PaletteTile("Highlight Text Color", "Foreground color for text or UI that is selected, interacted with (hover, pressed), or in progress", "SystemColorHighlightTextColor", "#263B50", "#8EE3F0", 0, 1, false, "", ""),
            PaletteTile("Highlight Color", "Background or accent color for UI that is selected, interacted with (hover, pressed), or in progress", "SystemColorHighlightColor", "#8EE3F0", "#263B50", 1, 1, false, "", ""),
            PaletteTile("Button Text Color", "Foreground color for buttons and any UI that can be interacted with", "SystemColorButtonTextColor", "#FFFFFF", "#202020", 0, 2, false, "", ""),
            PaletteTile("Button Face Color", "Background color for buttons and any UI that can be interacted with", "SystemColorButtonFaceColor", "#202020", "#FFFFFF", 1, 2, false, "", ""),
            PaletteTile("Hotlight Color", "Foreground / Text color for hyperlink text", "SystemColorHotlightColor", "#75E9FC", "#202020", 0, 3, false, "", ""),
            PaletteTile("Gray Text Color / Disabled", "Foreground / Text color for Inactive (disabled) UI", "SystemColorGrayTextColor", "#A6A6A6", "#FFFFFF", 1, 3, false, "", ""),
        )),
        PaletteBlock(title = "Desert"),
        PaletteBlock(columns = 4, tiles = listOf(
            PaletteTile("Window Text Color", "Foreground / Text color for Headings, body copy, lists, placeholder text, app and window borders, any UI that can't be interacted with", "SystemColorWindowTextColor", "#3D3D3D", "#FFFAEF", 0, 0, false, "", ""),
            PaletteTile("Window Color", "Background of pages, panes, popups, and windows", "SystemColorWindowColor", "#FFFAEF", "#3D3D3D", 1, 0, false, "", ""),
            PaletteTile("Highlight Text Color", "Foreground color for text or UI that is selected, interacted with (hover, pressed), or in progress", "SystemColorHighlightTextColor", "#FFF5E3", "#903909", 0, 1, false, "", ""),
            PaletteTile("Highlight Color", "Background or accent color for UI that is selected, interacted with (hover, pressed), or in progress", "SystemColorHighlightColor", "#903909", "#FFF5E3", 1, 1, false, "", ""),
            PaletteTile("Button Text Color", "Foreground color for buttons and any UI that can be interacted with", "SystemColorButtonTextColor", "#202020", "#FFFAEF", 0, 2, false, "", ""),
            PaletteTile("Button Face Color", "Background color for buttons and any UI that can be interacted with", "SystemColorButtonFaceColor", "#FFFAEF", "#202020", 1, 2, false, "", ""),
            PaletteTile("Hotlight Color", "Foreground / Text color for hyperlink text", "SystemColorHotlightColor", "#1C5E75", "#FFFAEF", 0, 3, false, "", ""),
            PaletteTile("Gray Text Color / Disabled", "Foreground / Text color for Inactive (disabled) UI", "SystemColorGrayTextColor", "#676767", "#FFFAEF", 1, 3, false, "", ""),
        )),
        PaletteBlock(title = "Dusk"),
        PaletteBlock(columns = 4, tiles = listOf(
            PaletteTile("Window Text Color", "Foreground / Text color for Headings, body copy, lists, placeholder text, app and window borders, any UI that can't be interacted with", "SystemColorWindowTextColor", "#FFFFFF", "#2D3236", 0, 0, false, "", ""),
            PaletteTile("Window Color", "Background of pages, panes, popups, and windows", "SystemColorWindowColor", "#2D3236", "#FFFFFF", 1, 0, false, "", ""),
            PaletteTile("Highlight Text Color", "Foreground color for text or UI that is selected, interacted with (hover, pressed), or in progress", "SystemColorHighlightTextColor", "#212D3B", "#ABCFF2", 0, 1, false, "", ""),
            PaletteTile("Highlight Color", "Background or accent color for UI that is selected, interacted with (hover, pressed), or in progress", "SystemColorHighlightColor", "#ABCFF2", "#212D3B", 1, 1, false, "", ""),
            PaletteTile("Button Text Color", "Foreground color for buttons and any UI that can be interacted with", "SystemColorButtonTextColor", "#B6F6F0", "#2D3236", 0, 2, false, "", ""),
            PaletteTile("Button Face Color", "Background color for buttons and any UI that can be interacted with", "SystemColorButtonFaceColor", "#2D3236", "#B6F6F0", 1, 2, false, "", ""),
            PaletteTile("Hotlight Color", "Foreground / Text color for hyperlink text", "SystemColorHotlightColor", "#70EBDE", "#202020", 0, 3, false, "", ""),
            PaletteTile("Gray Text Color / Disabled", "Foreground / Text color for Inactive (disabled) UI", "SystemColorGrayTextColor", "#A6A6A6", "#FFFFFF", 1, 3, false, "", ""),
        )),
        PaletteBlock(title = "Night Sky"),
        PaletteBlock(columns = 4, tiles = listOf(
            PaletteTile("Window Text Color", "Foreground / Text color for Headings, body copy, lists, placeholder text, app and window borders, any UI that can't be interacted with", "SystemColorWindowTextColor", "#FFFFFF", "#000000", 0, 0, false, "", ""),
            PaletteTile("Window Color", "Background of pages, panes, popups, and windows", "SystemColorWindowColor", "#000000", "#FFFFFF", 1, 0, false, "", ""),
            PaletteTile("Highlight Text Color", "Foreground color for text or UI that is selected, interacted with (hover, pressed), or in progress", "SystemColorHighlightTextColor", "#2B2B2B", "#D6B4FD", 0, 1, false, "", ""),
            PaletteTile("Highlight Color", "Background or accent color for UI that is selected, interacted with (hover, pressed), or in progress", "SystemColorHighlightColor", "#D6B4FD", "#2B2B2B", 1, 1, false, "", ""),
            PaletteTile("Button Text Color", "Foreground color for buttons and any UI that can be interacted with", "SystemColorButtonTextColor", "#FFEE32", "#000000", 0, 2, false, "", ""),
            PaletteTile("Button Face Color", "Background color for buttons and any UI that can be interacted with", "SystemColorButtonFaceColor", "#000000", "#FFEE32", 1, 2, false, "", ""),
            PaletteTile("Hotlight Color", "Foreground / Text color for hyperlink text", "SystemColorHotlightColor", "#8080FF", "#FFFFFF", 0, 3, false, "", ""),
            PaletteTile("Gray Text Color / Disabled", "Foreground / Text color for Inactive (disabled) UI", "SystemColorGrayTextColor", "#A6A6A6", "#000000", 1, 3, false, "", ""),
        )),
    ),
)
