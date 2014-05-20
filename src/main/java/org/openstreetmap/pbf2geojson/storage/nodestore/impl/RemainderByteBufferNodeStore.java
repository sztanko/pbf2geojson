package org.openstreetmap.pbf2geojson.storage.nodestore.impl;

import java.util.Arrays;

import lombok.extern.java.Log;

import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.storage.nodestore.NodeStore;

@Log
public class RemainderByteBufferNodeStore implements NodeStore{
	protected int capacity;
	protected final ByteBufferNodeStore[] stores;
	final private int numBuckets;
	
	public RemainderByteBufferNodeStore(long capacity)
	{
		int numBuckets = (int)(capacity/(1024*1024*1024/16) +1);
		this.numBuckets = numBuckets;
		this.stores = new ByteBufferNodeStore[numBuckets];
		for(int i=0;i<numBuckets;i++)
		this.stores[i] = new ByteBufferNodeStore((int)(capacity/numBuckets + 1));
	}

	@Override
	public void setNode(long ref, float lon, float lat) {
		stores[(int)(ref%this.numBuckets)].setNode(ref/this.numBuckets, lon, lat);
	}
	
	@Override
	public SimpleNode getNode(long ref, SimpleNode target) {
		SimpleNode t =  stores[(int)(ref%this.numBuckets)].getNode(ref/this.numBuckets, target);
		target.setRef(ref);
		return t;
	}
	
	
	@Override
	public void finalizeNodes() {
		Arrays.stream(stores).parallel().forEach(t-> t.finalizeNodes());
	}

	@Override
	public void close() {
		Arrays.stream(stores).parallel().forEach(t-> t.close());

	}
	
}
