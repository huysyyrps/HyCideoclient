LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog

LOCAL_MODULE    := libADPCM
LOCAL_SRC_FILES := ADPCM.c ADPCMAndroid.c

include $(BUILD_SHARED_LIBRARY)
