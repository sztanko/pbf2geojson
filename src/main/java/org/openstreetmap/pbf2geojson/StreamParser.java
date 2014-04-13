package org.openstreetmap.pbf2geojson;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.java.Log;

import org.geojson.GeoJsonObject;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;
import org.openstreetmap.pbf2geojson.convertors.ConvertorException;
import org.openstreetmap.pbf2geojson.convertors.ConvertorUtils;
import org.openstreetmap.pbf2geojson.storage.Storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import crosby.binary.BinaryParser;
import crosby.binary.Osmformat.DenseNodes;
import crosby.binary.Osmformat.HeaderBlock;
import crosby.binary.Osmformat.Node;
import crosby.binary.Osmformat.Relation;
import crosby.binary.Osmformat.Way;

import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Log
public class StreamParser extends BinaryParser {
	Writer out;
	Storage storage;
	WayClassifier classifier;

	public StreamParser(Writer out, Storage storage, WayClassifier classifier) {
		super();
		this.out = out;
		// .setLookupFunction();
		this.storage = storage;
		this.classifier = classifier;
	}

	protected void writeNoException(String str) {
		try {
			out.write(str);
			out.write('\n');
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void parse(HeaderBlock header) {
	}

	@Override
	protected void parseDense(DenseNodes denseNodes) {
		log.info("There are " + denseNodes.getIdCount()
				+ " nodes in this dense node");
		log.info("Keyvals original size: " + denseNodes.getKeysValsCount());
		List<Map<String, Object>> denseProps = getPropsFromDenseKeyVals(denseNodes
				.getKeysValsList());
		log.info("Keyvals:" + denseProps.size());

		final IncrementalLong lastId = new IncrementalLong(), lastLat = new IncrementalLong(), lastLon = new IncrementalLong();

		IntStream
				.range(0, denseNodes.getLonCount())
				.mapToObj(
						i -> new SimpleNode(this.parseLon(lastLon
								.incr(denseNodes.getLon(i))), this
								.parseLat(lastLat.incr(denseNodes.getLat(i))),
								lastId.incr(denseNodes.getId(i)), denseProps
										.get(i))).parallel()
				.map(storage::setNode).filter(this::isInteresting)
				.map(this::convertNode).sequential()
				.forEach(this::writeNoException);
		// .mapToObj(i -> new LngLatAlt());
		// Stream.iterate(seed, f)
	}

	protected List<Map<String, Object>> getPropsFromDenseKeyVals(
			List<Integer> keyvals) {
		List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();

		int i = 0;
		while (i < keyvals.size()) {
			Map<String, Object> curMap = new HashMap<String, Object>();
			out.add(curMap);
			while (i < keyvals.size() && keyvals.get(i) != 0) {
				String k = this.getStringById(keyvals.get(i++));
				String v = this.getStringById(keyvals.get(i++));
				curMap.put(k, v);
			}
			i++;
		}
		return out;
	}

	protected SimpleNode fromDenseNode(DenseNodes denseNodes, int i,
			Map<String, Object> props) {

		return new SimpleNode(this.parseLon(denseNodes.getLon(i)),
				this.parseLat(denseNodes.getLat(i)), denseNodes.getId(i), props);
	}

	protected SimpleNode fromNode(Node node) {
		SimpleNode sn = new SimpleNode(this.parseLon(node.getLon()),
				this.parseLat(node.getLat()), node.getId(),
				ConvertorUtils.getProperties(node.getKeysList(),
						node.getValsList(), this::getStringById));
		return sn;
	}

	protected String convertNode(SimpleNode node) throws ConvertorException {
		Point f = new Point(node.getLon(), node.getLat());
		f.setProperties(node.getProperties());
		try {
			String json = new ObjectMapper().writeValueAsString(f);
			return json;
		} catch (JsonProcessingException e) {
			throw new ConvertorException(e);
		}
	}

	@Override
	protected void parseNodes(List<Node> nodes) {
		log.info("There are " + nodes.size() + " nodes in this block");
		nodes.parallelStream().map(this::fromNode).map(storage::setNode)
				// .filter(this::isInteresting)
				.map(this::convertNode).sequential()
				.forEach(this::writeNoException);
	}

	@Override
	protected void parseRelations(List<Relation> rel) {
		log.info("There are "+rel.size()+ " relations in this block");
		//rel.get(0).
	}

	protected String convertWay(Way way) throws ConvertorException {
		final IncrementalLong refLong = new IncrementalLong();

		LngLatAlt[] coordinates = (LngLatAlt[]) way.getRefsList().stream()
				.map(ref -> storage.getNode(refLong.incr(ref)))
				.filter(n -> n != null)
				.map(n -> new LngLatAlt(n.getLon(), n.getLat()))
				.toArray(LngLatAlt[]::new);
		GeoJsonObject f = null;
		Map<String, Object> props = ConvertorUtils.getProperties(
				way.getKeysList(), way.getValsList(), this::getStringById);
		if (classifier.isLineString(coordinates, props)) {
			LineString line = new LineString();
			line.setCoordinates(Arrays.asList(coordinates));
			f = line;
		} else {
			Polygon polygon = new Polygon();
			polygon.setExteriorRing(Arrays.asList(coordinates));
			f = polygon;
		}
		f.setProperties(props);

		try {
			String json = new ObjectMapper().writeValueAsString(f);
			return json;
		} catch (JsonProcessingException e) {
			throw new ConvertorException(e);
		}
	}

	@Override
	protected void parseWays(List<Way> ways) {
		log.info("There are " + ways.size() + " ways in this block");
		ways.parallelStream().map(storage::setWay).filter(this::isInteresting)
				.map(this::convertWay).sequential()
				.forEach(this::writeNoException);

	}

	protected boolean isInteresting(Way way) {
		return way.getKeysCount() > 0;
	}

	protected boolean isInteresting(SimpleNode node) {
		return node.getProperties().size() > 1
				|| (node.getProperties().size() == 1 && !node.getProperties()
						.containsKey("source"));
	}

	protected boolean isInteresting(Relation rel) {
		return rel.getKeysCount() > 0;
	}

	public void complete() {

	}

	static protected class IncrementalLong {
		long l = 0;

		public long incr(long i) {
			l += i;
			return l;
		}

	}

}
