package org.openstreetmap.pbf2geojson.storage.bytestore;

import java.io.IOException;

public interface ByteStore {
public void close() throws IOException;
byte[] get(long ref);
void set(long ref, byte[] b);
public void prepareForGet();
}
