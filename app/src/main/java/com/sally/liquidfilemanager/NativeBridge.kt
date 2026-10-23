package com.sally.liquidfilemanager

class NativeBridge {

    external fun nativeVersion(): String

    companion object {
        init {
            System.loadLibrary("liquid_core")
        }
    }
}