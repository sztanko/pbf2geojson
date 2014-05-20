package org.openstreetmap.pbf2geojson.storage.nodestore;

import org.openstreetmap.pbf2geojson.data.SimpleNode;

public interface NodeStore {
public SimpleNode getNode(long ref, SimpleNode target);
public void setNode(long ref, float lon, float lat);
public void finalizeNodes();
public void close();
}
