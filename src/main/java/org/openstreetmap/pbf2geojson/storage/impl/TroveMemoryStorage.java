package org.openstreetmap.pbf2geojson.storage.impl;


import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongFloatHashMap;
import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.java.Log;

import org.geojson.Point;
import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.data.SimpleWay;
import org.openstreetmap.pbf2geojson.storage.Storage;

import crosby.binary.Osmformat.Node;
import crosby.binary.Osmformat.Relation;
import crosby.binary.Osmformat.Way;
@Log
public class TroveMemoryStorage implements Storage {

	private final TLongFloatHashMap latMap;
	private final TLongFloatHashMap lonMap;
	//private Map<Long, SimpleNode> nodeMap;
	private final TLongObjectHashMap<long[]> wayMap;
	//private final Map<Long, SimpleWay> wayMap;
	//private final Map<Long, Long> nMap;
	
	public TroveMemoryStorage() {
		super();
		this.latMap = new TLongFloatHashMap(400000);
		this.lonMap = new TLongFloatHashMap(400000);				//ConcurrentHashMap<Long, SimpleNode>(400000);//,0.12f, 32);
		this.wayMap = new TLongObjectHashMap<long[]>(400);//,0.12f, 32);
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
		return new SimpleWay(this.wayMap.get(ref), ref, null);
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
		return new SimpleNode(this.lonMap.get(ref), this.latMap.get(ref), ref,null);
		
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

	
}
