package com.sdhy.video.client;

public class adpcmState {
	
	 //private static adpcmState state = new adpcmState();
	 
	 public short  valprev;        /* Previous output value */ 
	 public char   index;          /* Index into stepsize table */ 

	 adpcmState()
	 {
		 valprev = 0;
		 index = 0;
	 }
}
