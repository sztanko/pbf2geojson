package org.openstreetmap.pbf2geojson.convertors;

public class IncrementalLong {
	private long l = 0;

	public long incr(long i) {
		l += i;
		return l;
	}
	
	public long getCount()
	{
		return l;
	}

}