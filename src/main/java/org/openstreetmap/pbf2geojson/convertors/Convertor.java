package org.openstreetmap.pbf2geojson.convertors;

import java.util.function.Function;

import crosby.binary.Osmformat.Node;
import crosby.binary.Osmformat.Relation;
import crosby.binary.Osmformat.Way;

public interface Convertor {

	public String convertNode(Node node) throws ConvertorException;
	public String convertWay(Way way) throws ConvertorException;
	public String convertRelation(Relation rel) throws ConvertorException;
	
	
}
