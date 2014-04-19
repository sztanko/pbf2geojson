package org.openstreetmap.pbf2geojson.data;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SimpleWay implements Serializable {
	private static final long serialVersionUID = 8707652299789374342L;
	private long[] refList;
	private long ref;
	private Map<String, Object> properties;
}
