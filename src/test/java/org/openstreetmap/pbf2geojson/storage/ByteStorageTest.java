package org.openstreetmap.pbf2geojson.storage;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;
import org.openstreetmap.pbf2geojson.data.SimpleWay;
import org.openstreetmap.pbf2geojson.storage.bytestore.impl.ByteBufferStore;
import org.openstreetmap.pbf2geojson.storage.bytestore.impl.MemoryDBStore;
import org.openstreetmap.pbf2geojson.storage.impl.ByteStorage;

public class ByteStorageTest {

	@Test
	public void test() {
		ByteStorage storage = new ByteStorage(new MemoryDBStore(),new ByteBufferStore(2,100));
		long refs[] = {1,2,3,4,5,6,7,8,9,10,11,12,13};
		SimpleWay w = new SimpleWay(10,refs, 123, new HashMap<String, Object>());
		storage.setWay(w);
		SimpleWay newWay = storage.getWay(w.getRef());
		assertEquals(w,newWay);
		
	}
	
	@Test
	public void testMulti()
	{
		int c=100000;
		ByteStorage storage = new ByteStorage(new MemoryDBStore(), new ByteBufferStore(c, 14*c));
			
		for(int i=0;i<c;i++)
		{
			long refs[] = {1,2,3,4,5,6,7,8,9,10,11,12,13};
			refs[i%refs.length]=i;
			SimpleWay w = new SimpleWay(10+i%5-2,refs, i, new HashMap<String, Object>());
			storage.setWay(w);
			SimpleWay newWay = storage.getWay(w.getRef());
			assertEquals(w,newWay);
		}
		
		for(int i=0;i<c;i++)
		{
			long refs[] = {1,2,3,4,5,6,7,8,9,10,11,12,13};
			refs[i%refs.length]=i;
			SimpleWay w = new SimpleWay(10+i%5-2,refs, i, new HashMap<String, Object>());
			SimpleWay newWay = storage.getWay(w.getRef());
			assertEquals(w,newWay);
		}
	}
	
	
	
	

}
