package simpleWarBackup;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;

/**
 * TODO write javadoc
 */
public class RegionChunkList 
{
	/**
	 * TODO write javadoc
	 */
	private final RandomAccessFile readwrite;
	
	/**
	 * TODO write javadoc
	 */
	final BitSet chunks = new BitSet(1024);
	
	/**
	 * TODO write javadoc
	 */
	final long regionCoordinates;
	
	
	
	/**
	 * TODO write javadoc
	 * 
	 * @param file
	 * @throws IOException
	 */
	RegionChunkList(File file) throws IOException
	{
		readwrite = new RandomAccessFile(file, "rw");
		
		// Write chunk list to BitSet:
		{	
			byte[] bytes = new byte[128];
			readwrite.readFully(bytes);
				
			/* Each bit represents one of the region's 1024 chunks (32x32). Chunks are 
			 * listed in ascending x,z order: (0,0),(1,0)...(31,0),(0,1),(1,1)......(31,31).
			 * 
			 * Bits in each byte are read from smallest to largest position: 1,2,4...128.
			 * The values are written to a BitSet, which represents the region in runtime.
			 * 
			 * 1 = true, 0 = false.
			 */
			int i = 0, j, k;
			for (byte b : bytes)
				for (j = 0, k = 1; j < 8; i++, j++, k <<= 1)
					if ((b & k) == k) chunks.set(i);
		}
		
		// Get region coordinate from filename:
		{
			String[] name = file.getName().split(".");
			int x = Integer.parseInt(name[1]);
			int z = Integer.parseInt(name[2]);
			
			regionCoordinates = (long) x | ((long) z << 32);
		}
	}
	
	
	/**
	 * TODO write javadoc
	 * 
	 * @param directory
	 * @param regionCoordinates
	 */
	RegionChunkList(File directory, long regionCoordinates) throws IOException
	{
		this.regionCoordinates = regionCoordinates;
		
		int x = (int)  regionCoordinates;
		int z = (int) (regionCoordinates >>> 32);
		String filename = "r." + x + "." + z + ".chunklist";
		
		readwrite = new RandomAccessFile(new File(directory, filename), "rw");
	}
	
	
	/**
	 * TODO write javadoc (and add comment to explain numbers)
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
	 * TODO write javadoc (and add comment to explain numbers)
	 * 
	 * @param x  chunk x coordinate
	 * @param z  chunk z coordinate
	 */
	void storeChunk(int x, int z)
	{
		x &= 31;
		z &= 31;
		chunks.set(x + z * 32);
	}
	
	
	/**
	 * TODO write javadoc (and add comment to explain numbers)
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
	
	
	/**
	 * TODO write javadoc
	 * 
	 * @throws IOException 
	 */
	void saveToFile() throws IOException
	{
		int i = 0;
		byte bytes[] = new byte[128];
		for (byte b : bytes)
		{
			if (chunks.get(i++)) b = (byte) (b | (byte) 1  );
			if (chunks.get(i++)) b = (byte) (b | (byte) 2  );
			if (chunks.get(i++)) b = (byte) (b | (byte) 4  );
			if (chunks.get(i++)) b = (byte) (b | (byte) 8  );
			if (chunks.get(i++)) b = (byte) (b | (byte) 16 );
			if (chunks.get(i++)) b = (byte) (b | (byte) 32 );
			if (chunks.get(i++)) b = (byte) (b | (byte) 64 );
			if (chunks.get(i++)) b = (byte) (b | (byte) 128);
		}
		readwrite.seek(0);
		readwrite.write(bytes);
	}
	
	
	/**
	 * Closes the RandomAccessFile associated with this RegionChunkList.<p>
	 * 
	 * When you are done with a RegionChunkList, you should invoke this method.
	 * 
	 * @throws IOException if the RandomAccessFile was not closed
	 */
	void close() throws IOException
	{
		readwrite.close();
	}
}
