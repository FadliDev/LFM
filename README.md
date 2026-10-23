# Liquid File Manager

Android-native file manager starter using Kotlin for UI and Rust for the native core.

## Current stage
Stage 1 scaffold:
- Android app shell
- JNI bridge to Rust
- GitHub Actions workflow building Rust libraries for Android and assembling a debug APK
- Draft LCA archive specification

The archive reader/writer and file operations are not implemented yet.

## Build locally

Requirements:
- JDK 17
- Android SDK with API 35 and NDK 27.2.12479018
- Rust stable
- `cargo-ndk`
- Gradle 8.11.1 or a compatible Gradle version for Android Gradle Plugin 8.9.2

Install Rust targets:
```sh
rustup target add aarch64-linux-android x86_64-linux-android
cargo install cargo-ndk --locked
```

Build native libraries:
```sh
cargo ndk --manifest-path rust-core/Cargo.toml \
  -t arm64-v8a -t x86_64 build --release \
  --out-dir app/src/main/jniLibs
```

Build APK:
```sh
gradle assembleDebug
```

If the Rust library has not been built, the APK may compile but the app will show a bridge error when launched. The GitHub Actions workflow builds Rust first.

## GitHub Actions
Workflow: `.github/workflows/android.yml`

Push the project to a GitHub repository. The workflow runs on pushes to `main`/`master`, pull requests, and manual dispatch. Download the generated APK from the run's **Artifacts** section.

## Important
This is a starter scaffold, not a complete file manager. The LCA format document is a draft; do not use it for important data until the format and parser have been implemented and tested.
