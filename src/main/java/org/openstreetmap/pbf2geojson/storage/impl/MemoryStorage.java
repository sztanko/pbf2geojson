package org.openstreetmap.pbf2geojson.storage.impl;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.java.Log;

import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.data.SimpleRelation;
import org.openstreetmap.pbf2geojson.data.SimpleWay;
import org.openstreetmap.pbf2geojson.storage.Storage;
import org.xerial.snappy.Snappy;

@Log
public class MemoryStorage implements Storage {

	private Map<Long, SimpleNode> nodeMap;
	private Map<Long, byte[]> wayMap;
	private Map<Long, SimpleRelation> relationMap;
	//private Map<Long, Long> nMap;
	private static final Map<String, Object> EMPTY_MAP = new HashMap<String,Object>();
	
	
	public MemoryStorage() {
		super();
		this.nodeMap = new ConcurrentHashMap<Long, SimpleNode>(400000);//,0.12f, 32);
		//this.nMap = new ConcurrentHashMap<Long, Long>(400);//,0.12f, 32);
		this.wayMap = new ConcurrentHashMap<Long, byte[]>(40000);//,0.12f, 32);
		//this.nodeMap = new HashMap<Long, SimpleNode>(400000);
		//this.wayMap = new HashMap<Long, SimpleWay>();
		this.relationMap = new ConcurrentHashMap<Long, SimpleRelation>(40000);
	
	}

	
	@Override
	public SimpleWay setWay(SimpleWay way) {
		try {
			byte[] w = Snappy.rawCompress(way.getRefList(), way.getRefListLength());
			this.wayMap.put(way.getRef(), w);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		 return way;
		
	}

	@Override
	public SimpleWay getWay(long ref) {
		
		//long[] ways=this.wayMap.get(ref);
		byte[] wc = this.wayMap.get(ref);
		if(wc==null) return null;
		long[] ways;
		try {
			ways = Snappy.uncompressLongArray(wc);
			SimpleWay w = new SimpleWay(ways.length, ways, ref, EMPTY_MAP);
			return w;
		} catch (IOException e) {
		}
		return null;
	}
	
	@Override
	public SimpleWay getWay(long ref, SimpleWay w) {
		byte[] wc = this.wayMap.get(ref);
		if(wc==null) return null;
		int l;
		try {
			l = Snappy.rawUncompress(wc, 0, wc.length, w.getRefList(), 0);
			w.setRefListLength(l);
			return w;
		} catch (IOException e) {
		}
		return null;
	}


	@Override
	public SimpleNode setNode(SimpleNode node) {
		
		if(node.getRef()<0){
			log.warning("Trying to store negative ref:" + node.toString());
		}
		SimpleNode n = new SimpleNode(node.getLon(), node.getLat(), node.getRef(),null);	
		this.nodeMap.put(node.getRef(), n);
		return node;
	}
	
	@Override
	public void setNode(long ref, float lon, float lat) {
		
		SimpleNode n = new SimpleNode(lon, lat, ref,null);	
		this.nodeMap.put(ref, n);
	}


	@Override
	public SimpleNode getNode(long ref) {
		SimpleNode sn =  this.nodeMap.get(ref);
		if(sn==null) return sn;
		sn.setProperties(EMPTY_MAP);
		return sn;
	}
	
	@Override
	public SimpleNode getNode(long ref, SimpleNode n) {
		return getNode(ref);
	}
	
	@Override
	public SimpleRelation setRelation(SimpleRelation ref) {
		this.relationMap.put(ref.getRef(), ref);
		return ref;
	}
	
	@Override
	public SimpleRelation getRelation(long ref) {
		return this.relationMap.get(ref);
	}
	

	@Override
	public void close() {
			
	}


	@Override
	public void finalizeNodes() {
		
	}


	@Override
	public void finalizeWays() {
		
	}


	

	
}
