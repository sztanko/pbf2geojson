package org.openstreetmap.pbf2geojson;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Callable;

import lombok.extern.java.Log;

import org.openstreetmap.pbf.BinaryParser;
import org.openstreetmap.pbf.file.FileBlock;
import org.openstreetmap.pbf.file.FileBlock.RawBlockPair;
import org.openstreetmap.pbf2geojson.convertors.Convertor;
import org.openstreetmap.pbf2geojson.convertors.GeoJSONConvertor;
import org.openstreetmap.pbf2geojson.storage.Storage;
import org.openstreetmap.pbf2geojson.storage.impl.TroveMemoryStorage;

import com.google.protobuf.ByteString;

import crosby.binary.Osmformat;
import crosby.binary.Osmformat.PrimitiveBlock;
import dimi.stats.StatsCollector;

@Log
public class ConvertSingleThread {

	public static void processBlock(ByteString b,
			Callable<BinaryParser> parserFactory) {
		PrimitiveBlock block;
		BinaryParser p;
		final StatsCollector sc = StatsCollector.getInstance();

		try {
			p = parserFactory.call();
			// sc.start();
			block = Osmformat.PrimitiveBlock.parseFrom(b);
			// sc.end("Osmformat.PrimitiveBlock.parseFrom", 0);
			// sc.start();
			p.parse(block);
			// sc.end("processBlock -> parseBlock",block.getSerializedSize());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void readFile(String file, BinaryParser parser)
			throws IOException {
		int c = 0;
		final StatsCollector sc = StatsCollector.getInstance();
		sc.start();
		final DataInputStream datinput = new DataInputStream(
				new BufferedInputStream(new FileInputStream(file)));

		try {
			while (true) {
				sc.start();
				sc.start();
				RawBlockPair p = FileBlock.processRaw(datinput);
				FileBlock bl = p.getHead().parseData(p.getContents());
				sc.end("Read pbf",bl.getData().size());
				if (bl.getType().equals("OSMData")) {
					sc.start();
					PrimitiveBlock block = Osmformat.PrimitiveBlock
							.parseFrom(bl.getData());
					sc.end("Parsed pbf",bl.getData().size());
					sc.start();
					parser.parse(block);
					sc.end("processed  block",0);
				}
				// log.info("Length of q is "+q.size());
				sc.end("read, parsed and processed a block", c);
				c++;
			}
		} catch (EOFException e) {
			parser.complete();
			sc.end("reached eof",0);
		}
		sc.end("Finished processing", c);
	}

	public static void main(String[] args) throws IOException {
		String file = args[0];
		String out = args[1];

		// final Storage storage = new MemoryStorage();
		// Storage storage = new HCStorage();
		final Storage storage = new TroveMemoryStorage();
		final WayClassifier classifier = new SimpleWayClassifier();
		final Convertor convertor = new GeoJSONConvertor(
				new SimpleWayClassifier(), storage);
		final PrintWriter p = new PrintWriter(new BufferedWriter(
				new PrintWriter(out), 1 * 1024 * 1024));

		StatsCollector sc = StatsCollector.getInstance();
		sc.start();
		BinaryParser parser = new StreamParser(p, storage, convertor,
				classifier);
		readFile(file, parser);
		sc.end("ParallelReaderTest - reading blocks", 0);
		sc.start();
		p.close();
		sc.end("Closing output streams", 0);
		StatsCollector.printSummaryForAll();

	}

}
