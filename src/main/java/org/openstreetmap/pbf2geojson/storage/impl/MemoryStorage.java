package org.openstreetmap.pbf2geojson.storage.impl;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.java.Log;

import org.geojson.Point;
import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.data.SimpleRelation;
import org.openstreetmap.pbf2geojson.data.SimpleWay;
import org.openstreetmap.pbf2geojson.storage.Storage;

import crosby.binary.Osmformat.Node;
import crosby.binary.Osmformat.Relation;
import crosby.binary.Osmformat.Way;
@Log
public class MemoryStorage implements Storage {

	private Map<Long, SimpleNode> nodeMap;
	private Map<Long, SimpleWay> wayMap;
	private Map<Long, SimpleRelation> relationMap;
	//private Map<Long, Long> nMap;
	
	public MemoryStorage() {
		super();
		this.nodeMap = new ConcurrentHashMap<Long, SimpleNode>(400000);//,0.12f, 32);
		//this.nMap = new ConcurrentHashMap<Long, Long>(400);//,0.12f, 32);
		this.wayMap = new ConcurrentHashMap<Long, SimpleWay>(40000);//,0.12f, 32);
		//this.nodeMap = new HashMap<Long, SimpleNode>(400000);
		//this.wayMap = new HashMap<Long, SimpleWay>();
		this.relationMap = new ConcurrentHashMap<Long, SimpleRelation>(40000);
	
	}

	
	@Override
	public SimpleWay setWay(SimpleWay way) {
		
		SimpleWay w = new SimpleWay(way.getRefList(), way.getRef(), null);
		 this.wayMap.put(way.getRef(), w);
		 return way;
		
	}

	@Override
	public SimpleWay getWay(long ref) {
		return this.wayMap.get(ref);
	}
	
	@Override
	public SimpleWay getWay(long ref, SimpleWay w) {
		return this.getWay(ref);
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
		if(sn==null)
		{
			log.warning("Node ref "+ref+" is null");
		}
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


	

	
}
