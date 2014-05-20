package org.openstreetmap.pbf2geojson.storage.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLongArray;

import lombok.extern.java.Log;

import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.data.SimpleRelation;
import org.openstreetmap.pbf2geojson.data.SimpleWay;
import org.openstreetmap.pbf2geojson.storage.Storage;
import org.openstreetmap.pbf2geojson.storage.StorageUtil;
import org.openstreetmap.pbf2geojson.storage.bytestore.ByteStore;
import org.openstreetmap.pbf2geojson.storage.nodestore.NodeStore;

@Log
public class ByteStorage implements Storage, NodeStore {

	private Map<Long, SimpleRelation> relationMap;
	protected ByteStore nodeStore, wayStore;
	private AtomicLongArray distribution = new AtomicLongArray(4000);

	public ByteStorage(ByteStore nodeStore, ByteStore wayStore) {
		this.nodeStore = nodeStore;
		this.wayStore = wayStore;
		this.relationMap = new ConcurrentHashMap<Long, SimpleRelation>(10000);
	}

	@Override
	public SimpleNode setNode(SimpleNode node) {
		this.setNode(node.getRef(), node.getLon(), node.getLat());
		return node;
	}

	@Override
	public SimpleNode getNode(long ref) {
		SimpleNode n = new SimpleNode(0, 0, 0, new HashMap<String, Object>());
		return getNode(ref, n);
	}

	@Override
	public SimpleNode getNode(long ref, SimpleNode n) {

		byte[] b = nodeStore.get(ref);
		if (b == null)
			return null;
		StorageUtil.deserializeNode(b, n);
		return n;
	}

	@Override
	public void setNode(long ref, float lon, float lat) {
		// this.distribution.getAndIncrement((int)(ref/1000000));
		byte[] b = new byte[16];
		StorageUtil.serializeNode(lat, lon, b);
		nodeStore.set(ref, b);
	}

	@Override
	public SimpleWay setWay(SimpleWay way) {
		// throw new RuntimeException("YOu shouldnt really use this");
		byte[] b = new byte[way.getRefListLength() * 8 + 4];
		StorageUtil.serializeWay(way, b);
		wayStore.set(way.getRef(), b);
		return way;
	}

	@Override
	public SimpleWay getWay(long ref) {
		byte[] b = wayStore.get(ref);
		if (b == null)
			return null;
		return StorageUtil.deserializeWay(ref, b);
	}

	@Override
	public SimpleWay getWay(long ref, SimpleWay w) {
		byte[] b = wayStore.get(ref);
		if (b == null)
			return null;
		w.setRef(ref);
		StorageUtil.deserializeWay(b, w);
		w.getRefList();
		return w;
	}

	@Override
	public SimpleRelation getRelation(long ref) {
		return this.relationMap.get(ref);
	}

	@Override
	public SimpleRelation setRelation(SimpleRelation ref) {
		this.relationMap.put(ref.getRef(), ref);
		return ref;
	}

	@Override
	public void finalizeNodes() {
		this.nodeStore.prepareForGet();
	}

	@Override
	public void finalizeWays() {
		this.wayStore.prepareForGet();
	}

	@Override
	public void close() {
		try {
			if (nodeStore != null)
				nodeStore.close();
			if (wayStore != null)
				wayStore.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
