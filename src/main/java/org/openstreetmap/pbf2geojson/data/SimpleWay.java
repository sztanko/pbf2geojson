package org.openstreetmap.pbf2geojson.data;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
@AllArgsConstructor
@Data public class SimpleWay {
	private long[] refList;
	private long ref;
	private Map<String,Object> properties;
}
