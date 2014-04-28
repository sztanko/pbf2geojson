package org.openstreetmap.pbf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

import lombok.extern.java.Log;

import org.openstreetmap.pbf.file.FileBlock.RawBlockPair;

import dimi.stats.StatsCollector;

@Log
public class ParallelReader {
private InputStream in;
private BlockingQueue<RawBlockPair> q;
private BinaryParser[] parsers;


public ParallelReader(InputStream in, BinaryParser[] parsers)
{
	this.in = in;
	this.q = new ArrayBlockingQueue<RawBlockPair>(Runtime.getRuntime().availableProcessors()*3);
	//this.q = new LinkedTransferQueue<RawBlockPair>();
	
	this.parsers = parsers;
}

public void readAndProcess() throws IOException
{
	StatsCollector sc = StatsCollector.getInstance().start();
	try{
	sc.start();
	final CountDownLatch countDown = new CountDownLatch(this.parsers.length);
	Arrays.stream(this.parsers).map(p -> new Thread(new BlockConsumer(this.q, countDown, p))).forEach(t -> t.start());
	final BlockReaderProducer producer = new BlockReaderProducer(this.in, this.q);
	final int numBlocks = producer.load();
	log.info("Read "+numBlocks+" blocks");
	countDown.await();
	sc.end("ParallelReader -> processing", numBlocks);
	sc.start();
	Arrays.stream(this.parsers).forEach(p -> p.complete());
	sc.end("ParallelReader -> closing", 0);
	}
	catch(InterruptedException e)
	{
		
	}
	sc.end("ParallelReader", 0);
}
}
