package org.openstreetmap.pbf2geojson;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.openstreetmap.pbf.BinaryParser;
import org.openstreetmap.pbf.file.FileBlock;
import org.openstreetmap.pbf.file.FileBlock.RawBlockPair;
import org.openstreetmap.pbf2geojson.convertors.GeoJSONConvertor;
import org.openstreetmap.pbf2geojson.storage.Storage;
import org.openstreetmap.pbf2geojson.storage.impl.ConcurrentTroveMemoryStorage;

import crosby.binary.Osmformat;
import crosby.binary.Osmformat.PrimitiveBlock;

public class ConvertSingleThread {

	public static void readFile(String file, BinaryParser parser)
			throws IOException {

		final DataInputStream datinput = new DataInputStream(
				new BufferedInputStream(new FileInputStream(file)));

		try {
			while (true) {
				RawBlockPair p = FileBlock.processRaw(datinput);
				FileBlock bl = p.getHead().parseData(p.getContents());
				if (bl.getType().equals("OSMData")) {
					PrimitiveBlock block = Osmformat.PrimitiveBlock
							.parseFrom(bl.getData());
					parser.parse(block);
				}
				// log.info("Length of q is "+q.size());
			}
		} catch (EOFException e) {
			parser.complete();
		}

	}

	public static void main(String[] args) throws IOException {
		String file = args[0];
		String out = args[1];

		//final Storage storage = new MemoryStorage();
		// Storage storage = new HCStorage();
		//final Storage storage = new TroveMemoryStorage();
		final Storage storage = new ConcurrentTroveMemoryStorage();
		final WayClassifier classifier = new SimpleWayClassifier();
		final GeoJSONConvertor convertor = new GeoJSONConvertor(
				new SimpleWayClassifier(), storage);
		final PrintWriter p = new PrintWriter(new BufferedWriter(
				new PrintWriter(out), 1 * 1024 * 1024));

		BinaryParser parser = new StreamParser(p, storage, convertor,
				classifier);
		readFile(file, parser);
		p.close();

	}

}
