package org.openstreetmap.pbf;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.openstreetmap.pbf.file.FileBlock;
import org.openstreetmap.pbf.file.FileBlock.RawBlockPair;

import lombok.extern.java.Log;

import com.google.protobuf.InvalidProtocolBufferException;

@Log
public class BlockConsumer implements Runnable {
	private BlockingQueue<RawBlockPair> q;
	private boolean isRunning;
	private CountDownLatch endLatch;
	BinaryParser adaptor;
	
	
	public BlockConsumer(BlockingQueue<RawBlockPair> q, CountDownLatch endLatch, BinaryParser adaptor)
	{
		this.q = q;
		this.endLatch = endLatch;
		this.adaptor = adaptor;
	}

	@Override
	public void run() {
		this.isRunning = true;
		long ts = System.currentTimeMillis();
		try {
			int c=0;
		while(this.isRunning){
			long t0 = System.nanoTime()/1000;

			RawBlockPair p = q.take();
			long t1 = System.nanoTime()/1000;
			if(p==null || p.getHead()==null)
			{
				log.info("Received poison");
				this.isRunning=false;
			}
			else{
				FileBlock block = p.getHead().parseData(p.getContents());
				//Thread.sleep(100);
				adaptor.handleBlock(block);
				long t2 = System.nanoTime()/1000;
				long t0m = System.currentTimeMillis()-ts; 
				log.info("CMeter:\t"+Thread.currentThread().getName()+
						"\t"+c+"\t"+p.getContents().length+
						"\t"+t0m+"\t"+(t1-t0)+'\t'+(t2-t1));
			}
			c++;
		}
		} catch (InterruptedException e) {
			log.warning("Interrupted.");	
		} catch (InvalidProtocolBufferException e) {
			log.warning("Invalid protobuf protocol.");
		}
		endLatch.countDown();
	}
	
}
