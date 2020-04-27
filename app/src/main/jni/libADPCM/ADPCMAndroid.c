#undef __cplusplus
#include <string.h>
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include "android/log.h"

#include "ADPCM.h"


jint Java_com_sdhy_video_client_PlayPCM_AdpcmCoder (JNIEnv* env, jobject thiz, jshortArray indata, jcharArray outdata, jlong len, jshort valprev,jchar index)
{
	jbyte * inBuf = (jbyte*)(*env)->GetByteArrayElements(env, in, 0);
	jshort * outBuf= (jshort*)(*env)->GetShortArrayElements(env, out, 0);
	jclass clazz = (jclass)(*env)->GetObjectClass(env,state);
	if (0 == clazz)
		{
		//	__android_log_print(ANDROID_LOG_INFO,"ASPCM","Getobjectclass clazz is null!");
			return (-1);
		}

		//获取类中每一个变量的定义     //valp rev
		jfieldID ID1 = (*env)->GetFieldID(env,clazz,"valprev","S");
		jfieldID ID2 = (*env)->GetFieldID(env,clazz,"index","C");

		struct adpcmState sta;
	//	sta = clazz;
		sta.valprev = (*env)->GetShortField(env, state, ID1);
		sta.index = (*env)->GetCharField(env, state, ID2 );
//		int count=adpcm_coder (short [], char [], long, struct adpcm_state *);
		int count = adpcm_coder (inBuf, outBuf, len , &sta);
		//给每一个实例的变量付值
		(*env)->SetShortField(env,state,ID1,sta.valprev);
		(*env)->SetCharField(env,state,ID2,sta.index);
		(*env)->ReleaseByteArrayElements(env, in, inBuf, 0);
		(*env)->ReleaseShortArrayElements(env, out, outBuf, 0);
		return count;
}
jint Java_com_sdhy_video_client_PlayPCM_AdpcmDecoder(JNIEnv* env, jobject thiz, jbyteArray in, jshortArray out, jint len, jobject state)
{
	//com.sdhy.video.client

	jbyte * inBuf = (jbyte*)(*env)->GetByteArrayElements(env, in, 0);
	jshort * outBuf= (jshort*)(*env)->GetShortArrayElements(env, out, 0);

	jclass clazz = (jclass)(*env)->GetObjectClass(env,state);

	if (0 == clazz)
	{
	//	__android_log_print(ANDROID_LOG_INFO,"ASPCM","Getobjectclass clazz is null!");
		return (-1);
	}

	//获取类中每一个变量的定义     //valp rev
	jfieldID ID1 = (*env)->GetFieldID(env,clazz,"valprev","S");
	jfieldID ID2 = (*env)->GetFieldID(env,clazz,"index","C");

	struct adpcmState sta;
//	sta = clazz;
	sta.valprev = (*env)->GetShortField(env, state, ID1);
	sta.index = (*env)->GetCharField(env, state, ID2 );

	//__android_log_print(ANDROID_LOG_INFO,"ASPCM","ADPCMAndroid + start; valprev =%d; index = %d;len=%d",sta.valprev,sta.index,len);

	int count = adpcm_decoder (inBuf, outBuf, len , &sta);

	//给每一个实例的变量付值
	(*env)->SetShortField(env,state,ID1,sta.valprev);
	(*env)->SetCharField(env,state,ID2,sta.index);

    //__android_log_print(ANDROID_LOG_INFO,"ASPCM","ADPCMAndroid + end;  valprev =%d; index = %d;count=%d",sta.valprev,sta.index,count);


	(*env)->ReleaseByteArrayElements(env, in, inBuf, 0);
	(*env)->ReleaseShortArrayElements(env, out, outBuf, 0);


	return count;

}
