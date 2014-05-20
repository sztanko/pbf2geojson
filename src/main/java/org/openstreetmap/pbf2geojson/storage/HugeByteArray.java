package org.openstreetmap.pbf2geojson.storage;

public interface HugeByteArray {
public long push(byte[] value);
public byte[] get(long position, byte[] target);
public void sort();
}
