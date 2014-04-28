package org.openstreetmap.pbf2geojson;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.java.Log;

import org.openstreetmap.pbf.BinaryParser;
import org.openstreetmap.pbf2geojson.convertors.Convertor;
import org.openstreetmap.pbf2geojson.convertors.ConvertorUtils;
import org.openstreetmap.pbf2geojson.convertors.IncrementalLong;
import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.data.SimpleWay;
import org.openstreetmap.pbf2geojson.storage.Storage;

import crosby.binary.Osmformat.DenseNodes;
import crosby.binary.Osmformat.HeaderBlock;
import crosby.binary.Osmformat.Node;
import crosby.binary.Osmformat.Relation;
import crosby.binary.Osmformat.Way;
import dimi.stats.StatsCollector;

import java.util.stream.IntStream;

@Log
public class StreamParser extends BinaryParser {
	PrintWriter out;
	Storage storage;
	Convertor convertor;
	WayClassifier classifier;

	public StreamParser(final PrintWriter out, final Storage storage, final Convertor convertor,
			final WayClassifier classifier) {
		super();
		this.out = out;
		this.storage = storage;
		this.convertor = convertor;
		this.classifier = classifier;
	}

	@Override
	protected void parse(HeaderBlock header) {
	}

	// @Override
	protected void parseDense2(DenseNodes denseNodes) {
	}

	@Override
	protected void parseDense(final DenseNodes denseNodes) {
		final StatsCollector sc = StatsCollector.getInstance();
		sc.start();
		long lastId = 0, lastLat = 0, lastLon = 0;
		int pos = 0;
		
		sc.start();

		final int size = denseNodes.getLatCount();
		final List<Integer> keyvals = denseNodes.getKeysValsList();
		sc.endSilent("parseDense - prep", 0);
		
		for (int i = 0; i < size; i++) {
			sc.start();
			
			//sc.start();
			lastLon += denseNodes.getLon(i);
			lastLat += denseNodes.getLat(i);
			lastId += denseNodes.getId(i);
			//sc.endSilent("calculating lastlonids", 0);
			
			sc.start();
			Map<String, Object> curMap = new HashMap<String, Object>(5);
			
			while (pos < keyvals.size() && keyvals.get(pos) != 0) {
				String k = this.getStringById(keyvals.get(pos++));
				String v = this.getStringById(keyvals.get(pos++));
				curMap.put(k, v);
			}
			pos++;
			sc.endSilent("parseDense - map population", curMap.size());
			
			float lon = (float)this.parseLon(lastLon);
			float lat = (float)this.parseLat(lastLat);
			
			sc.start();
			storage.setNode(lastId, lon, lat);
			sc.endSilent("Storing object", 0);
			if (classifier.isInteresting(curMap)) {
				sc.start();
				SimpleNode n = new SimpleNode(lon,lat, lastId, curMap);
				sc.endSilent("SimpleNode object population", 0);
				sc.start();
				String s = convertor.convertNode(n);
				n = null;
				sc.endSilent("Converting node to string",s.length());
				sc.start();
				this.writeNoException(s);
				sc.endSilent("writing json", s.length());
			}
			sc.endSilent("DenseNode inner cycle",0);
			
		}
		
		if (size > 0) {
			sc.end("parseDense", size);
		} else {
			sc.endSilent("parseDense empty", 0);
		}
		
	}


	@Override
	protected void parseNodes(List<Node> nodes) {
		nodes.stream().map(this::fromNode).map(storage::setNode)
				.filter(n -> classifier.isInteresting(n.getProperties())).map(convertor::convertNode)// .sequential()
				.forEach(this::writeNoException);
		
	}

	@Override
	protected void parseRelations(List<Relation> rel) {
		// log.info("There are "+rel.size()+ " relations in this block");
		// rel.get(0).
	}

	@Override
	protected void parseWays(List<Way> ways) {
		if(ways.size()==0)
			return;
		final StatsCollector sc = StatsCollector.getInstance();
		sc.start();
		ways
	    .stream()
		.map(this::fromWay)
		.map(storage::setWay)
		.filter(classifier::isInteresting)
		.map(convertor::convertWay)
		// .sequential()
		.forEach(this::writeNoException);
		sc.end("Parsed ways", ways.size());
	}

	protected void writeNoException(String str) {
		out.println(str);
		
	}

	protected Map<String, Object> getPropsForPosition(IncrementalLong position,
			List<Integer> keyvals) {
		Map<String, Object> curMap = new HashMap<String, Object>();
		int i = (int) position.getCount();
		while (i < keyvals.size() && keyvals.get(i) != 0) {
			String k = this.getStringById(keyvals.get(i++));
			String v = this.getStringById(keyvals.get(i++));
			curMap.put(k, v);
		}
		i++;
		position.incr(i - position.getCount());
		return curMap;
	}

	protected SimpleNode fromDenseNode(DenseNodes denseNodes, int i,
			Map<String, Object> props) {

		return new SimpleNode((float)this.parseLon(denseNodes.getLon(i)),
				(float)this.parseLat(denseNodes.getLat(i)), denseNodes.getId(i), props);
	}

	protected SimpleNode fromNode(Node node) {
		SimpleNode sn = new SimpleNode((float)this.parseLon(node.getLon()),
				(float)this.parseLat(node.getLat()), node.getId(),
				ConvertorUtils.getProperties(node.getKeysList(),
						node.getValsList(), this::getStringById));
		return sn;
	}

	protected SimpleWay fromWay(Way way) {
		final IncrementalLong refLong = new IncrementalLong();
		long[] coordinates = way.getRefsList().stream()
				.mapToLong(refLong::incr).toArray();
		Map<String, Object> props = ConvertorUtils.getProperties(
				way.getKeysList(), way.getValsList(), this::getStringById);
		SimpleWay w = new SimpleWay(coordinates, way.getId(), props);
		return w;
	}

	public void complete() {
	//	this.out.close();
		
	}

}
