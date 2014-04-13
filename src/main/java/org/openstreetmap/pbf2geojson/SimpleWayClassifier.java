package org.openstreetmap.pbf2geojson;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.geojson.LngLatAlt;

import crosby.binary.Osmformat.Way;

public class SimpleWayClassifier implements WayClassifier {

	@Override
	public boolean isLineString(LngLatAlt[] coordinates,
			Map<String, Object> props) {
		LngLatAlt fc = coordinates[0];
		LngLatAlt lc = coordinates[coordinates.length-1];
		if (!(lc.getLatitude()==fc.getLatitude() && lc.getLongitude()==fc.getLongitude()))
			return true;
		if(props.containsKey("highway"))
		{
			return true;
		}
		return false;
	}

}
