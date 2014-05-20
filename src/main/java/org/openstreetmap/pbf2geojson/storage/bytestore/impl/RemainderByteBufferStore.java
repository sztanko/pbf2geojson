package org.openstreetmap.pbf2geojson.storage.bytestore.impl;

import java.util.Arrays;

import lombok.extern.java.Log;

import org.openstreetmap.pbf2geojson.storage.bytestore.ByteStore;

@Log
public class RemainderByteBufferStore implements ByteStore {
	ByteBufferStore[] stores;
	public final int numBuckets;
	public RemainderByteBufferStore(int capacity, int numRefs) {
		numBuckets = (int)((numRefs * 8l + capacity*4l)/(1024*1024*1024))+1;
		this.stores = new ByteBufferStore[numBuckets];
		for(int i=0;i<numBuckets;i++)
		{
			stores[i] = new ByteBufferStore(capacity/(numBuckets), numRefs/(numBuckets));
		}
	}

	@Override
	public byte[] get(long ref) {
		return this.stores[(int)(ref%numBuckets)].get(ref%numBuckets);
	}

	
	@Override
	public void set(long ref, byte[] b) {
		this.stores[(int)(ref%numBuckets)].set(ref%numBuckets, b);
	}

	@Override
	public void close() {
		Arrays.stream(stores).parallel().forEach(s -> s.close());
	}

	@Override
	public void prepareForGet() {
		Arrays.stream(stores).parallel().forEach(s -> s.prepareForGet());
	}

}
