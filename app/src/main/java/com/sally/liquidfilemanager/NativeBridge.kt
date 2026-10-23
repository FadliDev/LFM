package com.sally.liquidfilemanager

/**
 * Minimal JNI bridge. Rust library is loaded from app/src/main/jniLibs.
 * Keep JNI method names synchronized with rust-core/src/lib.rs.
 */
class NativeBridge {
    external fun nativeVersion(): String

    companion object {
        init {
            System.loadLibrary("liquid_core")
        }
    }
}
