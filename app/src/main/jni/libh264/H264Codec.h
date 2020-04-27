#ifndef H264CODEC_H
#define H264CODEC_H



enum
{
	YUV420_TO_RGB565 = 1,
	YUV420_TO_RGB888 = 2,
	YUV420_TO_RGB8888 = 3
} ;



int InitDecoder_HY(int width, int height);
int UninitDecoder_HY( int nPort);
int DecoderNal_HY(int nPort, uint8_t* in, int nalLen, uint8_t* out,int type);


#endif
