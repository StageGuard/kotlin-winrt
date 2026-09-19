"""Translate the MIT Gallery GitHub outline into projected geometry calls offline.

Usage: python import-github-path.py <WinUI-Gallery checkout>
No markup or geometry parser is shipped with the application.
"""
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

root = ET.parse(Path(sys.argv[1]) / 'WinUIGallery/App.xaml').getroot()
path = next(node.text for node in root.iter() if node.get('{http://schemas.microsoft.com/winfx/2006/xaml}Key') == 'GitHubIconPath')
tokens = re.findall(r'[A-Za-z]|[-+]?(?:\d*\.\d+|\d+\.?\d*)(?:[eE][-+]?\d+)?', path)
lines = [
    '// Generated from the MIT WinUI Gallery GitHubIconPath; see tools/import-github-path.py.',
    'package io.github.composefluent.winrt.gallery',
    '',
    'import microsoft.ui.xaml.controls.PathIcon',
    'import microsoft.ui.xaml.media.*',
    'import windows.foundation.Point',
    'import windows.foundation.Size',
    '',
    'internal fun githubIcon() = PathIcon().apply {',
    '    data = PathGeometry().apply {',
    '        figures.add(PathFigure().apply {',
]
def number(value):
    return f'{value:.6f}'.rstrip('0').rstrip('.') + 'f'
def point(x, y):
    return f'Point({number(x)}, {number(y)})'

index = 0
x = y = 0
while index < len(tokens):
    if tokens[index].isalpha():
        command = tokens[index]
        index += 1
    absolute = command.isupper()
    if command.upper() == 'Z':
        lines.append('            isClosed = true')
        continue
    count = {'M': 2, 'C': 6, 'A': 7}[command.upper()]
    values = list(map(float, tokens[index:index + count])); index += count
    if command.upper() == 'M':
        x, y = values if absolute else [x + values[0], y + values[1]]
        lines.append(f'            startPoint = {point(x, y)}')
    elif command.upper() == 'C':
        points = [(values[i] + (0 if absolute else x), values[i + 1] + (0 if absolute else y)) for i in range(0, 6, 2)]
        lines.append('            segments.add(BezierSegment().apply { ' + '; '.join(f'point{i + 1} = {point(*p)}' for i, p in enumerate(points)) + ' })')
        x, y = points[-1]
    else:
        rx, ry, angle, large, sweep, dx, dy = values
        x, y = (dx, dy) if absolute else (x + dx, y + dy)
        direction = 'Clockwise' if sweep else 'Counterclockwise'
        lines.append(f'            segments.add(ArcSegment().apply {{ size = Size({number(rx)}, {number(ry)}); rotationAngle = {angle}; isLargeArc = {str(bool(large)).lower()}; sweepDirection = SweepDirection.{direction}; point = {point(x, y)} }})')
lines.extend(['        })', '    }', '}', ''])
destination = Path(__file__).resolve().parents[1] / 'src/winuiMain/kotlin/io/github/composefluent/winrt/gallery/GalleryGitHubIcon.kt'
destination.write_text('\n'.join(lines), encoding='utf-8')
