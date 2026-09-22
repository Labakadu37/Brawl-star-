#ifndef _JNI_H_
#define _JNI_H_

#include <stdint.h>

typedef int32_t  jint;
typedef int64_t  jlong;
typedef uint8_t  jboolean;

typedef void* jobject;

struct JNINativeInterface;
typedef const struct JNINativeInterface* JNIEnv;

struct JNIInvokeInterface;
typedef const struct JNIInvokeInterface* JavaVM;

#endif
