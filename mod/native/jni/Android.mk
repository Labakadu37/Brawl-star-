LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE      := jzbrawlv2
LOCAL_SRC_FILES   := jzbrawlv2.c
LOCAL_LDLIBS      := -llog -ldl
LOCAL_CFLAGS      := -std=c11 -fvisibility=hidden -O2 -ffunction-sections -fdata-sections
LOCAL_LDFLAGS     := -Wl,--gc-sections
include $(BUILD_SHARED_LIBRARY)
