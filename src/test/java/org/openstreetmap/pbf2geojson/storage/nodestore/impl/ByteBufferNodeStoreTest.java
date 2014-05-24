package org.openstreetmap.pbf2geojson.storage.nodestore.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Test;
import org.openstreetmap.pbf2geojson.data.SimpleNode;


public class ByteBufferNodeStoreTest {

	@Test
	public void test() {
		int capacity = 10;
		ByteBufferNodeStore store = new ByteBufferNodeStore(capacity);
		for(int i=0; i< capacity;i++)
		{
			SimpleNode n= new SimpleNode(i*2.0f, i*1.1f, (long)(capacity - i), new HashMap<String, Object>());
			store.setNode(n.getRef(), n.getLon(), n.getLat());
			
		}
		store.finalizeNodes();
		for(int i=0; i< capacity;i++)
		{
			SimpleNode n= new SimpleNode(i*2.0f, i*1.1f, (long)(capacity - i), new HashMap<String, Object>());
			SimpleNode m = new SimpleNode(0f, 0f, 0, new HashMap<String, Object>());
			m = store.getNode(capacity-i,m);
			assertNotNull(m);
			assertEquals(n, m);
			
		}
	}

	@Test
	public void testRandom() {
		long seed = System.currentTimeMillis();
		int capacity = 10000;
		final Random r = new Random(seed);
		
		ByteBufferNodeStore store = new ByteBufferNodeStore(capacity);
		Map<String, Object> p = new HashMap<String, Object>();
		r.longs(1, capacity*20).distinct().limit(capacity)
		.mapToObj(l -> new SimpleNode(r.nextFloat(), r.nextFloat(), l, p))
		.forEach(n -> 	store.setNode(n.getRef(), n.getLon(), n.getLat()));
		
		store.finalizeNodes();
		final Random r2 = new Random(seed);
		
		SimpleNode target = new SimpleNode(0f, 0f, 0, new HashMap<String, Object>());
		r2.longs(1, capacity*20).distinct().limit(capacity)
		.mapToObj(l -> new SimpleNode(r2.nextFloat(), r2.nextFloat(), l, p))
		.forEach(n -> 	assertEquals(n, store.getNode(n.getRef(), target)));
		
		
	}
	
}
