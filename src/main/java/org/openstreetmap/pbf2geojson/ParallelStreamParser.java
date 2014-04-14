package org.openstreetmap.pbf2geojson;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.java.Log;

import org.openstreetmap.pbf2geojson.convertors.Convertor;
import org.openstreetmap.pbf2geojson.convertors.ConvertorUtils;
import org.openstreetmap.pbf2geojson.convertors.IncrementalLong;
import org.openstreetmap.pbf2geojson.data.SimpleNode;
import org.openstreetmap.pbf2geojson.data.SimpleWay;
import org.openstreetmap.pbf2geojson.storage.Storage;

import crosby.binary.BinaryParser;
import crosby.binary.Osmformat.DenseNodes;
import crosby.binary.Osmformat.HeaderBlock;
import crosby.binary.Osmformat.Node;
import crosby.binary.Osmformat.Relation;
import crosby.binary.Osmformat.Way;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Log
public class ParallelStreamParser extends BinaryParser {
	Writer out;
	Storage storage;
	Convertor convertor;
	WayClassifier classifier;

	long rowCount=0;
	//private List<SimpleNode> allNodes;
	private List<SimpleWay> allWays;
	private BlockingQueue<SimpleNode> allNodes;
	//private List<Relation> allRelations;
	
	public ParallelStreamParser(Writer out, Storage storage, Convertor convertor, WayClassifier classifier) {
		super();
		this.out = out;
		this.storage = storage;
		this.convertor= convertor;
		this.classifier = classifier;
		
		this.allNodes= new LinkedBlockingQueue<SimpleNode>();
		this.allWays = new ArrayList<SimpleWay>();
		log.info("Started");
		//this.allRelations = new ArrayList<Relation>();
	}

	
	@Override
	protected void parse(HeaderBlock header) {
	}


	@Override
	protected void parseDense(DenseNodes denseNodes)
	{
		
		final IncrementalLong 
				lastId = new IncrementalLong(), 
				lastLat = new IncrementalLong(), 
				lastLon = new IncrementalLong();
		
		final IncrementalLong propsPos = new IncrementalLong();
		this.allNodes.addAll(IntStream
				.range(0, denseNodes.getLonCount())
				.mapToObj(
						i -> new SimpleNode(this.parseLon(lastLon.incr(denseNodes.getLon(i))), 
								this.parseLat(lastLat.incr(denseNodes.getLat(i))),
								lastId.incr(denseNodes.getId(i)),
								getPropsForPosition(propsPos, denseNodes.getKeysValsList()))
						).collect(Collectors.toList()));
		
	}
	
	/*
	protected String[] parseAllDense(DenseNodes denseNodes) {
		
				//.parallel()
				.map(storage::setNode)
				.filter(classifier::isInteresting)
				.map(convertor::convertNode).toArray(String[]::new);//.sequential()
				//.forEach(this::writeNoException);
	//return denseNodes;	
	} */

		
	@Override
	protected void parseNodes(List<Node> nodes) {
		allNodes.addAll(nodes.parallelStream().map(this::fromNode).collect(Collectors.toList()));
	}
	
	protected Stream<String> parseAllNodes(BlockingQueue<SimpleNode> nodes) {
		//log.info("There are " + nodes.size() + " nodes in this block");
		return nodes
				//.stream()
				.parallelStream()
				.map(storage::setNode)
				.filter(classifier::isInteresting)
				.map(convertor::convertNode);//.sequential()
				
	}

	@Override
	protected void parseRelations(List<Relation> rel) {
		//log.info("There are "+rel.size()+ " relations in this block");
		//rel.get(0).
	}

	
	@Override
	protected void parseWays(List<Way> ways)
	{
		this.allWays.addAll(ways
				//.stream()
				.parallelStream()
				.map(this::fromWay).collect(Collectors.toList()));
	}
	
	protected Stream<String> parseAllWays(List<SimpleWay> ways) {
		//log.info("There are " + ways.size() + " ways in this block");
		return ways
		.parallelStream()
		//.stream()
		.map(storage::setWay)
		.filter(classifier::isInteresting)
		.map(convertor::convertWay);
				//.sequential()
				//.forEach(this::writeNoException);

	}

	protected void writeNoException(String str) {
		try {
			if(this.rowCount++%10000==1)
				log.info("Wrote "+this.rowCount+" lines");
			out.write(str);
			out.write('\n');
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
		log.info("Finished loading, now parsing");
		log.info("Nodes: "+this.allNodes.size()+", ways: "+this.allWays.size());
this.parseAllNodes(this.allNodes).forEach(this::writeNoException);
log.info("Finished parsing nodes, now parsing ways");
//this.allDenseNodes.parallelStream().flatMap(f -> Arrays.stream(this.parseAllDense(f))).forEach(this::writeNoException);
this.parseAllWays(this.allWays).forEach(this::writeNoException);
log.info("Finished parsing ways");
	}



}
