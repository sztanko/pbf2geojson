package org.openstreetmap.pbf2geojson.data;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
@AllArgsConstructor
@Data public class SimpleNode {
private double lon;
private double lat;
private long ref;
private Map<String,Object> properties;
}
