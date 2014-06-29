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
		int numBuckets = Integer.max((int)(capacity/(1024*1024*1024/16) +1), 300);
		log.info("Number of buckets is going to be "+numBuckets);
		this.numBuckets = numBuckets;
		this.stores = new ByteBufferNodeStore[numBuckets];
		for(int i=0;i<numBuckets;i++)
		this.stores[i] = new ByteBufferNodeStore((int)(capacity/numBuckets + 4000));
	}

	@Override
	public void setNode(long ref, float lon, float lat) {
		int r = (int)(ref/this.numBuckets);
		//if(r<0)
		//	log.info("Node "+ref+" is absolutely large: "+r);
		stores[(int)(ref%this.numBuckets)].setNode(r, lon, lat);
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
