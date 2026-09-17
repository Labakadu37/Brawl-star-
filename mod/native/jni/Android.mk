LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE      := jz
LOCAL_SRC_FILES   := libjz.cpp
LOCAL_LDLIBS      := -llog
LOCAL_CPPFLAGS    := -std=c++17 -fvisibility=hidden -O2
include $(BUILD_SHARED_LIBRARY)
