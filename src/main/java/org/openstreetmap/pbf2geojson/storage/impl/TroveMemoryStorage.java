package org.openstreetmap.pbf2geojson.storage.impl;


import gnu.trove.map.hash.TLongFloatHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.HashMap;

import lombok.extern.java.Log;

import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.data.SimpleRelation;
import org.openstreetmap.pbf2geojson.data.SimpleWay;
import org.openstreetmap.pbf2geojson.storage.Storage;

@Log
public class TroveMemoryStorage implements Storage {

	private final TLongFloatHashMap latMap;
	private final TLongFloatHashMap lonMap;
	//private Map<Long, SimpleNode> nodeMap;
	private final TLongObjectHashMap<long[]> wayMap;
	private final TLongObjectHashMap<SimpleRelation> relationMap;
	//private final Map<Long, SimpleWay> wayMap;
	//private final Map<Long, Long> nMap;
	
	public TroveMemoryStorage() {
		super();
		this.latMap = new TLongFloatHashMap(400000);
		this.lonMap = new TLongFloatHashMap(400000);				//ConcurrentHashMap<Long, SimpleNode>(400000);//,0.12f, 32);
		this.wayMap = new TLongObjectHashMap<long[]>(10000);//,0.12f, 32);
		
		this.relationMap = new TLongObjectHashMap<SimpleRelation>(400);
		//this.nodeMap = new HashMap<Long, SimpleNode>(400000);
		//this.wayMap = new HashMap<Long, SimpleWay>();
	
	}

	
	@Override
	public SimpleWay setWay(SimpleWay way) {
		
//		SimpleWay w = new SimpleWay(way.getRefList(), way.getRef(), null);
		this.wayMap.put(way.getRef(), way.getRefList());
		 return way;
		
	}

	@Override
	public SimpleWay getWay(long ref) {
		long[] w=this.wayMap.get(ref);
		if(w==null) return null;
		return new SimpleWay(w.length, w, ref, new HashMap<String, Object>());
	}
	
	@Override
	public SimpleWay getWay(long ref, SimpleWay w) {
		long[] ww=this.wayMap.get(ref);
		if(ww==null) return null;
		w.setRefList(ww);
		w.setRef(ref);
		w.getProperties().clear();
		return w;
	}


	@Override
	public SimpleNode setNode(SimpleNode node) {
		
		if(node.getRef()<0){
			log.warning("Trying to store negative ref:" + node.toString());
		}
		this.lonMap.put(node.getRef(), node.getLon());
		this.latMap.put(node.getRef(), node.getLat());
		return node;
	}

	
	@Override
	public void setNode(long ref, float lon, float lat) {
		this.lonMap.put(ref, lon);
		this.latMap.put(ref, lat);
		
	}

	@Override
	public SimpleNode getNode(long ref) {
		float lon = this.lonMap.get(ref);
		if(lon==0.0)
			return null;
		return new SimpleNode(lon, this.latMap.get(ref), ref,new HashMap<String, Object>());
		
		//SimpleNode sn =  this.nodeMap.get(ref);
		//if(sn==null)
		//{
		//	log.warning("Node ref "+ref+" is null");
		//}
		//return sn;
	}
	
	@Override
	public SimpleNode getNode(long ref, SimpleNode n) {
		float lon = this.lonMap.get(ref);
		if(lon==0.0)
			return null;
		n.setLon(lon);
		n.setLat(this.latMap.get(ref));
		n.setRef(ref);
		n.getProperties().clear();
		return n;		
		//SimpleNode sn =  this.nodeMap.get(ref);
		//if(sn==null)
		//{
		//	log.warning("Node ref "+ref+" is null");
		//}
		//return sn;
	}


	@Override
	public void close() {
			
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
		// TODO Auto-generated method stub
		
	}


	@Override
	public void finalizeWays() {
		// TODO Auto-generated method stub
		
	}

	
}
