package com.sdhy.video.client;

public class PacketObject
{
	public long seqNum = 0;
	public long recvTime = 0;
	public long busCode = 0;
	public long packNum = 0;
	public long packNo = 0;

	public byte channel = 0;
	
	public int packSize = 0;
	public byte[] packBuff = null;
}
