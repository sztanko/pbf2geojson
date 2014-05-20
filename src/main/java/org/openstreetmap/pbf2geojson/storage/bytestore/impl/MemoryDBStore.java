package org.openstreetmap.pbf2geojson.storage.bytestore.impl;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.java.Log;

import org.openstreetmap.pbf2geojson.storage.bytestore.ByteStore;

@Log
public class MemoryDBStore implements ByteStore {

	Map<Long, byte[]> map;

	public MemoryDBStore() {
		this.map = new ConcurrentHashMap<Long, byte[]>(1000000);
	}

	@Override
	public byte[] get(long ref) {
		return this.map.get(ref);
	}

	@Override
	public void set(long ref, byte[] b) {
		this.map.put(ref, b);
	}

	@Override
	public void close() throws IOException {
		this.map.clear();
		
	}

	@Override
	public void prepareForGet() {
		// TODO Auto-generated method stub
		
	}
	
}
