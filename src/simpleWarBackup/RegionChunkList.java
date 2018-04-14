package simpleWarBackup;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;
import java.util.HashMap;

public class RegionChunkList 
{
	private final File file;
	private final RandomAccessFile readwrite;
	private final BitSet chunks = new BitSet(1024);
	
	private final HashMap<String, HashMap
	                     <String, HashMap
	                     <String, RegionChunkList>>> lists = 
	                     
	              new HashMap<String, HashMap
	                         <String, HashMap
	                         <String, RegionChunkList>>>();
	
	
	/**
	 * TODO
	 * 
	 * @param file
	 * @throws IOException
	 */
	RegionChunkList(File file, String backup, String town, String world) throws IOException
	{
		readwrite = new RandomAccessFile((this.file = file), "rw");
		
		byte[] bytes = new byte[128];
		if (readwrite.length() >= 128) 
		{
			readwrite.readFully(bytes);
			
			int i = 0, j, k;
			for (byte b : bytes)
				for (j = 0, k = 1; j < 8; i++, j++, k <<= 1)
					if ((b & k) == k) chunks.set(i);
		}
		else readwrite.write(bytes);
		
		lists.get(backup)
		     .get(town)
		     .put(world, this);
	}
	
	
	/**
	 * TODO
	 * 
	 * @param x
	 * @param z
	 * @return
	 */
	boolean isChunkStored(int x, int z)
	{
		x &= 31;
		z &= 31;
		return chunks.get(x + z * 32);
	}
	
	
	/**
	 * TODO
	 * 
	 * @param x
	 * @param z
	 */
	void storeChunk(int x, int z)
	{
		x &= 31;
		z &= 31;
		chunks.set(x + z * 32);
	}
	
	
	/**
	 * TODO
	 * 
	 * @param x
	 * @param z
	 */
	void markChunkAsRestored(int x, int z)
	{
		x &= 31;
		z &= 31;
		chunks.clear(x + z * 32);
	}
}
