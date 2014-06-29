package org.openstreetmap.pbf2geojson.convertors;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.java.Log;

import org.apache.commons.lang3.ArrayUtils;
import org.geojson.GeoJsonObject;
import org.geojson.GeometryCollection;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.MultiLineString;
import org.geojson.MultiPolygon;
import org.geojson.Point;
import org.geojson.Polygon;
import org.openstreetmap.pbf2geojson.WayClassifier;
import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.data.SimpleRelation;
import org.openstreetmap.pbf2geojson.data.SimpleRelation.Member;
import org.openstreetmap.pbf2geojson.data.SimpleWay;
import org.openstreetmap.pbf2geojson.storage.Storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crosby.binary.Osmformat.Relation.MemberType;

@Log
public class GeoJSONConvertor implements Convertor {
	private ObjectMapper objectMapper;
	private WayClassifier classifier;
	private Storage storage;
	public static final String[] lineStringRelations = { "route", "bridge",
			"waterway", "route_master", "public_transport" };
	public static final String[] polygonRelations = { "multipolygon",
			"boundary" };
	private Set<String> lineStringRelationsSet;
	private Set<String> polygonRelationsSet;

	public GeoJSONConvertor(WayClassifier classifier, Storage storage) {
		this.objectMapper = new ObjectMapper();
		this.classifier = classifier;
		this.storage = storage;
		this.lineStringRelationsSet = new HashSet<String>(
				Arrays.asList(lineStringRelations));
		this.polygonRelationsSet = new HashSet<String>(
				Arrays.asList(polygonRelations));
	}

	@Override
	public String convertNode(SimpleNode node) throws ConvertorException {
		Point f = parseNode(node);
		return convertToString(f);

	}

	public Point parseNode(SimpleNode node) {
		Point f = new Point(node.getLon(), node.getLat());
		f.setProperties(node.getProperties());
		f.getProperties().put("id", node.getRef());
		return f;
	}

	@Override
	public String convertWay(SimpleWay way) throws ConvertorException {
		// return "";
		GeoJsonObject f = parseWay(way);
		return convertToString(f);

	}

	public GeoJsonObject parseWay(SimpleWay way) {
		GeoJsonObject f = null;

		SimpleNode[] coordinates =
		// IntStream.range(0,way.getRefListLength())
		// .mapToObj(i -> storage.get)
		ConvertorUtils.retrieveNodes(way, storage).toArray(SimpleNode[]::new);
		List<LngLatAlt> lngLats = Arrays.stream(coordinates)
				.map(s -> new LngLatAlt(s.getLon(), s.getLat()))
				.collect(Collectors.toList());
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
		return f;
	}

	@Override
	public String convertRelation(SimpleRelation rel) throws ConvertorException {

		GeoJsonObject f = parseRelation(rel);
		return convertToString(f);

	}

	protected String convertToString(GeoJsonObject f) {
		try {
			String json = objectMapper.writeValueAsString(f);
			// String json = new ObjectMapper().writeValueAsString(f);
			return json;
		} catch (JsonProcessingException e) {
			log.warning("Error with geometry " + f.getProperty("id"));
			throw new ConvertorException(e);
		}
	}

	public GeoJsonObject parseRelation(SimpleRelation rel) {
		final GeoJsonObject f;
		if (this.polygonRelationsSet.contains(rel.getType())) {
			f = this.parseAsPolygon(rel);
		} else if (this.lineStringRelationsSet.contains(rel.getType())) {
			f = this.parseAsWay(rel);
		} else
			f = this.parseAsGenericRelation(rel);
		f.setProperty("id", rel.getRef());
		return f;
	}

	public GeometryCollection parseAsGenericRelation(SimpleRelation rel) {

		final GeometryCollection collection = new GeometryCollection();
		for (Member m : rel.getMembers()) {
			if (m.getType() == MemberType.NODE) {
				if (m.getRef() < 0)
					log.info("Relation #" + rel.getRef() + " has negative ref "
							+ m.getRef());
				SimpleNode n = storage.getNode(m.getRef());
				if (n != null)
					collection.add(this.parseNode(n));
			}
			if (m.getType() == MemberType.WAY) {
				SimpleWay w = storage.getWay(m.getRef());
				if (w != null)
					collection.add(this.parseWay(w));
			}
			if (m.getType() == MemberType.RELATION) {
				SimpleRelation relation = storage.getRelation(m.getRef());
				if (relation != null)
					collection.add(this.parseRelation(relation));
			}
		}
		return collection;
	}

	protected GeoJsonObject parseAsPolygonOld(SimpleRelation rel) {
		MultiPolygon f = new MultiPolygon();
		f.setProperties(rel.getProperties());
		// List<List<LngLatAlt>> interiors = new ArrayList<List<LngLatAlt>>();
		Polygon p = null;
		for (Member m : rel.getMembers()) {
			List<LngLatAlt> coords = null;
			if (m.getType() == MemberType.WAY) {
				SimpleWay w = this.storage.getWay(m.getRef());
				if (w != null) {
					coords = ConvertorUtils.retrieveNodes(w, this.storage)
							.map(s -> new LngLatAlt(s.getLon(), s.getLat()))
							.collect(Collectors.toList());
					if (coords.size() > 0) {
						if (m.getRole().equals("outer")
								|| m.getRole().equals("")) {
							if (p != null)
								f.add(p);
							p = new Polygon();
							p.setExteriorRing(coords);
							// interiors.add(coords);
						}// ;
						else if (p != null && m.getRole().equals("inner"))
							p.addInteriorRing(coords);
					}
				}
			}

		}

		return f;
	}

