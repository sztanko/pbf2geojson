package org.openstreetmap.pbf2geojson.convertors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ConvertorUtils {
	public static  Map<String, Object> getProperties(List<Integer> keys,
			List<Integer> vals, Function<Integer, String> lookupFunction) {
		Map<String, Object> props = new HashMap<String, Object>();
		for (int i = 0; i < keys.size(); i++) {
			props.put(lookupFunction.apply(keys.get(i)),
					lookupFunction.apply(vals.get(i)));
		}
		return props;
	}
	
}
