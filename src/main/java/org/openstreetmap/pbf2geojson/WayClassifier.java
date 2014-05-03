package org.openstreetmap.pbf2geojson;

import java.util.Map;

import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.data.SimpleWay;

import crosby.binary.Osmformat.Relation;

public interface WayClassifier {
boolean isLineString(SimpleNode[] coordinates, Map<String, Object> props);

boolean isInteresting(SimpleWay way);

boolean isInteresting(Map<String, Object> props);

boolean isInteresting(Relation rel);
}
