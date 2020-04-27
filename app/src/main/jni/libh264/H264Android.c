
/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <string.h>
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>

#include "H264Codec.h"
#include "H264Codec.c"
#include "H264Android.c"



//====================================================

/*
 * Class:     h264_com_VView
 * Method:    InitDecoder
 * Signature: ()I
 */

jint Java_com_sdhy_video_client_PlayView_InitDecoder(JNIEnv* env, jobject thiz, jint width, jint height)
{

	return InitDecoder_HY(width,  height);
}

/*
 * Class:     h264_com_VView
 * Method:    UninitDecoder
 * Signature: ()I
 */

jint Java_com_sdhy_video_client_PlayView_UninitDecoder(JNIEnv* env, jobject thiz,jint nPort)
{


	int n= UninitDecoder_HY( nPort);
	if (n == nPort)
	{
		return 1;
	}
	else
	{
		return 0;

	}
}

/*
 * Class:     h264_com_VView
 * Method:    DecoderNal
 * Signature: ([B[I)I
 */
int consumed_bytes[16];

jint Java_com_sdhy_video_client_PlayView_DecoderNal(JNIEnv* env, jobject thiz,jint nPort, jbyteArray in, jint nalLen, jbyteArray out ,jint type)
{
	//int consumed_bytes = 0;

	jbyte * Buf = (jbyte*)(*env)->GetByteArrayElements(env, in, 0);
	jbyte * Pixel= (jbyte*)(*env)->GetByteArrayElements(env, out, 0);
	
	consumed_bytes[nPort] = DecoderNal_HY(nPort, Buf, nalLen,Pixel,type);

    (*env)->ReleaseByteArrayElements(env, in, Buf, 0);    
    (*env)->ReleaseByteArrayElements(env, out, Pixel, 0);

	return consumed_bytes[nPort];
}
