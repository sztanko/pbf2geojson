package org.openstreetmap.pbf2geojson.storage;

import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.Test;

public class ByteBufSortTest {

	@Test
	public void test() {
		int recordSize=8;
		int num = 10000;
		Random r = new Random();
		ByteBuffer b = ByteBuffer.allocateDirect(num*recordSize);
		for(int i=0;i<num;i++)
		{
			int v = r.nextInt(num*10);
			//System.out.println(v);
			b.putInt(v);
			b.putInt(i);
		}
	
		b.clear();
		for(int i=0;i<num;i++)
		{
			int v = b.getInt();
			assertTrue(v<num*10);
			b.getInt();
		}
		System.out.println("Done populating");
		ByteBufSort.sort(b, num);
		System.out.println("Done sorting");
		int prev = b.getInt(0);
		b.clear();
		for(int i=0;i<num;i++)
		{
			int v = b.getInt();
			assertTrue(prev <=v);
			b.getInt();
		}
		
	}

}
