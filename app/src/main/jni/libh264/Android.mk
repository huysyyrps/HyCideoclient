 
LOCAL_PATH := $(call my-dir) 

#include $(CLEAR_VARS)
#LOCAL_C_INCLUDES := $(AV_DECODE_DIR)/ffmpeg
#LOCAL_LDLIBS += -L$(LOCAL_PATH) -lffmpeg
#LOCAL_LDLIBS += -llibffmpeg
#LOCAL_CFLAGS := -D__STDC_CONSTANT_MACROS
#LOCAL_C_INCLUDES += $(LOCAL_PATH)/../libffmpeg 
#-lavformat  -lavdevice -lavcodec  -lavutil -pthread  -ldl -lswscale
#LOCAL_STATIC_LIBRARIES := libavformat  libavutil libavcodec libswscale
#LOCAL_LDLIBS := -lffmpeg -llog -ljnigraphics -lz -ldl -lgcc 
#LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog

#LOCAL_MODULE    := libh264
#LOCAL_SRC_FILES := H264Android.c H264Codec.c 
#../libffmpeg/cmdutils_common_opts.h ../libffmpeg/cmdutils.h ../libffmpeg/cmdutils.c ../libffmpeg/ffmpeg.c 

#include $(BUILD_SHARED_LIBRARY)



include $(CLEAR_VARS)
LOCAL_MODULE    := avformat
LOCAL_SRC_FILES := libavformat.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := avcodec
LOCAL_SRC_FILES := libavcodec.a
include $(PREBUILT_STATIC_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE    := swscale
LOCAL_SRC_FILES := libswscale.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := avutil
LOCAL_SRC_FILES := libavutil.a
include $(PREBUILT_STATIC_LIBRARY)

#include $(CLEAR_VARS)
#LOCAL_MODULE    := ffmpeg
#LOCAL_C_INCLUDES += $(LOCAL_PATH)/../ffmpeg
#LOCAL_SRC_FILES := libffmpeg.so
#include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
#LOCAL_CFLAGS := -D__STDC_CONSTANT_MACROS -Wno-sign-compare -Wno-switch -Wno-pointer-sign -DHAVE_NEON=1
 #     -mfpu=neon -mfloat-abi=softfp -fPIC -DANDROID #这里的Cflag是照抄之前的config.sh里面的，实际可能用不到这么多

LOCAL_C_INCLUDES += $(LOCAL_PATH)/../ffmpeg2

LOCAL_SRC_FILES := H264Android.c H264Codec.c   #这个写你自己的jni文件名

LOCAL_LDLIBS += -L$(LOCAL_PATH)  -lavformat  -lavcodec  -lswscale -lavutil -llog -ljnigraphics -lz -ldl 
#这两个-L很重要， 保证程序能找到库
LOCAL_MODULE := h264
include $(BUILD_SHARED_LIBRARY)