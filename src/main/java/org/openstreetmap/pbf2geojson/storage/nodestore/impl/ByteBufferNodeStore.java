package org.openstreetmap.pbf2geojson.storage.nodestore.impl;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.java.Log;

import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.storage.ByteBufSort;
import org.openstreetmap.pbf2geojson.storage.RangeArray;
import org.openstreetmap.pbf2geojson.storage.nodestore.NodeStore;

@Log
public class ByteBufferNodeStore implements NodeStore{
	ByteBuffer latBuf;
	ByteBuffer lonBuf;
	ByteBuffer refBuf;
	protected int capacity;
	protected long minRef;
	protected long maxRef;
	
	protected RangeArray ranges;
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
		//4*8/9
		int[] range = ranges.getRangeFor(ref);
		//int pos = this.searchNodeIndex((int)ref);
		
		int pos = this.searchNodeIndex((int)ref, range[0], range[1]);
		//log.info("Pos is "+pos);
		if(pos==-1){
//			log.info("Range index for "+ref+" is "+rangeIndex+" - "+Arrays.toString(range) 
//				+" (["+this.refBuf.getInt(range[0]*8)+", "+this.refBuf.getInt(range[1]* 8)+"])");
			return null;
			
			}
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
		final int lo = 0;
        final int hi = pos.get()-1;
        return this.searchNodeIndex(key, lo, hi);
	}

	protected int searchNodeIndex(int key, int lo, int hi)
	{
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
		//log.info("Number of nodes is: "+pos.get());
		//log.info("Allocating a range array of size "+Integer.max(pos.get()>>10,2));
		this.ranges = new RangeArray(this.refBuf, pos.get(), Integer.max(pos.get()>>10,2));
	}

	@Override
	public void close() {
		this.refBuf.clear();
		this.latBuf.clear();
		this.lonBuf.clear();
		this.ranges = null;
	}
	
}
