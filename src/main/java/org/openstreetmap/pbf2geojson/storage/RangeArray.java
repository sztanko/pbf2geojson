package org.openstreetmap.pbf2geojson.storage;

import java.nio.ByteBuffer;

import lombok.extern.java.Log;

@Log
public class RangeArray {

	final int minRef;
	final int maxRef;
	final int maxIndex;
	final int numRanges;
	final int[][] ranges;

	public RangeArray(ByteBuffer b, int maxIndex, int numRanges) {
		this.minRef = b.getInt(0);
		this.maxRef = b.getInt(maxIndex * 8 - 8) + 1;
		this.numRanges = numRanges;
		this.ranges = new int[numRanges][2];
		this.maxIndex = maxIndex;
		//log.info("NumRanges is "+numRanges);
		populateRanges(b);
	}

	protected void populateRanges(ByteBuffer b) {
		//log.info("MinRef: " + minRef + ", maxRef: " + maxRef);
		//log.info("Lookup interval is " + (maxRef - minRef) / numRanges);
		for (int i = 0; i < numRanges; i++) {
			ranges[i][0] = -1;
			ranges[i][1] = -1;
		}
		for (int i = 0; i < this.maxIndex; i++) {
			int v = b.getInt(i * 8);
			int rangeIndex = (int)((long)numRanges * (v - minRef) / (maxRef - minRef));
			if (ranges[rangeIndex][0] == -1)
				ranges[rangeIndex][0] = i;
			if (ranges[rangeIndex][1] < i)
				ranges[rangeIndex][1] = i;
		}
	}

	public int[] getRangeFor(long ref) {
		long rangeIndex = numRanges * (ref - minRef) / (maxRef - minRef);
		return ranges[(int)rangeIndex];
	}

}
