package org.openstreetmap.pbf2geojson.convertors;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.data.SimpleWay;
import org.openstreetmap.pbf2geojson.storage.Storage;

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
	
	
	public static  Stream<SimpleNode> retrieveNodes(SimpleWay way, Storage storage)
	{

		//if(way.getRefList()==null)
		//	return null;
		return Arrays.stream(way.getRefList()).mapToObj(storage::getNode)
				.filter(n -> n!=null);
				//.toArray(SimpleNode[]::new);
	}
}
