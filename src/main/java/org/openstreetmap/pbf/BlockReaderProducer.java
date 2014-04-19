package org.openstreetmap.pbf;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;

import lombok.Data;
import lombok.extern.java.Log;

import org.openstreetmap.pbf.file.FileBlock;
import org.openstreetmap.pbf.file.FileBlock.RawBlockPair;

@Log
@Data
public class BlockReaderProducer{
    private InputStream input;
    private BlockingQueue<RawBlockPair> q;
    
	public BlockReaderProducer(InputStream input, BlockingQueue<RawBlockPair> q) {
        this.input = input;
        this.q = q;
       }

    public int load() throws IOException, InterruptedException {
    	int c=0;
    	DataInputStream datinput = new DataInputStream(new BufferedInputStream(input));
    	try {
        while (true) {
          RawBlockPair p = FileBlock.processRaw(datinput);
          q.put(p);
          //log.info(msg);
          c++;
        }
      } catch (EOFException e) {
        
      }
    	return c;
    }

    public void close() throws IOException {
        input.close();
    }


	
}
