package org.openstreetmap.pbf2geojson.storage.nodestore.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.java.Log;

import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.storage.ByteBufSort;
import org.openstreetmap.pbf2geojson.storage.nodestore.NodeStore;

@Log
public class ByteBufferNodeStore implements NodeStore{
	ByteBuffer latBuf;
	ByteBuffer lonBuf;
	ByteBuffer refBuf;
	protected int capacity;

	final AtomicInteger pos;
	
	public ByteBufferNodeStore(int capacity)
	{
		this.capacity = capacity;
		this.pos = new AtomicInteger(0);
		latBuf = ByteBuffer.allocateDirect(capacity*4);
		lonBuf = ByteBuffer.allocateDirect(capacity*4);
		refBuf = ByteBuffer.allocateDirect(capacity*8);
		
	}

	@Override
	public void setNode(long ref, float lon, float lat) {
		int i = pos.getAndIncrement();
		int offset=i*4;
		this.latBuf.putFloat(offset, lat);
		this.lonBuf.putFloat(offset, lon);
		offset*=2;
		this.refBuf.putInt(offset, (int)ref);
		this.refBuf.putInt(offset+4, i);
		
	}
	
	@Override
	public SimpleNode getNode(long ref, SimpleNode target) {
		int pos = this.searchNodeIndex((int)ref);
		if(pos==-1)
			return null;
		int offset = this.refBuf.getInt((pos << 3) +4) << 2;
		target.setRef(ref);
		target.setLat(this.latBuf.getFloat(offset));
		target.setLon(this.lonBuf.getFloat(offset));
		return target;
	}
	
	protected int getFromByte(int index)
	{
		return this.refBuf.getInt(index<<3);
	}
	
	protected int searchNodeIndex(int key)
	{
		int lo = 0;
        int hi = pos.get() - 1;
        while (lo <= hi) {
            // Key is in a[lo..hi] or not present.
            int mid = lo + (hi - lo) / 2;
            int curRef = getFromByte(mid);
            if      (key < curRef) hi = mid - 1;
            else if (key > curRef) lo = mid + 1;
            else return mid;
        }
        return -1;
 		
	}

	protected synchronized void sortRefBuffer()
	{
		ByteBufSort.sort(this.refBuf, pos.get());
		//log.info("Smallest ref is "+this.refBuf.getInt(0));
	}
	

	@Override
	public void finalizeNodes() {
		sortRefBuffer();
	}

	@Override
	public void close() {
		this.refBuf.clear();
		this.latBuf.clear();
		this.lonBuf.clear();
	}
	
}