	protected GeoJsonObject parseAsWay(SimpleRelation rel) {
		MultiLineString f = new MultiLineString();
		f.setProperties(rel.getProperties());
		SimpleWay w = new SimpleWay(0, new long[10000], 0,
				new HashMap<String, Object>());
		for (Member m : rel.getMembers()) {
			List<LngLatAlt> coords = null;
			if (m.getType() == MemberType.WAY) {
				SimpleWay tempw = this.storage.getWay(m.getRef(), w);

				if (tempw != null) {
					coords = ConvertorUtils.retrieveNodes(w, this.storage)
							.map(s -> new LngLatAlt(s.getLon(), s.getLat()))
							.collect(Collectors.toList());
					f.add(coords);
				}
			}

		}

		return f;
	}

	protected GeoJsonObject parseAsPolygon(SimpleRelation rel) {

		// List<List<LngLatAlt>> interiors = new ArrayList<List<LngLatAlt>>();
		MultiPolygonGenerator g = new MultiPolygonGenerator(storage);
		for (Member m : rel.getMembers()) {
			g.process(m);
		}
		MultiPolygon f = g.complete();
		f.setProperties(rel.getProperties());
		return f;
	}

	protected static class MultiPolygonGenerator {
		public static enum State {
			OUTER, INNER
		};

		State state;
		MultiPolygon f;

		Polygon p;
		Storage storage;

		TLongList refs;

		boolean isPolygonNew = true;
		boolean hasExteriror = false;

		public MultiPolygonGenerator(Storage storage) {
			state = State.OUTER;
			f = new MultiPolygon();
			this.storage = storage;
			p = new Polygon();
			refs = new TLongArrayList();
		}

		protected long[] reverse(SimpleWay w) {
			long[] refs = w.getRefList();
			ArrayUtils.reverse(refs, 0, w.getRefListLength());
			return refs;
		}

		public void process(Member m) {
			if (m.getType() != MemberType.WAY)
				return;
			SimpleWay w = this.storage.getWay(m.getRef());
			if (w == null)
				return;
			String role = m.getRole();
			switch (state) {
			case OUTER:
				if ("outer".equals(role) || "".equals(role)) {
					
					if (refs.size() > 0 && w.getRefListLength() > 0) {
						if (refs.get(refs.size() - 1) == w.getRefList()[w
								.getRefListLength() - 1]) {
							reverse(w);
						}
						long[] refsToAdd = ArrayUtils.subarray(w.getRefList(), 1, w.getRefListLength());
						refs.add(refsToAdd);
						
					}
					else
					{
						refs.add(w.getRefList(), 0, w.getRefListLength());
						
					}
					if (refs.size() > 2
							&& refs.get(0) == refs.get(refs.size() - 1)) {
						completeExterior();
						// We have closed a circle, happy to go to the inner
						// state
					}
				}
				if ("inner".equals(role)) {
					// so we now have inner ways starting, we should complete
					// the exterior ring as it is
					completeExterior();
					refs.add(w.getRefList(), 0, w.getRefListLength());
					if (refs.size() > 2
							&& refs.get(0) == refs.get(refs.size() - 1)) {
						addInterior();
					}
				}

				break;
			case INNER:
				if ("outer".equals(role) || "".equals(role)) {
					completePolygon();
					refs.add(w.getRefList(), 0, w.getRefListLength());
					if (refs.size() > 2
							&& refs.get(0) == refs.get(refs.size() - 1)) {
						completeExterior();
						// We have closed a circle, happy to go to the inner
						// state
					}
				}
				if ("inner".equals(role)) {
					refs.add(w.getRefList(), 0, w.getRefListLength());
					if (refs.size() > 2
							&& refs.get(0) == refs.get(refs.size() - 1)) {
						addInterior();
					}
				}
			}
		}

		public MultiPolygon complete() {
			if (!isPolygonNew)
				f.add(p);
			return f;
		}

		public void completePolygon() {
			f.add(p);
			p = new Polygon();
			isPolygonNew = true;
		}

		public void completeExterior() {
			List<LngLatAlt> coords = completeRef();
			p.setExteriorRing(coords);
			state = State.INNER;
			isPolygonNew = false;

		}

		public void addInterior() {

			List<LngLatAlt> coords = completeRef();
			if (isPolygonNew)
				return;
			p.addInteriorRing(coords);
			state = State.INNER;

		}

		public List<LngLatAlt> completeRef() {
			List<LngLatAlt> coords = Arrays.stream(refs.toArray())
					.mapToObj(this.storage::getNode).filter(n -> n != null)
					.map(s -> new LngLatAlt(s.getLon(), s.getLat()))
					.collect(Collectors.toList());

			refs = new TLongArrayList();
			return coords;
		}
		// public void handleWay()
	}
}
