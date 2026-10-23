use jni::objects::{JClass, JString};
use jni::sys::jstring;
use jni::JNIEnv;

/// Must match package/class/method:
/// com.sally.liquidfilemanager.NativeBridge.nativeVersion()
#[no_mangle]
pub extern "system" fn Java_com_sally_liquidfilemanager_NativeBridge_nativeVersion(
    mut env: JNIEnv,
    _class: JClass,
) -> jstring {
    let message = env
        .new_string("Rust core aktif • Liquid Core 0.1.0")
        .expect("failed to allocate Java string");
    message.into_raw()
}
