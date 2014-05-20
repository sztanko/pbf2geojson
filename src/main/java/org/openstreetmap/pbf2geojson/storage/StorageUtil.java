package org.openstreetmap.pbf2geojson.storage;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

import lombok.extern.java.Log;

import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.data.SimpleWay;


@Log
public class StorageUtil {
	
	
	public static byte[] longtoBytes(long l) {
		return ByteBuffer.allocate(8).putLong(l).array();
	}
	
	public static long bytesToLong(byte[] source)
	{
		return ByteBuffer.wrap(source).getLong();
		
	}

	public static void serializeNode(SimpleNode node, byte[] target ){
		target = ByteBuffer.wrap(target).putFloat(node.getLat()).putFloat(node.getLon()).array();
		
	}
	
	public static void serializeNode(float lat, float lon, byte[] target ){
		target = ByteBuffer.wrap(target).putFloat(lat).putFloat(lon).array();
	}
	
	public static void deserializeNode(byte[] source, SimpleNode target)
	{
		target.setLat(ByteBuffer.wrap(source).getFloat());
		target.setLon(ByteBuffer.wrap(source).getFloat());
	}

	public static void serializeWay(SimpleWay way, byte[] target)
	{
		//ByteBuffer.allocate(way.getRefListLength()*8).
		ByteBuffer buf = ByteBuffer.wrap(target);
		int size = way.getRefListLength();
		buf.putInt(size);
		long[] refList = way.getRefList();
		for(int i=0;i<size;i++)
		{
			buf.putLong(refList[i]);
		}
	}
	
	public static void deserializeWay(byte[] source, SimpleWay target)
	{
		//ByteBuffer.allocate(way.getRefListLength()*8).
		ByteBuffer buf = ByteBuffer.wrap(source);
		int size = buf.getInt();
		if(size>100000)
		{
			log.info("Reference is "+target.getRef());
			log.info("number of elements in this way is "+size);
			log.info("This is how source looks like:\n"+Arrays.toString(source));
		}
		target.setRefListLength(size);
		long[] refList = target.getRefList();
		for(int i =0; i< size;i++)
			refList[i] = buf.getLong();
	}
	public static SimpleWay deserializeWay(long ref, byte[] source)
	{
		//ByteBuffer.allocate(way.getRefListLength()*8).
		ByteBuffer buf = ByteBuffer.wrap(source);
		int size = buf.getInt();
		SimpleWay target = new SimpleWay(size, new long[size], ref, new HashMap<String, Object>());
		long[] refList = target.getRefList();
		if(size>source.length/4)
		{
			System.out.println("Suspicious size: "+size);
		}
		for(int i =0; i< size;i++){
			refList[i] = buf.getLong();
		}
		return target;
	}
	
	public static long packInts(int n1, int n2)
	{
		long v = n1;
		v = v<<32;
		v += n2;
		return v;
	}
	
	public static int getSecondInt(long v)
	{
		v = v << 32;
		v = v >> 32;
		return (int)v;
	}
	
	
	public static void quickSort(ByteBuffer buf, int recordSizeBytes)
	{
		
	}
	
}
