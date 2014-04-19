package org.openstreetmap.pbf2geojson;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.openstreetmap.pbf.BinaryParser;
import org.openstreetmap.pbf.ParallelReader;
import org.openstreetmap.pbf2geojson.convertors.Convertor;
import org.openstreetmap.pbf2geojson.convertors.GeoJSONConvertor;
import org.openstreetmap.pbf2geojson.storage.Storage;
import org.openstreetmap.pbf2geojson.storage.impl.MapDBStorage;
import org.openstreetmap.pbf2geojson.storage.impl.MemoryStorage;

import java.util.stream.IntStream;

import sun.nio.ch.ChannelInputStream;

public class Convert {

	public static void main(String[] args) throws IOException, InterruptedException {
		//Thread.sleep(20000);
//		InputStream input = ReadFileExample.class.getResourceAsStream("/sample.pbf");
		InputStream input = System.in;
		if(args.length>0)
		{
			//input = new ChannelInputStream(arg0)
			input = new FileInputStream(new File(args[0]));
		}
		
		OutputStream output = System.out;
		//final File output;// = null;
		if(args.length>1)
		{
			output = new FileOutputStream(new File(args[1]));
			//output = new File(args[1]);
		//	output.mkdirs();
		}
		else
		{
			output = null;
		}
		
		Writer out = new BufferedWriter(new PrintWriter(output), 10*1024*1024);
		Storage storage = new MemoryStorage();
		//Storage storage = new MapDBStorage();
		WayClassifier classifier = new SimpleWayClassifier();
		Convertor convertor = new GeoJSONConvertor(new SimpleWayClassifier(), storage);
		PrintWriter pout = new PrintWriter(out);
		BinaryParser[] parsers = IntStream
				.range(0,Runtime.getRuntime().availableProcessors())
				//.mapToObj(i -> new StreamParser(createFile(output, i), storage, convertor, classifier))
				.mapToObj(i -> new StreamParser(pout, storage, convertor, classifier))	
				.toArray(BinaryParser[]::new);
//		BinaryParser brad = new StreamParser(out, storage, convertor, classifier);
		
		

		//BlockReaderAdapter brad = new ParallelStreamParser(pout, storage, convertor, classifier);
		ParallelReader reader = new ParallelReader(input, parsers);
		reader.readAndProcess();
		//new BlockInputStream(input, brad).process();
        storage.close();
	}

	protected static PrintWriter createFile(File parent,  int index)
	{
		try {
			return new PrintWriter(new FileOutputStream(new File(parent, "out_"+index+".json")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public CommandLineParser getParser() {
		CommandLineParser parser = new GnuParser();
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("help")
				.withDescription("Display help message").create());
		
		return parser;
	}

}
