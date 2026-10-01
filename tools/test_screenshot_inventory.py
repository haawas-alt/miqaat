import tempfile
import unittest
from pathlib import Path
import struct
import zlib

def save_png(path, width, height):
    def chunk(kind, data):
        return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data))
    path.write_bytes(b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 2, 0, 0, 0)) + chunk(b"IDAT", zlib.compress((b"\0" + bytes(width * 3)) * height)) + chunk(b"IEND", b""))
from check_screenshot_inventory import expected_names, validate

class InventoryTests(unittest.TestCase):
    def fixture(self, root):
        for name in expected_names('phone-landscape', 'a'):
            save_png(root / name, 10, 5)
    def test_complete_inventory(self):
        with tempfile.TemporaryDirectory() as d:
            root = Path(d); self.fixture(root)
            self.assertEqual(40, validate(root, 'phone-landscape', 'a')['count'])
    def test_missing_capture_fails(self):
        with tempfile.TemporaryDirectory() as d:
            root = Path(d); self.fixture(root)
            next(root.glob('*.png')).unlink()
            with self.assertRaisesRegex(ValueError, 'Missing'): validate(root, 'phone-landscape', 'a')
    def test_wrong_orientation_fails(self):
        with tempfile.TemporaryDirectory() as d:
            root = Path(d); self.fixture(root)
            save_png(next(root.glob('home*.png')), 5, 10)
            with self.assertRaisesRegex(ValueError, 'Wrong orientation'): validate(root, 'phone-landscape', 'a')
    def test_capture_error_fails(self):
        with tempfile.TemporaryDirectory() as d:
            root = Path(d); self.fixture(root); (root / '_errors.txt').write_text('capture failed')
            with self.assertRaisesRegex(ValueError, 'Capture error'): validate(root, 'phone-landscape', 'a')
    def test_full_matrix_count(self):
        self.assertEqual(992, sum(len(expected_names(d, g)) for d in ('phone-portrait', 'phone-landscape', 'tablet-portrait', 'tablet-landscape') for g in 'abcd'))

if __name__ == '__main__': unittest.main()
