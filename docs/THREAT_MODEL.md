# Threat Model

Treat every archive as untrusted input.

Initial protections:
- Reject path traversal and absolute output paths.
- Bound entry count, metadata size, decompressed bytes, and per-file output.
- Check arithmetic for overflow before allocating or seeking.
- Verify integrity before reporting extraction success.
- Avoid following symlinks during extraction.
- Extract to temporary files and rename only after successful validation.
- Return structured errors instead of panicking on malformed archive input.

Encryption is out of scope for the first archive prototype. Do not invent a custom cipher.
