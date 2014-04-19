package org.openstreetmap.pbf2geojson;

import java.io.IOException;
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

import java.util.stream.IntStream;

@Log
public class StreamParser extends BinaryParser {
	Writer out;
	Storage storage;
	Convertor convertor;
	WayClassifier classifier;

	public StreamParser(Writer out, Storage storage, Convertor convertor, WayClassifier classifier) {
		super();
		this.out = out;
		this.storage = storage;
		this.convertor= convertor;
		this.classifier = classifier;
	}

	
	@Override
	protected void parse(HeaderBlock header) {
	}

	@Override
	protected void parseDense(DenseNodes denseNodes) {
		final IncrementalLong lastId = new IncrementalLong(), lastLat = new IncrementalLong(), lastLon = new IncrementalLong();
		final IncrementalLong propsPos = new IncrementalLong();
		IntStream
				.range(0, denseNodes.getLonCount())
				.mapToObj(
						i -> new SimpleNode(this.parseLon(lastLon.incr(denseNodes.getLon(i))), 
								this.parseLat(lastLat.incr(denseNodes.getLat(i))),
								lastId.incr(denseNodes.getId(i)), 
								getPropsForPosition(propsPos, denseNodes.getKeysValsList())
								//denseProps.get(i)
								))
				//.parallel()
				.map(storage::setNode)
				.filter(classifier::isInteresting)
				.map(convertor::convertNode)//.sequential()
				.forEach(this::writeNoException);
		}

		
	@Override
	protected void parseNodes(List<Node> nodes) {
		//log.info("There are " + nodes.size() + " nodes in this block");
		nodes.stream().map(this::fromNode).map(storage::setNode)
				.filter(classifier::isInteresting)
				.map(convertor::convertNode)//.sequential()
				.forEach(this::writeNoException);
	}

	@Override
	protected void parseRelations(List<Relation> rel) {
		//log.info("There are "+rel.size()+ " relations in this block");
		//rel.get(0).
	}

	
	@Override
	protected void parseWays(List<Way> ways) {
		//log.info("There are " + ways.size() + " ways in this block");
		ways
		.stream()
		//.parallelStream()
		.map(this::fromWay)
		.map(storage::setWay)
		.filter(classifier::isInteresting)
		.map(convertor::convertWay)
				//.sequential()
				.forEach(this::writeNoException);

	}

	protected void writeNoException(String str) {
		try {
			out.write(str+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected Map<String, Object> getPropsForPosition(IncrementalLong position, List<Integer> keyvals)
	{
		Map<String, Object> curMap = new HashMap<String, Object>();
		int i=(int)position.getCount();
		while (i < keyvals.size() && keyvals.get(i) != 0) {
			String k = this.getStringById(keyvals.get(i++));
			String v = this.getStringById(keyvals.get(i++));
			curMap.put(k, v);
		}
		i++;
		position.incr(i-position.getCount());
		return curMap;
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
	
	protected SimpleWay fromWay(Way way) {
		final IncrementalLong refLong = new IncrementalLong();
		long[] coordinates = way.getRefsList()
				.stream()
				.mapToLong(refLong::incr)
				.toArray();
		Map<String, Object> props = ConvertorUtils.getProperties(
				way.getKeysList(), way.getValsList(), this::getStringById);
		SimpleWay w = new SimpleWay(coordinates, way.getId(), props);
		return w;
	}


	public void complete() {
		try {
			this.out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



}
