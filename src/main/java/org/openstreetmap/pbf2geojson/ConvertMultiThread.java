package org.openstreetmap.pbf2geojson;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import lombok.extern.java.Log;

import org.openstreetmap.pbf.BinaryParser;
import org.openstreetmap.pbf.file.FileBlock;
import org.openstreetmap.pbf.file.FileBlock.RawBlockPair;
import org.openstreetmap.pbf2geojson.CountingStreamParser.Stats;
import org.openstreetmap.pbf2geojson.convertors.GeoJSONConvertor;
import org.openstreetmap.pbf2geojson.storage.Storage;
import org.openstreetmap.pbf2geojson.storage.bytestore.ByteStore;
import org.openstreetmap.pbf2geojson.storage.bytestore.impl.RemainderByteBufferStore;
import org.openstreetmap.pbf2geojson.storage.impl.NodeByteStorage;
import org.openstreetmap.pbf2geojson.storage.nodestore.NodeStore;
import org.openstreetmap.pbf2geojson.storage.nodestore.impl.RemainderByteBufferNodeStore;

import crosby.binary.Osmformat;
import crosby.binary.Osmformat.PrimitiveBlock;

@Log
public class ConvertMultiThread {

	public static void readFile(String file,
			Callable<BinaryParser> parserFactory) throws IOException {

		final DataInputStream datinput = new DataInputStream(
				new BufferedInputStream(new FileInputStream(file)));

		try {
			while (true) {
				RawBlockPair p = FileBlock.processRaw(datinput);
				FileBlock bl = p.getHead().parseData(p.getContents());
				if (bl.getType().equals("OSMData")) {
					PrimitiveBlock block = Osmformat.PrimitiveBlock
							.parseFrom(bl.getData());
					BinaryParser parser = parserFactory.call();
					parser.parse(block);
				}
				// log.info("Length of q is "+q.size());
			}

		} catch (EOFException e) {
			// do nothing
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			datinput.close();
		}
	}

	public static class OSMExecutorThread implements Runnable {
		final BlockingQueue<Runnable> nodesQ;
		final BlockingQueue<Runnable> waysQ;
		final BlockingQueue<Runnable> relationsQ;
		final Storage storage;

		public OSMExecutorThread(final BlockingQueue<Runnable> nodesQ,
				final BlockingQueue<Runnable> waysQ,
				final BlockingQueue<Runnable> relationsQ, Storage storage) {
			super();
			this.nodesQ = nodesQ;
			this.waysQ = waysQ;
			this.relationsQ = relationsQ;
			this.storage = storage;
		}

		public void exec(BlockingQueue<Runnable> q, String threadName,
				int concurrency) throws InterruptedException {
			// final int numP=Runtime.getRuntime().availableProcessors()-2;

			final CountDownLatch latch = new CountDownLatch(concurrency);
			// log.info("Running an executor, que size is: "+ q.size());

			class BlockExecutor implements Runnable {

				@Override
				public void run() {
					// log.info("Running an executor");//, que size is: "+
					// q.size());
					Runnable r;
					try {
						while ((r = q.poll(500, TimeUnit.MILLISECONDS)) != null) {
							// log.info("Running parser");
							r.run();
						}
						// log.info("Finished " + threadName);
					} catch (InterruptedException e) {
						log.info("INTERRUPT!");
					} finally {
						latch.countDown();
					}
				}

			}
			for (int i = 0; i < concurrency; i++) {
				// log.info("starting executors");
				BlockExecutor exec = new BlockExecutor();
				Thread t = new Thread(exec, threadName + "-" + i);
				t.start();
				// log.info("start done");
			}
			// log.info("Latch is "+latch.getCount());
			latch.await();
			// log.info("Finished");
		}

		@Override
		public void run() {
			try {
				Thread.sleep(10);
				int concurrency = Runtime.getRuntime().availableProcessors();
				exec(nodesQ, "nodes", concurrency);
				log.info("Done with nodes");
				storage.finalizeNodes();
				log.info("Finalized nodes");
				exec(waysQ, "ways", concurrency);
				storage.finalizeWays();
				log.info("Finalized ways");
				exec(relationsQ, "relations", concurrency);
				log.info("Done with everything");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public static void guesstimateCapacity(String fileName, Stats stats) {

		// getFileSystem().g
		File f = new File(fileName);

		long size = 0;
		try {
			size = Files.size(f.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stats.setNodeCount(size / 7);
		stats.setWayCount(size / 70);
		stats.setRelationCount(size / 250);
		stats.setNodeRefCount(size / 8);

	}

	public static void main(String[] args) throws IOException,
			InterruptedException {
		String file = args[0];
		String out = args[1];

		final CountingStreamParser.Stats stats = new CountingStreamParser.Stats();

		log.info("Guesstimating number nodes, ways and relations");
		guesstimateCapacity(file, stats);
		log.info("Done with counting");
		log.info("Estimated number of nodes: " + stats.getNodeCount());
		log.info("Estimated number of ways: " + stats.getWayCount());
		log.info("Estimated number of relations: " + stats.getRelationCount());

		NodeStore nodeStore = new RemainderByteBufferNodeStore(
				stats.getNodeCount());
		ByteStore wayStore = new RemainderByteBufferStore(
				(int) stats.getWayCount(), (int) stats.getNodeRefCount());

		final Storage storage = new NodeByteStorage(nodeStore, wayStore);
		final WayClassifier classifier = new SimpleWayClassifier();
		final GeoJSONConvertor convertor = new GeoJSONConvertor(
				new SimpleWayClassifier(), storage);
		final PrintWriter p = new PrintWriter(new BufferedWriter(
				new PrintWriter(out), 50 * 1024));
		final BlockingQueue<Runnable> nodesQ = new LinkedBlockingQueue<Runnable>(
				10);
		final BlockingQueue<Runnable> waysQ = new LinkedBlockingQueue<Runnable>(
				10);
		final BlockingQueue<Runnable> relationsQ = new LinkedBlockingQueue<Runnable>(
				10);

		final Callable<BinaryParser> parserFactory = () -> {
			return new QueingStreamParser(p, storage, convertor, classifier,
					nodesQ, waysQ, relationsQ);
		};
		final OSMExecutorThread exec = new OSMExecutorThread(nodesQ, waysQ,
				relationsQ, storage);
		Thread eThread = new Thread(exec, "executor");
		eThread.start();
		log.info("Started reading file");
		readFile(file, parserFactory);
		log.info("Finished reading file");
		eThread.join();
		log.info("Closing input");
		p.close();
		log.info("Closing storage");
		storage.close();
		log.info("Done");
	}

}
