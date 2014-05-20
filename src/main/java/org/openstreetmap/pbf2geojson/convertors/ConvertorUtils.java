package org.openstreetmap.pbf2geojson.convertors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Polygon;
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
	
	public static  Map<String, Object> getProperties(List<Integer> keys,
			List<Integer> vals, Function<Integer, String> lookupFunction,Map<String, Object> props) {
		props.clear();
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
		long[] refList = way.getRefList();
		return IntStream.range(0,way.getRefListLength())
				.mapToLong(i -> refList[i])
		.mapToObj(storage::getNode)
				.filter(n -> n!=null);
				//.toArray(SimpleNode[]::new);
	}
	
	public static StringBuilder propertiesToString(Map<String,Object> props, StringBuilder target)
	{
		target.append("\"properties\":{");
		Set<Entry<String, Object>> entrySet = props.entrySet();
		int s = entrySet.size();
		for(Entry<String,Object> p: entrySet)
		{
			target.append("\"");
			target.append(p.getKey());
			target.append("\":\"");
			target.append(p.getValue());
			target.append("\"");
			if(s>1)
				target.append(',');
			s--;
			
		}
		target.append("}");
		
		return target;
	}
	
	public static final long POW10[] = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};

	 protected static StringBuilder formatDouble(double val, int precision, StringBuilder sb) {
	     
	     if (val < 0) {
	         sb.append('-');
	         val = -val;
	     }
	     long exp = POW10[precision];
	     long lval = (long)(val * exp + 0.5);
	     sb.append(lval / exp).append('.');
	     long fval = lval % exp;
	     for (int p = precision - 1; p > 0 && fval < POW10[p] && fval>0; p--) {
	         sb.append('0');
	     }
	     sb.append(fval);
	     int i = sb.length()-1;
	     while(sb.charAt(i)=='0' && sb.charAt(i-1)!='.')
	     {
	    	 sb.deleteCharAt(i);
	    	 i--;
	     }
	     return sb;
	 }
	
	public static StringBuilder lineToString(LineString way, StringBuilder target){
		target.delete(0, target.length());
		target.append("{\"type\":\"LineString\",");
		propertiesToString(way.getProperties(), target);
		target.append(",\"coordinates\":[");
		List<LngLatAlt> coords = way.getCoordinates();
		int s = coords.size();
		for(LngLatAlt point: coords)
		{
			target.append('[');
			formatDouble(point.getLongitude(), 6, target);
			target.append(',');
			formatDouble(point.getLatitude(), 6, target);
			target.append(']');
			if(s>1)
				target.append(',');
			s--;
		}
		target.append(']');
		target.append('}');
		
		return target;
	}
	
	public static StringBuilder polygonToString(Polygon way, StringBuilder target){
		target.delete(0, target.length());
		target.append("{\"type\":\"Polygon\",");
		propertiesToString(way.getProperties(), target);
		target.append(",\"coordinates\":[[");
		List<LngLatAlt> coords = way.getExteriorRing();
		int s = coords.size();
		for(LngLatAlt point: coords)
		{
			target.append('[');
			formatDouble(point.getLongitude(), 6, target);
			target.append(',');
			formatDouble(point.getLatitude(), 6, target);
			target.append(']');
			if(s>1)
				target.append(',');
			s--;
		}
		target.append("]]");
		target.append('}');
		
		return target;
	}
	
	public static StringBuilder wayToString(GeoJsonObject g, StringBuilder target){
		if(g instanceof Polygon)
		{
			return polygonToString((Polygon)g, target);
		}
		else{
			return lineToString((LineString)g, target);
		}
	}
}
