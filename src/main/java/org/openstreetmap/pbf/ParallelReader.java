package org.openstreetmap.pbf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import lombok.extern.java.Log;

import org.openstreetmap.pbf.file.FileBlock.RawBlockPair;

@Log
public class ParallelReader {
private InputStream in;
private BlockingQueue<RawBlockPair> q;
private BinaryParser[] parsers;


public ParallelReader(InputStream in, BinaryParser[] parsers)
{
	this.in = in;
	this.q = new LinkedBlockingQueue<RawBlockPair>(parsers.length*2);
	this.parsers = parsers;
}

public void readAndProcess() throws IOException
{
	try{
	CountDownLatch countDown = new CountDownLatch(this.parsers.length);
	Arrays.stream(this.parsers).map(p -> new Thread(new BlockConsumer(this.q, countDown, p))).forEach(t -> t.start());
	BlockReaderProducer producer = new BlockReaderProducer(this.in, this.q);
	int numBlocks = producer.load();
	log.info("Read "+numBlocks+" blocks");
	countDown.await();
	Arrays.stream(this.parsers).forEach(p -> p.complete());
	log.info("Done");
	}
	catch(InterruptedException e)
	{
		
	}
}
}
