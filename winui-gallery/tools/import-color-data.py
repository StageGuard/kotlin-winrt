"""Import MIT Gallery palette documentation as Kotlin data, never runtime markup."""
import json
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

source = Path(sys.argv[1]) / 'WinUIGallery/Controls/DesignGuidance/ColorSections'
destination = Path(__file__).resolve().parents[1] / 'src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/GalleryColorData.kt'
def quote(value):
    return json.dumps(value, ensure_ascii=True).replace('$', '\\$')
def kind(element):
    return element.tag.split('}')[-1]
def resource(value):
    return value.removeprefix('{ThemeResource ').removeprefix('{StaticResource ').removesuffix('}')

lines = ['// Generated from MIT-licensed WinUI Gallery color documentation.',
         '// Regenerate with tools/import-color-data.py <WinUI-Gallery checkout>.',
         'package io.github.composefluent.winrt.gallery', '',
         'internal data class PaletteTile(val name: String, val explanation: String, val key: String, val background: String, val foreground: String, val row: Int, val column: Int, val separator: Boolean, val backdrop: String, val comment: String)',
         'internal data class PaletteBlock(val title: String = "", val description: String = "", val background: String = "", val foreground: String = "", val columns: Int = 1, val tiles: List<PaletteTile> = emptyList())',
         'internal val galleryPalettes = listOf(']
for section in ['Text', 'Fill', 'Stroke', 'Background', 'Signal', 'HighContrast']:
    root = ET.parse(source / (section + 'Section.xaml')).getroot()
    lines.append('    listOf(')
    def visit(element):
        name, a = kind(element), element.attrib
        if name == 'ColorPageExample':
            values = [a.get('Title', ''), a.get('Description', ''), resource(a.get('Background', '')), resource(a.get('Foreground', ''))]
            lines.append('        PaletteBlock(' + ', '.join(map(quote, values)) + '),')
        elif name == 'Grid' and any(kind(child) == 'ColorTile' for child in element):
            columns = next((len(child) for child in element if kind(child) == 'Grid.ColumnDefinitions'), 1)
            lines.append(f'        PaletteBlock(columns = {columns}, tiles = listOf(')
            for child in element:
                if kind(child) != 'ColorTile':
                    continue
                a = child.attrib
                values = [a.get('ColorName', ''), a.get('ColorExplanation', ''), a.get('ColorBrushName', ''), resource(a.get('Background', '')), resource(a.get('Foreground', ''))]
                comment = ' '.join(' '.join(child.itertext()).split())
                lines.append('            PaletteTile(' + ', '.join(map(quote, values)) + ', ' + a.get('Grid.Row', '0') + ', ' + a.get('Grid.Column', '0') + ', ' + str(a.get('ShowSeparator', 'True') != 'False').lower() + ', ' + quote(a.get('Backdrop', '')) + ', ' + quote(comment) + '),')
            lines.append('        )),')
        elif name == 'TextBlock' and section == 'HighContrast':
            lines.append('        PaletteBlock(title = ' + quote(a.get('Text', '')) + '),')
        else:
            for child in element:
                visit(child)
    visit(root)
    lines.append('    ),')
lines.append(')')
destination.write_text('\n'.join(lines) + '\n', encoding='utf-8')
