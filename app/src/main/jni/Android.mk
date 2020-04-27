LOCAL_PATH := $(call my-dir)

AV_DECODE_DIR := $(LOCAL_PATH)

include $(CLEAR_VARS)
#include $(AV_DECODE_DIR)/libffmpeg/Android.mk
include $(AV_DECODE_DIR)/libADPCM/Android.mk
include $(AV_DECODE_DIR)/libh264/Android.mk
#include $(AV_DECODE_DIR)/ffmpeg2/Android.mk	