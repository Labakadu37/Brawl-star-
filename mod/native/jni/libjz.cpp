// libjz - JZS Brawl native signature stub
// Loaded by com.jzs.brawl.JZSInit at boot.
// Kept intentionally minimal: identity + version stamp only.

#include <jni.h>
#include <android/log.h>
#include <string.h>

#define JZS_TAG      "JZS"
#define JZS_MOD_NAME "JZS Brawl"
#define JZS_VERSION  "JZS V1 (lastest)"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  JZS_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void* /*reserved*/) {
    LOGI("libjz loaded - %s %s", JZS_MOD_NAME, JZS_VERSION);
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_jzs_brawl_JZSInit_nativeVersion(JNIEnv* env, jclass /*clazz*/) {
    return env->NewStringUTF(JZS_VERSION);
}
