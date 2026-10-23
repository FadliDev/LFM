# Liquid Container Archive (LCA) — Draft Specification

Status: design draft. This is not yet the implemented archive format.

## Goals
- A custom container format using a tested compression algorithm.
- Independent compressed records for random access.
- Streaming reads and writes.
- Defensive parsing and bounded resource use.
- Versioned format for future compatibility.

## Proposed layout
1. Header: magic `LCA1`, format version, flags, and index location.
2. Data records: one or more independently compressed payload records.
3. Central index: normalized relative paths, record offsets, stored sizes, original sizes, compression method, and SHA-256 digest.
4. Footer: index metadata and end marker to detect truncation.

## Initial choices
- Container extension: `.lca`
- Compression: Zstandard (Zstd)
- Endianness: little-endian
- Integrity: SHA-256 per file
- Default target record size: 1 MiB (to be benchmarked)
- Encryption: deferred until the base format is stable
- Archive updates: rewrite to a temporary file and replace only after validation

## Parser safety requirements
- Validate every offset and length before reading.
- Reject integer overflow and overlapping/invalid ranges.
- Normalize entry paths and reject absolute paths, drive prefixes, NUL bytes, and `..` traversal.
- Enforce configurable entry-count and extraction-size limits.
- Never trust sizes or paths supplied by an archive.
- Write extraction output to temporary paths and only publish completed files.
