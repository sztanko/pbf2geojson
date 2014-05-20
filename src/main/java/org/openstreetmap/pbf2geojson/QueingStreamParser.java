package org.openstreetmap.pbf2geojson;

import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import lombok.extern.java.Log;

import org.openstreetmap.pbf2geojson.convertors.GeoJSONConvertor;
import org.openstreetmap.pbf2geojson.storage.Storage;

import crosby.binary.Osmformat;

@Log
public class QueingStreamParser extends StreamParser {
	
	final BlockingQueue<Runnable> nodesQ;
	final BlockingQueue<Runnable> waysQ;
	final BlockingQueue<Runnable> relationsQ;
	
	public QueingStreamParser(final PrintWriter out, final Storage storage,
			final GeoJSONConvertor convertor, final WayClassifier classifier, 
			final BlockingQueue<Runnable> nodesQ,
			final BlockingQueue<Runnable> waysQ,
			final BlockingQueue<Runnable> relationsQ) {
		super(out, storage,
				convertor,classifier);
		this.nodesDone=false;
		this.nodesQ = nodesQ;
		this.waysQ = waysQ;
		this.relationsQ = relationsQ;

	}
	
	 public void parse(Osmformat.PrimitiveBlock block) {
		 	this.nodesDone=true;
	        Osmformat.StringTable stablemessage = block.getStringtable();
	        strings = new String[stablemessage.getSCount()];

	        for (int i = 0; i < strings.length; i++) {
	            strings[i] = stablemessage.getS(i).toStringUtf8();
	        }

	        granularity = block.getGranularity();
	        lat_offset = block.getLatOffset();
	        lon_offset = block.getLonOffset();
	        date_granularity = block.getDateGranularity();
	    	try {
				
	        for (final Osmformat.PrimitiveGroup groupmessage : block
	                .getPrimitivegroupList()) {
	            // Exactly one of these should trigger on each loop.
	        	if (groupmessage.hasDense())
	        	{
	        		//log.info("Offering a densenode");
	        			this.nodesQ.offer(() -> parseDense(groupmessage.getDense()), 5000, TimeUnit.MILLISECONDS);
					//this.denseNodesQ.offer(()->groupmessage.getDense());
	        	}
	        	if(groupmessage.getNodesList().size()>0)
	        		this.nodesQ.offer(() -> parseNodes(groupmessage.getNodesList()), 5000, TimeUnit.MILLISECONDS);
	            if(groupmessage.getWaysList().size()>0)
	            	this.waysQ.offer(() -> parseWays(groupmessage.getWaysList()), 5000, TimeUnit.MILLISECONDS);
	            if(groupmessage.getRelationsList().size()>0)
	            	this.relationsQ.offer(()-> parseRelations(groupmessage.getRelationsList()), 5000, TimeUnit.MILLISECONDS);
	            
	        }
	    	} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	
	    }

}
