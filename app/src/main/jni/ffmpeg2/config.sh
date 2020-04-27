#!/bin/bash
export PREBUILT=/home/wf/ndk/toolchains/arm-linux-androideabi-4.8
export PLATFORM=/home/wf/ndk/platforms/android-19/arch-arm
export TMPDIR=c:/cygwin64/tmp

./configure \
--target-os=linux \
--arch=arm \
--disable-ffmpeg \
--disable-ffplay \
--disable-ffprobe \
--disable-ffserver \
--disable-avdevice \
--disable-avfilter \
--disable-postproc \
--disable-swresample \
--disable-avresample \
--disable-symver \
--disable-debug \
--disable-stripping \
--disable-yasm \
--disable-asm \
--enable-gpl \
--enable-version3 \
--enable-nonfree \
--disable-doc \
--enable-static \
--disable-shared \
--enable-pthreads \
--enable-cross-compile \
--prefix=/home/tmp \
--cc=$PREBUILT/prebuilt/windows-x86_64/bin/arm-linux-androideabi-gcc \
--cross-prefix=$PREBUILT/prebuilt/windows-x86_64/bin/arm-linux-androideabi- \
--nm=$PREBUILT/prebuilt/windows-x86_64/bin/arm-linux-androideabi-nm \
--extra-cflags="-fPIC -DANDROID -I$PLATFORM/usr/include" \
--extra-ldflags="-L$PLATFORM/usr/lib -nostdlib"

sed -i 's/HAVE_LRINT 0/HAVE_LRINT 1/g' config.h
sed -i 's/HAVE_LRINTF 0/HAVE_LRINTF 1/g' config.h
sed -i 's/HAVE_ROUND 0/HAVE_ROUND 1/g' config.h
sed -i 's/HAVE_ROUNDF 0/HAVE_ROUNDF 1/g' config.h
sed -i 's/HAVE_TRUNC 0/HAVE_TRUNC 1/g' config.h
sed -i 's/HAVE_TRUNCF 0/HAVE_TRUNCF 1/g' config.h
sed -i 's/HAVE_CBRT 0/HAVE_CBRT 1/g' config.h
sed -i 's/HAVE_CBRTF 0/HAVE_CBRTF 1/g' config.h
sed -i 's/HAVE_ISINF 0/HAVE_ISINF 1/g' config.h
sed -i 's/HAVE_ISNAN 0/HAVE_ISNAN 1/g' config.h
sed -i 's/HAVE_SINF 0/HAVE_SINF 1/g' config.h
sed -i 's/HAVE_RINT 0/HAVE_RINT 1/g' config.h
sed -i 's/HAVE_PTHREADS 0/HAVE_PTHREADS 1/g' config.h
sed -i 's/HAVE_THREADS 0/HAVE_THREADS 1/g' config.h
sed -i 's/HAVE_PTHREAD_CANCEL 0/HAVE_PTHREAD_CANCEL 1/g' config.h
sed -i 's/CONFIG_FRAME_THREAD_ENCODER 0/CONFIG_FRAME_THREAD_ENCODER 1/g' config.h
sed -i 's/#define av_restrict restrict/#define av_restrict/g' config.h
