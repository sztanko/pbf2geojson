package org.openstreetmap.pbf2geojson;

import crosby.binary.Osmformat.Relation;


public interface RelationClassifier {
String getType(Relation rel);
}
