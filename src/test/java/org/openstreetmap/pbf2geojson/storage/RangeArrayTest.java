package org.openstreetmap.pbf2geojson.storage;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.junit.Test;

public class RangeArrayTest {

	private static ByteBuffer makeBuf(int[] a)
	{
		ByteBuffer b = ByteBuffer.allocate(a.length*8);
		for(int i: a)
		{
			b.putInt(i);
			b.putInt(0);
		}
		return b;
	}
	
	@Test
	public void test() {
		int[] a = {1,2,5,6,8,9,15};
		RangeArray r = new RangeArray(makeBuf(a), a.length, 3);
		
		assertEquals(3, r.getRangeFor(6)[0]);
	}

}
