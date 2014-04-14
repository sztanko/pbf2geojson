package org.openstreetmap.pbf2geojson.storage.impl;


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
public class MemoryStorage implements Storage {

	private Map<Long, SimpleNode> nodeMap;
	private Map<Long, SimpleWay> wayMap;
	
	public MemoryStorage() {
		super();
		this.nodeMap = new ConcurrentHashMap<Long, SimpleNode>();
		this.wayMap = new ConcurrentHashMap<Long, SimpleWay>();
	}

	
	@Override
	public SimpleWay setWay(SimpleWay way) {
		
		SimpleWay w = new SimpleWay(way.getRefList(), way.getRef(), null);
		 this.wayMap.put(way.getRef(), w);
		 return way;
		
	}

	@Override
	public SimpleWay getWay(Long ref) {
		return this.wayMap.get(ref);
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
	public SimpleNode getNode(Long ref) {
		SimpleNode sn =  this.nodeMap.get(ref);
		if(sn==null)
		{
			log.warning("Node ref "+ref+" is null");
		}
		return sn;
	}


	@Override
	public void close() {
			
	}

	
}
