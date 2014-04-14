package org.openstreetmap.pbf2geojson.convertors;

import java.util.function.Function;

import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.data.SimpleWay;

import crosby.binary.Osmformat.Node;
import crosby.binary.Osmformat.Relation;
import crosby.binary.Osmformat.Way;

public interface Convertor {

	public String convertNode(SimpleNode node) throws ConvertorException;
	public String convertWay(SimpleWay way) throws ConvertorException;
	//public String convertRelation(SimpleRelation rel) throws ConvertorException;
	
	
}
