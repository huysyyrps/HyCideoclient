package com.sdhy.video.client;

import java.util.ArrayList;
import java.util.List;

public class PixelList {

	private static Object lock = new Object();
	
	private List<byte[]> dataList = new ArrayList<byte[]>();
	
	public void push(byte[] buf)
	{
		synchronized(lock) {
			dataList.add(buf);
		}
	}

	public byte[] get()
	{
		byte[] buf = null;
		synchronized(lock)
		{
			
			if (dataList.size() > 0)
			{
				buf = dataList.remove(0);
			}
		
		}
		return buf;
	}
	
	public int count()
	{
		synchronized(lock)
		{
			return dataList.size();
		}
		
	}
	
	public void clear()
	{
		synchronized(lock)
		{
			dataList.clear();
		}
	}


}
