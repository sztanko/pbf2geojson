package org.openstreetmap.pbf2geojson;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public class ParallelReaderTest {
	
	public static List<ByteString> read(String dir) throws IOException {
		final StatsCollector sc = StatsCollector.getInstance();
		return Arrays.stream(new File(dir).listFiles()).parallel().map(f -> {
			try {
				sc.start();
				ByteString b=ByteString.copyFrom(Files.readAllBytes(Paths.get(f.getAbsoluteFile().toURI())));
				sc.end("Reading file", b.size());
				return b;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}).collect(Collectors.toList());

	}
	
	public static void processBlock(ByteString b, Callable<BinaryParser> parserFactory){
		PrimitiveBlock block;
		BinaryParser p;
		final StatsCollector sc = StatsCollector.getInstance();
		
		try {
			p = parserFactory.call();
			//sc.start();
			block = Osmformat.PrimitiveBlock
			        .parseFrom(b);
			//sc.end("Osmformat.PrimitiveBlock.parseFrom", 0);
			//sc.start();
			p.parse(block);
			//sc.end("processBlock -> parseBlock",block.getSerializedSize());
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		
		
	}

	public static List<ByteString> readFile(String file) throws IOException
	{
		int c=0;
    	DataInputStream datinput = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
    	List<ByteString> q = new ArrayList<ByteString>();
    	try{
    	while (true) {
          RawBlockPair p = FileBlock.processRaw(datinput);
          FileBlock bl = p.getHead().parseData(p.getContents());
          if(bl.getType().equals("OSMData"))
        	  q.add(bl.getData());
          //log.info("Length of q is "+q.size());
          c++;
        }
    	}
    	catch (EOFException e) {
            
        }
    	return q;
	}
	
	
	public static void executeAll(Stream<ByteString> b, Callable<BinaryParser> parserFactory)
	{

		b.forEach(p -> processBlock(p, parserFactory));
	}
	
	public static void main(String[] args) throws IOException
	{
		String file = args[0];
		String out = args[1];
		String isMulti = "no";
		if(args.length>=3)
			isMulti = args[2].toLowerCase();
		log.info("Reading from "+file);
	
		//final Storage storage = new MemoryStorage();
		//Storage storage = new HCStorage();
		final Storage storage = new TroveMemoryStorage();
		final WayClassifier classifier = new SimpleWayClassifier();
		final Convertor convertor = new GeoJSONConvertor(new SimpleWayClassifier(), storage);
		final List<PrintWriter> writers = Collections.synchronizedList(new ArrayList<PrintWriter>());
		final ThreadLocal<PrintWriter> pout = new ThreadLocal<PrintWriter>(){
			@Override
			protected PrintWriter initialValue() {
				
				try {
					PrintWriter p = new PrintWriter(
							new BufferedWriter(
									new PrintWriter(out+"."+Thread.currentThread().getName()), 1*1024*1024));
					writers.add(p);
					return p;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				return null;
			}
		};
		
		//final PrintWriter pout = new PrintWriter(new BufferedOutputStream(new FileOutputStream(out), 10000000));
		
		StatsCollector sc=StatsCollector.getInstance();
		sc.start();
		//List<ByteString> b = read(dir);
		final List<ByteString> b = readFile(file);
		sc.end("ParallelReaderTest - reading blocks", b.size());
		sc.start();
		StatsCollector.numThreads=Runtime.getRuntime().availableProcessors();
		Stream<ByteString> st = b.stream();
		if("multi".equals(isMulti))
			st=st.parallel();
		sc.end("ParallelReaderTest - parsing", b.size());
		sc.start();
		writers.forEach(w -> w.close());
		//pout.close();
		sc.end("Closing output streams", 0);
		StatsCollector.printSummaryForAll();
		
	}
	
}
