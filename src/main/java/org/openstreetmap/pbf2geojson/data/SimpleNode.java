package org.openstreetmap.pbf2geojson.data;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SimpleNode implements Serializable {
	private static final long serialVersionUID = 9101186615860056358L;
	private double lon;
	private double lat;
	private long ref;
	private Map<String, Object> properties;
}
