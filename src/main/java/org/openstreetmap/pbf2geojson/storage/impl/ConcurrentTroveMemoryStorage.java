package org.openstreetmap.pbf2geojson.storage.impl;


import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongFloatHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import lombok.Data;
import lombok.extern.java.Log;

import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.data.SimpleWay;

@Log
public class ConcurrentTroveMemoryStorage extends MemoryStorage {


	
	private ThreadLocal<NodeStorage> nodes;
	
	private final TLongFloatHashMap latMap;
	private final TLongFloatHashMap lonMap;
	private static final Map<String, Object> EMPTY_MAP = new HashMap<String,Object>();
	private final Queue<NodeStorage> allNodes;
	//private final Map<Long, SimpleWay> wayMap;
	//private final Map<Long, Long> nMap;
	int wayCount=0;
	
	public ConcurrentTroveMemoryStorage() {
		super();
		
		this.allNodes = new ConcurrentLinkedQueue<NodeStorage>();
		this.nodes = new ThreadLocal<ConcurrentTroveMemoryStorage.NodeStorage>(){
			@Override
			protected NodeStorage initialValue() {
				NodeStorage ns = new NodeStorage();
				allNodes.add(ns);
				return ns;
			};
		};
		
		this.latMap = new TLongFloatHashMap(0);
		this.lonMap = new TLongFloatHashMap(0);
	
	}
	
	@Override
	public synchronized void finalizeNodes()
	{
		int c=0;
		this.nodes=null;
		for(NodeStorage ns: allNodes)
			c+=ns.getRefList().size();
		this.lonMap.ensureCapacity(c*11/10);
		this.latMap.ensureCapacity(c*11/10);
		this.lonMap.setAutoCompactionFactor(0);
		this.latMap.setAutoCompactionFactor(0);
		for(NodeStorage ns: allNodes)
		{
			TFloatArrayList latList = ns.getLatList();
			TFloatArrayList lngList = ns.getLngList();
			TLongArrayList refList = ns.getRefList();
			
			for(int i=0;i<refList.size();i++)
			{
				long ref = refList.get(i);
				
				this.latMap.put(ref, latList.getQuick(i));
				this.lonMap.put(ref, lngList.getQuick(i));
				
			}
			latList.clear();
			lngList.clear();
			refList.clear();
			
		}
		allNodes.clear();
		System.gc();
		log.info("Number of nodes in the system: "+c);
	}
	
	
	
	@Override
	public SimpleWay setWay(SimpleWay way) {
		this.wayCount++;
		return super.setWay(way);
	}

	@Override
	public synchronized void finalizeWays() {
		super.finalizeWays();
		log.info("Number of ways in the system: "+this.wayCount);
	}

	@Override
	public SimpleNode setNode(SimpleNode node) {
		
		this.nodes.get().add(node.getLat(), node.getLon(), node.getRef());
		
		return node;
	}

	
	@Override
	public void setNode(long ref, float lon, float lat) {
		this.nodes.get().add(lat, lon, ref);
	}

	@Override
	public SimpleNode getNode(long ref) {
		float lat = this.latMap.get(ref);
		if(lat==0.0)
			return null;
		return new SimpleNode(this.lonMap.get(ref), lat, ref,EMPTY_MAP);
		
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


	
	@Data
	protected  static class NodeStorage
	{

		private final TFloatArrayList latList;
		private final TFloatArrayList lngList;
		private final TLongArrayList refList;
		
		public NodeStorage()
		{
			this.latList=new TFloatArrayList(400000);
			this.lngList=new TFloatArrayList(400000);
			this.refList=new TLongArrayList(400000);
		}
		
		public void add(float lat, float lng, long ref)
		{
			this.latList.add(lat);
			this.lngList.add(lng);
			this.refList.add(ref);
		}
	}
}
