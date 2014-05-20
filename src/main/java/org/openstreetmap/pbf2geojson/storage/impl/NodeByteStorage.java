package org.openstreetmap.pbf2geojson.storage.impl;

import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.storage.Storage;
import org.openstreetmap.pbf2geojson.storage.bytestore.ByteStore;
import org.openstreetmap.pbf2geojson.storage.nodestore.NodeStore;



public class NodeByteStorage extends ByteStorage implements Storage {

	NodeStore nodeStore;

	public NodeByteStorage(NodeStore nodeStore, ByteStore wayStore) {
		super(null, wayStore);
		this.nodeStore = nodeStore;

	}

	@Override
	public SimpleNode getNode(long ref, SimpleNode n) {
		n = nodeStore.getNode(ref, n);
		return n;
	}

	@Override
	public void setNode(long ref, float lon, float lat) {
		nodeStore.setNode(ref, lon, lat);
	}

	@Override
	public void finalizeNodes() {
		nodeStore.finalizeNodes();
	}

	@Override
	public void close() {
			nodeStore.close();
			super.close();
	
	}

}
