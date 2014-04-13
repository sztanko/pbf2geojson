package org.openstreetmap.pbf2geojson.storage.impl;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.java.Log;

import org.geojson.Point;
import org.openstreetmap.pbf2geojson.SimpleNode;
import org.openstreetmap.pbf2geojson.storage.Storage;

import crosby.binary.Osmformat.Node;
import crosby.binary.Osmformat.Relation;
import crosby.binary.Osmformat.Way;
@Log
public class MemoryStorage implements Storage {

	private Map<Long, SimpleNode> nodeMap;
	private Map<Long, Way> wayMap;
	
	public MemoryStorage() {
		super();
		this.nodeMap = new ConcurrentHashMap<Long, SimpleNode>();
		this.wayMap = new ConcurrentHashMap<Long, Way>();
	}

	
	@Override
	public Way setWay(Way way) {
		 this.wayMap.put(way.getId(), way);
		 return way;
		
	}

	@Override
	public Way getWay(Long ref) {
		return this.wayMap.get(ref);
	}

	@Override
	public Relation setRelation(Relation relation) {
		return relation;
		// TODO Auto-generated method stub

	}

	@Override
	public Relation getRelation(Long ref) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public SimpleNode setNode(SimpleNode node) {
		if(node.getRef()<0){
			log.warning("Trying to store negative ref:" + node.toString());
			//throw new RuntimeException(node.toString());
		}
			this.nodeMap.put(node.getRef(), node);
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

	
}
