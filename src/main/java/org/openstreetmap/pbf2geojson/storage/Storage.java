package org.openstreetmap.pbf2geojson.storage;

import org.geojson.Point;
import org.openstreetmap.pbf2geojson.SimpleNode;

import crosby.binary.Osmformat.Node;
import crosby.binary.Osmformat.Relation;
import crosby.binary.Osmformat.Way;

public interface Storage {
	SimpleNode setNode(SimpleNode node);
	SimpleNode getNode(Long ref);
	
	
	Way setWay(Way way);
	Way getWay(Long ref);
	
	Relation setRelation(Relation relation);
	Relation getRelation(Long ref);
	
}
