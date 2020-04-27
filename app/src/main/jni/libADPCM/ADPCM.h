/* 
** adpcm.h - include file for adpcm coder. 
** 
** Version 1.0, 7-Jul-92. 
*/ 
 
struct adpcmState {
    short      valprev;        /* Previous output value 以前的输出值*/
    char       index;          /* Index into stepsize table 指数为步长的表*/
}; 
 

int adpcm_coder (short [], char [], long, struct adpcmState *);
int adpcm_decoder (char [], short [], int, struct adpcmState *);
