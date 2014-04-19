package org.openstreetmap.pbf2geojson.storage.impl;


import org.mapdb.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.java.Log;

import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.data.SimpleWay;
import org.openstreetmap.pbf2geojson.storage.Storage;

@Log
public class MapDBStorage implements Storage {

	private HTreeMap<Long, SimpleNode> nodeMap;
	private HTreeMap<Long, SimpleWay> wayMap;
	
	public MapDBStorage() {
		super();
		//DB db = DBMaker.newTempHashMap();
		this.nodeMap = createTempHashMap();
		this.wayMap = createTempHashMap();
		//this.nodeMap = new HashMap<Long, SimpleNode>();
		//this.wayMap = new HashMap<Long, SimpleWay>();
	
	}

	@SuppressWarnings("rawtypes")
	private HTreeMap createTempHashMap()
	{
		return DBMaker.newTempHashMap();
		/*return
				DBMaker.newMemoryDirectDB()
                .deleteFilesAfterClose()
                .closeOnJvmShutdown()
                .transactionDisable()
                .make()
                .getHashMap("temp"); */
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
