package org.openstreetmap.pbf2geojson.storage.bytestore.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.Test;
import org.openstreetmap.pbf2geojson.storage.bytestore.ByteStore;

public class ByteBufferStoreTest {

	@Test
	public void test() {
		int capacity = 40;
		int maxNodesPerWay = 100;
		//long ts = System.currentTimeMillis();
		long ts = 12345661;
		Random r = new Random(ts);
		long ref = 0;

		ByteStore store = new ByteBufferStore(capacity, capacity
				* (maxNodesPerWay+1));
		for (int i = 0; i < capacity; i++) {
			ref += r.nextInt(10) + 1;
			//System.out.println(ref);
			Random refR = new Random(ref);
			byte[] bytes = new byte[maxNodesPerWay * 8 + 4];
			ByteBuffer buf = ByteBuffer.wrap(bytes);
			int l = refR.nextInt(maxNodesPerWay)+1;
			buf.putInt(l);
			//System.out.println("\tlength: "+l);
			for (int j = 0; j < l; j++) {
				int num = refR.nextInt(capacity)+1;
				//System.out.println("\t"+num);
				buf.putInt(num);
			}
			store.set(ref, bytes);
		}

		store.prepareForGet();

		r = new Random(ts);
		ref = 0;
		for (int i = 0; i < capacity; i++) {
			ref += r.nextInt(10) + 1;
			//System.out.println(ref);
			Random refR = new Random(ref);

			byte[] bytes = store.get(ref);
			ByteBuffer buf = ByteBuffer.wrap(bytes);
			int l = refR.nextInt(maxNodesPerWay)+1;
			assertTrue(bytes.length >= l * 8 + 4);
			assertEquals(l, buf.getInt());

			for (int j = 0; j < l; j++) {
				assertEquals(refR.nextInt(capacity)+1, buf.getInt());
			}

		}
	}

}
