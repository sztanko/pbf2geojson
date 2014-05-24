package org.openstreetmap.pbf2geojson.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Random;
import java.util.stream.IntStream;

import org.junit.Test;
import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.data.SimpleWay;

public class StorageUtilTest {

	@Test
	public void testLongToBytes() {
		for (long i = Long.MAX_VALUE/2-100; i < Long.MAX_VALUE/2+100; i++) {
			byte[] conv = StorageUtil.longtoBytes(i);
			long l = StorageUtil.bytesToLong(conv);
			assertEquals(l, i);
		}

	}

	@Test
	public void testByteArrays()
	{
	SimpleNode n = new SimpleNode(0.1f, 0.1f, 1l, new HashMap<String, Object>());
	byte[] target = new byte[100];
	StorageUtil.serializeNode(n, target);
	SimpleNode r = new SimpleNode(0.0f, 0.0f, 1l, new HashMap<String, Object>());
	assertNotEquals(r,n);
	StorageUtil.deserializeNode(target, r);
	assertEquals(r, n);
	}
	
	@Test
	public void testEightBytesArray()
	{
	SimpleNode n = new SimpleNode(0.1f, 0.1f, 1l, new HashMap<String, Object>());
	byte[] target = new byte[8];
	StorageUtil.serializeNode(n, target);
	SimpleNode r = new SimpleNode(0.0f, 0.0f, 1l, new HashMap<String, Object>());
	assertNotEquals(r,n);
	StorageUtil.deserializeNode(target, r);
	assertEquals(r, n);
	}
	
	@Test
	public void testSevenBytesArray()
	{
		boolean hasFailed=false;
	SimpleNode n = new SimpleNode(0.1f, 0.1f, 1l, new HashMap<String, Object>());
	byte[] target = new byte[7];
	try{
	StorageUtil.serializeNode(n, target);
	SimpleNode r = new SimpleNode(0.0f, 0.0f, 1l, new HashMap<String, Object>());
	assertNotEquals(r,n);
	StorageUtil.deserializeNode(target, r);
	assertEquals(r, n);
	}
	catch(java.nio.BufferOverflowException e)
	{
		hasFailed=true;
	}
	assertTrue(hasFailed);
	}
	
	@Test
	public void testSerdeWay()
	{
		long[] refs = {0,1,2,3,4,5,6,7,8,9};
		SimpleWay w= new SimpleWay(10,refs, 1, new HashMap<String, Object>());
		int sizes[] ={10000, 1000, 100};
		for(int size: sizes){
		byte[] b = new byte[size];
		StorageUtil.serializeWay(w, b);
		long[] refs2 = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		
		SimpleWay w2= new SimpleWay(12,refs2, 1, new HashMap<String, Object>());
		StorageUtil.deserializeWay(b, w2);
		assertEquals(w.getRefListLength(), w2.getRefListLength());
		IntStream.range(0, w.getRefListLength()).forEach(i -> assertEquals(w.getRefList()[i], w2.getRefList()[i]));
		}
		}
	
	@Test
	public void testPacking()
	{
		int testSize =100000;
		Random r = new Random(12345667l);
		for(int i =0;i<testSize;i++)
		{
			int random = r.nextInt();
			long v = StorageUtil.packInts(random, i);
			assertEquals(i, StorageUtil.getSecondInt(v));
			v = StorageUtil.packInts(i, random);
			assertEquals(random, StorageUtil.getSecondInt(v));
			
		}
			
		
	}
}

