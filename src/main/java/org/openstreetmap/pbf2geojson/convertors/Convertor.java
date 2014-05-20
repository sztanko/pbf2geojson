package org.openstreetmap.pbf2geojson.convertors;

import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.data.SimpleRelation;
import org.openstreetmap.pbf2geojson.data.SimpleWay;

public interface Convertor {

	public String convertNode(SimpleNode node) throws ConvertorException;
	public String convertWay(SimpleWay way) throws ConvertorException;
	public String convertRelation(SimpleRelation rel) throws ConvertorException;
	
	
}
