package org.openstreetmap.pbf2geojson;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.Thread.State;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Log
public class ParallelStreamParser extends BinaryParser {
	PrintWriter out;
	Storage storage;
	Convertor convertor;
	WayClassifier classifier;
	AtomicLong nodeCounter;
	AtomicLong wayCounter;

	CountDownLatch nodeLatch;
	CountDownLatch wayLatch;

	Thread t;

	long rowCount = 0;
	// private List<SimpleNode> allNodes;
	// private List<SimpleWay> allWays;
	private BlockingQueue<SimpleWay> allWays;
	private BlockingQueue<SimpleNode> allNodes;

	// private List<Relation> allRelations;

	public ParallelStreamParser(PrintWriter out, Storage storage,
			Convertor convertor, WayClassifier classifier) {
		super();
		Thread.currentThread().setName("Main");
		this.out = out;
		this.storage = storage;
		this.convertor = convertor;
		this.classifier = classifier;

		this.allNodes = new LinkedBlockingQueue<SimpleNode>(100000);
		this.allWays = new LinkedBlockingQueue<SimpleWay>(30000);

		t = new Thread(new ControlThread());
		t.setName("Control Thread");
		log.info("Started");
		nodeCounter = new AtomicLong();
		wayCounter = new AtomicLong();
		// this.allRelations = new ArrayList<Relation>();

	}

	private class ControlThread implements Runnable {

		@Override
		public void run() {
			try {
				int numP = Runtime.getRuntime().availableProcessors();
				//int numP = 40;
				log.info("Number of available cores is " + numP);
				nodeLatch = new CountDownLatch(numP);
				IntStream.range(0, numP).mapToObj(i -> new NodeProcessor())
						.forEach(t -> new Thread(t).start());
				log.info("Started parsing nodes");
				nodeLatch.await();
				log.info("Finished parsing " + nodeCounter.get()
						+ " nodes, now parsing ways");

				wayLatch = new CountDownLatch(numP);
				IntStream.range(0, numP).mapToObj(i -> new WayProcessor())
						.forEach(t -> new Thread(t).start());

				wayLatch.await();
				log.info("Finished parsing " + wayCounter.get() + " ways");

			} catch (InterruptedException e) {
				e.printStackTrace();
				log.warning(e.getMessage());
			}
		}

	}

	@Override
	protected void parse(HeaderBlock header) {
		if (t.getState() == State.NEW) {
			log.info("Starting control thread");
			t.start();
		}

	}

	@Override
	protected void parseDense(DenseNodes denseNodes) {

		final IncrementalLong lastId = new IncrementalLong(), lastLat = new IncrementalLong(), lastLon = new IncrementalLong();

		final IncrementalLong propsPos = new IncrementalLong();
		// this.allNodes.addAll(
		IntStream
				.range(0, denseNodes.getLonCount())
				.mapToObj(
						i -> new SimpleNode((float)this.parseLon(lastLon
								.incr(denseNodes.getLon(i))), (float)this
								.parseLat(lastLat.incr(denseNodes.getLat(i))),
								lastId.incr(denseNodes.getId(i)),
								getPropsForPosition(propsPos,
										denseNodes.getKeysValsList())))
				.forEach(this::putIntoNodes);

	}

	protected void putIntoNodes(SimpleNode node) {

		try {
			this.allNodes.put(node);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected void putIntoWays(SimpleWay way) {

		try {
			this.allWays.put(way);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void parseNodes(List<Node> nodes) {
		nodes.stream()
		// .parallelStream()
				.map(this::fromNode).forEach(this::putIntoNodes);
	}

	@Override
	protected void parseRelations(List<Relation> rel) {
		// log.info("There are "+rel.size()+ " relations in this block");
		// rel.get(0).
	}

	@Override
	protected void parseWays(List<Way> ways) {
		// this.allWays.addAll(
		ways.stream()
		// .parallelStream()
				.map(this::fromWay).forEach(this::putIntoWays);
		// .collect(Collectors.toList()));
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

	protected class NodeProcessor implements Runnable {

		@Override
		public void run() {
			Thread.currentThread().setName("NodeProcessor "+Thread.currentThread().getName());
			boolean isFinished = false;
			log.info("Started a node processor");
			try {
				while (!isFinished) {
					// log.info("Inside node processor");
					SimpleNode el = allNodes.poll(1, TimeUnit.SECONDS);
					if (el == null) {
						//log.info("Retrieved a null");
						isFinished = true;
					} else {
						nodeCounter.incrementAndGet();

						Stream.of(el).map(storage::setNode)
								.filter(n -> classifier.isInteresting(n.getProperties()))
								.map(convertor::convertNode)
								.forEach(out::println);
					}

				}
			} catch (InterruptedException e) {
				isFinished = true;
				log.info("Interrupted!");
			}
			nodeLatch.countDown();
			log.info("Thread finished, "+nodeLatch.getCount()+" threads left");
		}

	}

	protected class WayProcessor implements Runnable {
		@Override
		public void run() {
			Thread.currentThread().setName("WayProcessor "+Thread.currentThread().getName());
			boolean finished = false;
			try {

				while (!finished) {
					SimpleWay el = allWays.poll(1, TimeUnit.SECONDS);
					if (el == null) {
						finished = true;
					} else {
						wayCounter.incrementAndGet();

						Stream.of(el).map(storage::setWay)
								.filter(classifier::isInteresting)
								.map(convertor::convertWay)
								.forEach(out::println);// .sequential()
					}
				}
			} catch (InterruptedException e) {
				finished = true;
			}
			wayLatch.countDown();
			log.info("Thread finished, "+wayLatch.getCount()+" threads left");
		}

	}

	public void complete() {
		log.info("Done with all");
	}

}
