package org.openstreetmap.pbf2geojson;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.openstreetmap.pbf2geojson.storage.Storage;
import org.openstreetmap.pbf2geojson.storage.impl.MemoryStorage;

import crosby.binary.file.BlockInputStream;
import crosby.binary.file.BlockReaderAdapter;

public class Convert {

	public static void main(String[] args) throws IOException {

//		InputStream input = ReadFileExample.class.getResourceAsStream("/sample.pbf");
		InputStream input = System.in;
		
		Writer out = new BufferedWriter(new PrintWriter(System.out));
		Storage storage = new MemoryStorage();
		BlockReaderAdapter brad = new StreamParser(out, storage, new SimpleWayClassifier());
        new BlockInputStream(input, brad).process();
	}

	public CommandLineParser getParser() {
		CommandLineParser parser = new GnuParser();
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("help")
				.withDescription("Display help message").create());
		
		return parser;
	}

}
