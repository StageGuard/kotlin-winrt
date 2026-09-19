"""Import the MIT WinUI Gallery icon catalog into platform-neutral Kotlin data.

Usage: python import-icon-data.py <WinUI-Gallery checkout>
Only data is read; no XAML or runtime font assets are bundled.
"""
import json
from pathlib import Path
import sys

source = Path(sys.argv[1]) / 'WinUIGallery/Samples/Iconography/IconsData.json'
rows = json.loads(source.read_text(encoding='utf-8-sig'))
destination = Path(__file__).resolve().parents[1] / 'src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/GalleryIconData.kt'

def quoted(value):
    return json.dumps(value, ensure_ascii=True).replace('$', '\\$')

lines = [
    '// Generated from the MIT-licensed WinUI Gallery IconsData.json; see tools/import-icon-data.py.',
    'package io.github.composefluent.winrt.gallery',
    '',
    'internal data class GalleryIcon(val name: String, val code: String, val character: String, val fluentOnly: Boolean, val tags: List<String>)',
    '',
    'internal val galleryIcons: List<GalleryIcon> by lazy { buildList {',
]
for chunk in range((len(rows) + 95) // 96):
    lines.append(f'    addAll(iconData{chunk}())')
lines.extend(['} }', ''])
for chunk, start in enumerate(range(0, len(rows), 96)):
    lines.append(f'private fun iconData{chunk}() = listOf(')
    for row in rows[start:start + 96]:
        tags = ', '.join(quoted(tag) for tag in row.get('Tags', []))
        values = [quoted(row['Name']), quoted(row['Code']), quoted(chr(int(row['Code'], 16))), str(row.get('IsSegoeFluentOnly', False)).lower(), f'listOf({tags})']
        lines.append('    GalleryIcon(' + ', '.join(values) + '),')
    lines.extend([')', ''])
destination.write_text('\n'.join(lines), encoding='utf-8')
print(f'Imported {len(rows)} icon records.')
