package org.openstreetmap.pbf;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import lombok.extern.java.Log;

import org.openstreetmap.pbf.file.FileBlock;
import org.openstreetmap.pbf.file.FileBlock.RawBlockPair;

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
		try {
			
		while(this.isRunning){
			
			RawBlockPair p = q.poll(300, TimeUnit.MILLISECONDS);
			if(p==null)
			{
				this.isRunning=false;
			}
			else{
				FileBlock block = p.getHead().parseData(p.getContents());
				adaptor.handleBlock(block);
			}
		}
		} catch (InterruptedException e) {
			log.warning("Interrupted.");	
		} catch (InvalidProtocolBufferException e) {
			log.warning("Invalid protobuf protocol.");
		}
		endLatch.countDown();
	}
	
}
