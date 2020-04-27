
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
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <android/log.h>
#include <jni.h>
#include "H264Codec.h"

#include "libavcodec/thread.h"
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"



//====================================================

/*
 * Class:     h264_com_VView
 * Method:    InitDecoder
 * Signature: ()I
 */
struct CodecParam
{
    AVCodecParserContext* parserContext;
    AVCodecContext* c;
    AVFrame* frame;
    struct SwsContext *img_convert_ctx;
    int width;
    int height;
    int first;
    uint8_t*  src;
    int  inLength;

    
};

struct CodecParam param[16];
int g_index = -1;

//int g_count = 0;
//uint8_t*  g_src = NULL;
//int  g_inLength = 0 ;
int InitDecoder_HY(int width, int height)
{
	if (g_index < 0)
	{
		av_register_all();
	}
    
    AVCodec *codec = avcodec_find_decoder(AV_CODEC_ID_H264);
    if (!codec) {
        return -1;
    }

    AVCodecParserContext* avParserContext = av_parser_init(AV_CODEC_ID_H264);
    AVCodecContext *c = avcodec_alloc_context3(codec);
    //__android_log_print(ANDROID_LOG_INFO,"h264Codec","InitDecoder + codec->capabilities=%d",codec->capabilities);
    if(codec->capabilities&CODEC_CAP_TRUNCATED)
    {
        c->flags|= CODEC_FLAG_TRUNCATED;
    }

//    if(codec->capabilities&CODEC_CAP_DELAY  )
//    {
//    	c->active_thread_type |= FF_THREAD_FRAME;
//    	__android_log_print(ANDROID_LOG_INFO,"h264Codec","InitDecoder + llllllllllll";
//    }


    AVFrame * frame = av_frame_alloc();
    
	if(c)
	{
        if (frame)
        {
            g_index ++;
            if (g_index<16)
            {
            	param[g_index].first = 1;
                param[g_index].frame = frame;
                param[g_index].c = c;
                param[g_index].parserContext = avParserContext;
                param[g_index].img_convert_ctx = sws_getContext(width,
                                                                height,
                                                                AV_PIX_FMT_YUV420P,
                                                                width,
                                                                height,
                                                                AV_PIX_FMT_RGB565LE,
                                                                SWS_FAST_BILINEAR, NULL, NULL, NULL);
                param[g_index].width = width;
                param[g_index].height = height;
                param[g_index].src = NULL;
                param[g_index].inLength = 0;
                
                if (avcodec_open2(c,codec,NULL) < 0)
                {
                    return -1;
                }
                return g_index ;
            }
            else
            {

                int i = 0;
                for(i=0;i<16;i++)
                {
                    if (!param[i].c)
                    {
                    	param[i].first = 1;
                        param[i].frame= frame;
                        param[i].c = c;
                        param[i].parserContext = avParserContext;
                        param[i].img_convert_ctx = sws_getContext(width,
                                                                height,
                                                                AV_PIX_FMT_YUV420P,
                                                                width,
                                                                height,
                                                                AV_PIX_FMT_RGB565LE,
                                                                SWS_FAST_BILINEAR, NULL, NULL, NULL);
                        param[i].width = width;
                        param[i].height = height;
                        param[i].src = NULL;
                        param[i].inLength = 0;
                        
                        if (avcodec_open2(c,codec,NULL) < 0)
                        {
                            return -1;
                        }
                        break;
                    }
                    
                }
                return i ;
            }
        }

        avcodec_close(c);
	}
	else
	{
		return -1;
	}
    return -1;

}

/*
 * Class:     h264_com_VView
 * Method:    UninitDecoder
 * Signature: ()I
 */

