#!/usr/bin/env python3
"""
Count tokens, lines, and characters for:
  html, css, ts, tsx, kt, md
while skipping:
  backend\src\main\resources\static
  any node_modules directory or subdirectory
  frontend\build
  website\build
  backend\target
"""
import os
import sys
import tiktoken
from collections import defaultdict

# ---------- CONFIG ----------
EXTENSIONS = {".html", ".css", ".ts", ".tsx", ".kt", ".md"}
ENCODING_NAME = "cl100k_base"      # GPT-3.5 / GPT-4

# Paths to exclude (any entry contained in the absolute path triggers skip)
EXCLUDE_FRAGS = [
    "node_modules",
    r"backend\src\main\resources\static",
    r"frontend\build",
    r"website\build",
    r"backend\target",
    r"frontend\public\libcss",
    r"test\playwright-report",
    r"website\.docusaurus",
    r"website\build",
    ".obsidian"
]
# ----------------------------

enc = tiktoken.get_encoding(ENCODING_NAME)

tokens_by_ext = defaultdict(int)
lines_by_ext  = defaultdict(int)
chars_by_ext  = defaultdict(int)
files_by_ext  = defaultdict(int)   # NEW: file counter

def should_skip(path: str) -> bool:
    norm = os.path.abspath(path)
    return any(frag in norm for frag in EXCLUDE_FRAGS)

def process(file_path: str, ext: str) -> None:
    try:
        with open(file_path, "rb") as f:
            raw = f.read()
        text = raw.decode("utf-8", errors="ignore")
    except Exception as e:
        print(f"Skipping {file_path}: {e}", file=sys.stderr)
        return

    tokens_by_ext[ext] += len(enc.encode(text))
    lines_by_ext[ext]  += text.count("\n") + (1 if text and text[-1] != "\n" else 0)
    chars_by_ext[ext]  += len(raw)
    files_by_ext[ext]  += 1            # NEW: count this file

for root, dirs, files in os.walk(".", topdown=True):
    # Remove excluded subdirectories from the walk in-place
    dirs[:] = [d for d in dirs if not should_skip(os.path.join(root, d))]

    if should_skip(root):
        continue

    for name in files:
        ext = next((e for e in EXTENSIONS if name.lower().endswith(e)), None)
        if ext:
            process(os.path.join(root, name), ext)

# ---------- REPORT ----------
print("Extension  Files   Tokens    Lines   Chars")
print("--------- ------ -------- -------- --------")
for ext in sorted(tokens_by_ext):
    print(f"{ext:9} {files_by_ext[ext]:6} "
          f"{tokens_by_ext[ext]:8} {lines_by_ext[ext]:8} {chars_by_ext[ext]:8}")
print("--------- ------ -------- -------- --------")
print(f"TOTAL     {sum(files_by_ext.values()):6} "
      f"{sum(tokens_by_ext.values()):8} "
      f"{sum(lines_by_ext.values()):8} {sum(chars_by_ext.values()):8}")