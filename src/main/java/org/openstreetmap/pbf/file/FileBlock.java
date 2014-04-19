/** Copyright (c) 2010 Scott A. Crosby. <scott@sacrosby.com>

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as 
   published by the Free Software Foundation, either version 3 of the 
   License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package org.openstreetmap.pbf.file;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.Deflater;

import lombok.Data;

import com.google.protobuf.ByteString;


/** A full fileblock object contains both the metadata and data of a fileblock */
public class FileBlock extends FileBlockBase {
    /** Contains the contents of a block for use or further processing */
    ByteString data; // serialized Format.Blob

    /** Don't be noisy unless the warning occurs somewhat often */
    static int warncount = 0;

    private FileBlock(String type, ByteString blob, ByteString indexdata) {
        super(type, indexdata);
        this.data = blob;
    }

    public static FileBlock newInstance(String type, ByteString blob,
            ByteString indexdata) {
      if (blob != null && blob.size() > MAX_BODY_SIZE/2) {
        System.err.println("Warning: Fileblock has body size too large and may be considered corrupt");
        if (blob != null && blob.size() > MAX_BODY_SIZE-1024*1024) {
          throw new Error("This file has too many entities in a block. Parsers will reject it.");
        }
      }
      if (indexdata != null && indexdata.size() > MAX_HEADER_SIZE/2) {
        System.err.println("Warning: Fileblock has indexdata too large and may be considered corrupt");
        if (indexdata != null && indexdata.size() > MAX_HEADER_SIZE-512) {
          throw new Error("This file header is too large. Parsers will reject it.");
        }
      }
      return new FileBlock(type, blob, indexdata);
    }

     protected void deflateInto(crosby.binary.Fileformat.Blob.Builder blobbuilder) {
        int size = data.size();
        Deflater deflater = new Deflater();
        deflater.setInput(data.toByteArray());
        deflater.finish();
        byte out[] = new byte[size];
        deflater.deflate(out);
        
        if (!deflater.finished()) {
            // Buffer wasn't long enough. Be noisy.
          ++warncount;
          if (warncount > 10 && warncount%100 == 0)
               System.out.println("Compressed buffers are too short, causing extra copy");
            out = Arrays.copyOf(out, size + size / 64 + 16);
            deflater.deflate(out, deflater.getTotalOut(), out.length
                    - deflater.getTotalOut());
            if (!deflater.finished()) {
              throw new Error("Internal error in compressor");
            }
        }
        ByteString compressed = ByteString.copyFrom(out, 0, deflater
                .getTotalOut());
        blobbuilder.setZlibData(compressed);
        deflater.end();
    }

    

    /** Reads or skips a fileblock. */
    static void process(DataInputStream input, BlockHandler callback)
            throws IOException {
        FileBlockHead fileblock = FileBlockHead.readHead(input);
            callback.handleBlock(fileblock.readContents(input));
        
    }
    
    public static @Data class RawBlockPair{
    	private byte[] contents;
    	private FileBlockHead head;
    }

    
    static public RawBlockPair processRaw(DataInputStream input)
        throws IOException {
    	FileBlockHead head = FileBlockHead.readHead(input);
        byte[] block = head.readBytes(input);
        RawBlockPair p = new RawBlockPair();
        p.setContents(block);
        p.setHead(head);
    	//FileBlock block =  head.readContents(input);
        return p;
    }
    
    public ByteString getData() {
        return data;
    }
}
