package org.openstreetmap.pbf2geojson;

import java.util.Map;

import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.data.SimpleWay;

import crosby.binary.Osmformat.Relation;


public class SimpleWayClassifier implements WayClassifier {

	@Override
	public boolean isLineString(SimpleNode[] coordinates,
			Map<String, Object> props) {
		SimpleNode fc = coordinates[0];
		SimpleNode lc = coordinates[coordinates.length-1];
		if (!(lc.getLat()==fc.getLat() && lc.getLon()==fc.getLon()))
			return true;
		if(props.containsKey("highway"))
		{
			return true;
		}
		return false;
	}
	@Override
	public boolean isInteresting(SimpleWay way) {
		return way.getProperties().size() > 0;
	}
	@Override
	public boolean isInteresting(SimpleNode node) {
		cleanupProps(node.getProperties());
		return node.getProperties().size() > 0;
				
	}
	@Override
	public boolean isInteresting(Relation rel) {
		return rel.getKeysCount() > 0;
	}
	
	protected Map<String, Object> cleanupProps(Map<String,Object> props)
	{
		props.remove("source");
		props.remove("created_by");
		return props;
	}

}