int UninitDecoder_HY( int nPort)
{
	if(param[nPort].img_convert_ctx)
    {
        sws_freeContext(param[nPort].img_convert_ctx);
        param[nPort].img_convert_ctx = 0;
    }
	
    
	if(param[nPort].frame)
	{
        
        av_frame_free(&param[nPort].frame);
        param[nPort].frame = 0;
    
	}
    if(param[nPort].c)
    {
        
        avcodec_close(param[nPort].c);
        param[nPort].c = 0;
        
    }
    
    if (param[nPort].parserContext) {
        av_parser_close(param[nPort].parserContext);
        param[nPort].parserContext = 0;
    }
    
    param[nPort].width = 0;
    param[nPort].height = 0;
    param[nPort].src = NULL;
    param[nPort].inLength = 0;
    param[nPort].first = 1;

	return nPort;

}

/*
 * Class:     h264_com_VView
 * Method:    DecoderNal
 * Signature: ([B[I)I
 */

int DecoderNal_HY(int nPort, uint8_t* in, int nalLen, uint8_t* out,int type)
{


    if (!param[nPort].c || !param[nPort].frame)
    {
        return -1;
    }
    
    if (nalLen != 0) {
        
        param[nPort].inLength = nalLen;
        param[nPort].src = in;
    }

    int ret = -1;
    while (param[nPort].inLength > 0)
    {
    	AVPacket packet;
    	av_init_packet(&packet);
    	//uint8_t* outData;
        int parsedLength = av_parser_parse2(param[nPort].parserContext, param[nPort].c,
										&packet.data, &packet.size,
                                        param[nPort].src, param[nPort].inLength,
                                        0, 0, 0);
        param[nPort].src += parsedLength;
        param[nPort].inLength -= parsedLength;

       // __android_log_print(ANDROID_LOG_INFO,"h264Codec","decodeNal + 111111111111111");
        if (packet.size > 0)
        {
        	int got_picture = 0;

            ret = avcodec_decode_video2(param[nPort].c, param[nPort].frame, &got_picture, &packet);
//            __android_log_print(ANDROID_LOG_INFO,"h264Codec","decodeNal + 1111111111111111111111");
            if (ret > 0)
            {
//            	__android_log_print(ANDROID_LOG_INFO,"h264Codec","decodeNal + 2222222222222222");
            	if (got_picture)
            	{
//            		if (param[nPort].first)
//					{
//            			if (param[nPort].frame->key_frame == 1)
//            			{
//            				param[nPort].first = 0;
//            				//ret = -2;
//            			}
//					}

//            		__android_log_print(ANDROID_LOG_INFO,"h264Codec","decodeNal + 3333333333333333");
					if (param[nPort].img_convert_ctx)// && !param[nPort].first)
					{
		//            	yuv420_2_rgb565(out,param[nPort].frame->data[0],param[nPort].frame->data[1],param[nPort].frame->data[2], param[nPort].width, param[nPort].height, param[nPort].frame->linesize[0], param[nPort].frame->linesize[1], param[nPort].width<<1,yuv2rgb565_table,0);

						AVPicture picture;
						avpicture_alloc(&picture, AV_PIX_FMT_RGB565LE, param[nPort].width, param[nPort].height);
						int ret2 = sws_scale(param[nPort].img_convert_ctx, (const uint8_t* const*)param[nPort].frame->data, param[nPort].frame->linesize, 0, param[nPort].frame->height, picture.data, picture.linesize);
						if (ret2 > 0) {
							memcpy(out, picture.data[0], param[nPort].width*param[nPort].height*2);
//							__android_log_print(ANDROID_LOG_INFO,"h264Codec","decodeNal + 44444444444444");

						}

						avpicture_free(&picture);
						//__android_log_print(ANDROID_LOG_INFO,"h264Codec","decodeNal + 66666666666666666");
					}
					return ret;
            	}
            }
//            __android_log_print(ANDROID_LOG_INFO,"h264Codec","decodeNal + 77777777777777777");
        }
//		if (param[nPort].inLength > 0)
//		{
//			__android_log_print(ANDROID_LOG_INFO,"h264Codec","decodeNal + 555555555555555");
//			return -1001;
//		}

    }

    return -1;

	
}
