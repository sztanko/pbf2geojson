package org.openstreetmap.pbf2geojson.convertors;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;
import org.openstreetmap.pbf2geojson.WayClassifier;
import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.data.SimpleWay;
import org.openstreetmap.pbf2geojson.storage.Storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GeoJSONConvertor implements Convertor {
	private ObjectMapper objectMapper;
	private WayClassifier classifier;
	private Storage storage;
	
	public GeoJSONConvertor(WayClassifier classifier, Storage storage)
	{
		this.objectMapper = new ObjectMapper();
		this.classifier= classifier;
		this.storage= storage;
	}
	
	@Override
	public String convertNode(SimpleNode node) throws ConvertorException {
		Point f = new Point(node.getLon(), node.getLat());
		f.setProperties(node.getProperties());
		f.getProperties().put("id", node.getRef());
		try {
			//String json = new ObjectMapper().writeValueAsString(f);
			String json = objectMapper.writeValueAsString(f);
			return json;
		} catch (JsonProcessingException e) {
			throw new ConvertorException(e);
		}
	}

	
	@Override
	public String convertWay(SimpleWay way) throws ConvertorException {

		GeoJsonObject f = null;
		
		SimpleNode[] coordinates = ConvertorUtils.retrieveNodes(way, storage).toArray(SimpleNode[]::new);
		List<LngLatAlt> lngLats = Arrays.stream(coordinates).map(s -> new LngLatAlt(s.getLon(), s.getLat())).collect(Collectors.toList());
		if (classifier.isLineString(coordinates, way.getProperties())) {
			LineString line = new LineString();
			line.setCoordinates(lngLats);
			f = line;
		} else {
			Polygon polygon = new Polygon();
			polygon.setExteriorRing(lngLats);
			f = polygon;
		}
		f.setProperties(way.getProperties());
		f.getProperties().put("id", way.getRef());
		try {
			String json = objectMapper.writeValueAsString(f);
			//String json = new ObjectMapper().writeValueAsString(f);
			return json;
		} catch (JsonProcessingException e) {
			throw new ConvertorException(e);
		}
		
	}

}
