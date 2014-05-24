package org.openstreetmap.pbf2geojson.storage;

import java.nio.ByteBuffer;
import java.util.Arrays;

import lombok.extern.java.Log;

@Log
public class ByteBufSort {

	private final ByteBuffer b;
	private final int recordSize;
	private final int SYSTEM_SORT_THRESHOLD = 32 * 1024 * 1024;
	
	ByteBufSort(ByteBuffer b) {
		this.b = b;
		this.recordSize = 8;
	}

	protected void sort(int maxIndex) {
		quickSort(0, maxIndex - 1);
	}

	int get(int pos) {
		return this.b.getInt(pos * recordSize);
	}

	int pivot(int firstpl, int lastpl) {
		if (firstpl >= lastpl)
			return -1;
		else
			return firstpl;
	}

	private void quickSortInternal(int low, int high) {
		long[] buf = new long[high - low + 1];
		for (int i = low; i <= high; i++) {
			// int index=i-low;
			buf[i - low] = b.getLong(i * recordSize);
			// = StorageUtil.packInts(b.getInt(i*recordSize),
			// b.getInt(i*recordSize+32));
		}

		Arrays.parallelSort(buf);
		for (int i = low; i <= high; i++) {
			b.putLong(i * recordSize, buf[i - low]);
		}
	}

	private void quickSort(int low, int high) {
		if (high - low < SYSTEM_SORT_THRESHOLD) {
			quickSortInternal(low, high);
			return;
		}
		int i = low, j = high;
		// Get the pivot element from the middle of the list
		int pivot = get(low + (high - low) / 2);

		// Divide into two lists
		while (i <= j) {
			// If the current value from the left list is smaller then the pivot
			// element then get the next element from the left list
			while (get(i) < pivot) {
				i++;
			}
			// If the current value from the right list is larger then the pivot
			// element then get the next element from the right list
			while (get(j) > pivot) {
				j--;
			}

			// If we have found a values in the left list which is larger then
			// the pivot element and if we have found a value in the right list
			// which is smaller then the pivot element then we exchange the
			// values.
			// As we are done we can increase i and j
			if (i <= j) {
				swap(i, j);
				i++;
				j--;
			}
		}
		// Recursion
		if (low < j)
			quickSort(low, j);
		if (i < high)
			quickSort(i, high);
	}

	private void getBytes(byte[] target, int pos) {
		for (int i = 0; i < target.length; i++) {
			target[i] = b.get(pos + i);
		}
	}

	private void putBytes(byte[] src, int pos) {
		for (int i = 0; i < src.length; i++) {
			b.put(pos + i, src[i]);
		}
	}

	private void swap(int i, int j) {
		final byte[] tj = new byte[recordSize];
		final byte[] ti = new byte[recordSize];

		getBytes(ti, i * recordSize);
		getBytes(tj, j * recordSize);

		putBytes(ti, j * recordSize);
		putBytes(tj, i * recordSize);
	}

	public static void sort(ByteBuffer b, int maxIndex) {
		new ByteBufSort(b).sort(maxIndex);
	}


	
	}
