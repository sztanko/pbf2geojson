package org.openstreetmap.pbf2geojson.storage.bytestore.impl;

import java.io.IOException;

import org.openstreetmap.pbf2geojson.storage.bytestore.ByteStore;

public class DummyStore implements ByteStore {
	
	public DummyStore() {
		
	}
	
	@Override
	public byte[] get(long ref) {
		return null;
	}

	@Override
	public void set(long ref, byte[] b) {
		}

	@Override
	public void close() throws IOException {
	}

	@Override
	public void prepareForGet() {
		// TODO Auto-generated method stub
		
	}

}
