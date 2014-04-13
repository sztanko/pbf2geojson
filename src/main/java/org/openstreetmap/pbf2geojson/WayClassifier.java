package org.openstreetmap.pbf2geojson;

import java.util.Map;
import java.util.function.Function;

import org.geojson.LngLatAlt;

import crosby.binary.Osmformat.Way;

public interface WayClassifier {
boolean isLineString(LngLatAlt[] coordinates, Map<String, Object> props);
}
