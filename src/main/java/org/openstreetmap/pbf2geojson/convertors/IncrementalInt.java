package org.openstreetmap.pbf2geojson.convertors;

public class IncrementalInt {
	private int l;

	public IncrementalInt(){
		this(0);
	}
	public IncrementalInt(int initialValue){
		this.l = initialValue;
	}
	
	public int incr(int i) {
		l += i;
		return l;
	}
	
	public int getCount()
	{
		return l;
	}

}