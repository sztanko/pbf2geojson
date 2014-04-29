package org.openstreetmap.pbf2geojson.storage;

import org.geojson.Point;
import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.data.SimpleRelation;
import org.openstreetmap.pbf2geojson.data.SimpleWay;

import crosby.binary.Osmformat.Node;
import crosby.binary.Osmformat.Relation;
import crosby.binary.Osmformat.Way;

public interface Storage {
	SimpleNode setNode(SimpleNode node);
	SimpleNode getNode(long ref);
	void setNode(long ref, float lon, float lat);
	
	SimpleWay setWay(SimpleWay way);
	SimpleWay getWay(long ref);
	
	SimpleRelation getRelation(long ref);
	SimpleRelation setRelation(SimpleRelation ref);
	void close();
	
}
