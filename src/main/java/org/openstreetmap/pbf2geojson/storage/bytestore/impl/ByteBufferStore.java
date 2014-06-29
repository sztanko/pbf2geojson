package org.openstreetmap.pbf2geojson.storage.bytestore.impl;

import java.nio.ByteBuffer;

import lombok.extern.java.Log;

import org.openstreetmap.pbf2geojson.storage.ByteBufSort;
import org.openstreetmap.pbf2geojson.storage.bytestore.ByteStore;

@Log
public class ByteBufferStore implements ByteStore {
	ByteBuffer buf;
	ByteBuffer refBuf;
	protected int capacity;
	int pos, offset;
	protected int[][] ranges;
	int minRef, maxRef;
	/**
	 * 
	 * @param capacity - number of ways
	 * @param numRefs number of points
	 */
	
	public ByteBufferStore(int capacity, int numRefs) {
		this.capacity = capacity;
		this.pos = 0;
		this.offset = 0;
		buf = ByteBuffer.allocateDirect(numRefs * 8 + capacity * 4);
		refBuf = ByteBuffer.allocateDirect(capacity * 8);
		//this.ranges = new int[LOOKUP_SIZE][2];
	}

	@Override
	public byte[] get(long ref) {
		int pos = this.searchNodeIndex((int) ref);
		
		if (pos == -1)
			return null;
		int offset = this.refBuf.getInt(pos * 8 + 4);
		int len = buf.getInt(offset) * 8 + 4;
		byte[] target = new byte[len];
		for (int i = 0; i < len; i++) {
			target[i] = buf.get(offset + i);
		}
		return target;
	}

	public synchronized  int[] incrementPositionAndOffset(int len)
	{
		int[] pos_offset= new int[2];
		pos_offset[0] = pos;
		pos_offset[1] = offset;
		pos++;
		offset+=len;
		return pos_offset;
	}
	
	@Override
	public void set(long ref, byte[] b) {
		final int i, offs;

		int len = ByteBuffer.wrap(b).getInt() * 8 + 4;
		if(offset+len> buf.capacity())
		{
			log.warning("It feels like we won't be able to allocate "+len+" bytes at offset "+offset+", so skiping this");
			return;
		}
		int[] pos_offset = incrementPositionAndOffset(len);
		i = pos_offset[0];
		offs = pos_offset[1];
		
		
		if (i > capacity) {
			log.info("Offset over capacity! " + i);
		}
		if (offs+len > buf.capacity()) {
			log.info("Capacity "+ buf.capacity()+" is clearly smaller then " + (offs+len));
		}
		this.refBuf.putInt(i * 8, (int) ref);
		this.refBuf.putInt(i * 8 + 4, offs);
		for (int j = 0; j < len; j++)
			this.buf.put(offs + j, b[j]);
	}

	protected int getFromByte(int index) {
		return this.refBuf.getInt(index << 3);
	}

	
	protected int searchNodeIndex(int key) {
		return this.searchNodeIndexRange(key, 0, pos-1);
	}
	
	protected int searchNodeIndexRange(int key, int lo, int hi) {
		while (lo <= hi) {
			// Key is in a[lo..hi] or not present.
			int mid = lo + (hi - lo) / 2;
			int curRef = getFromByte(mid);
			if (key < curRef)
				hi = mid - 1;
			else if (key > curRef)
				lo = mid + 1;
			else
				return mid;
		}
		return -1;

	}

	@Override
	public void close() {

	}

	@Override
	public void prepareForGet() {
		ByteBufSort.sort(this.refBuf, pos);
		//log.info("Smallest ref is " + this.refBuf.getInt(0));
		
	}

}
