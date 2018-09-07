package simpleWarBackup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.v1_12_R1.RegionFile;

/**
 * TODO write javadoc (mention that everything in this class is package private)
 */
public class ChunkAccess_Cache 
{
	static Map<RegionFile, Access> cache = new HashMap<RegionFile,  Access>();
	
	/**
	 * TODO write javadoc
	 * 
	 * @param file
	 * @param regionFile
	 */
	static void createAndPut(File file, RegionFile regionFile)
	{
		try
		{
			cache.put(regionFile, new Access(regionFile));
		} 
		catch (FileNotFoundException  | 
		       IllegalAccessException | 
		       IllegalArgumentException e) 
		{
			e.printStackTrace();//TODO
		}
	}
	
	
	/**
	 * TODO
	 * 
	 * @param regionFile
	 * @param x
	 * @param z
	 * @return
	 */
	static int getChunkLength(RegionFile regionFile, int x, int z)
	{
		Access access = cache.get(regionFile);
		
		/* "offset" refers to the location of the chunk's bytes
		 * within the .mca file (i.e. how many bytes into the file
		 * to seek when reading) divided by 4096.
		 */
		int offset;
		
		/* The first four bytes at that location encode an int. This
		 * int is the chunk's remaining length in bytes, excluding the
		 * four bytes of the int.
		 * 
		 * The fifth byte represents compression scheme. The first five 
		 * bytes are omitted when the game reads a chunk's data. 
		 * 
		 * The following method returns the length of data actually read.
		 */
		if (access != null && (offset = access.offset[x + z * 32]) > 0)
		{
			try 
			{
				access.bytes.seek(offset * 4096);
				return access.bytes.readInt() - 1;
			} 
			catch (IOException e) { e.printStackTrace(); }
		}
		return 0;
	}
	
	
	/**
	 * Provides access to the private fields {@code int[] d}, and {@code RandomAccessFile c}, 
	 * within a RegionFile object. The names of these fields may change in new releases of
	 * Spigot. TODO explain what both of those fields are
	 */
	private static class Access
	{
		static final      Field       offsetField;
		static final      Field       bytesField;
		       final      int[]       offset;
		       final RandomAccessFile bytes;
		
		static
		{
			Field tmp1 = null;
			Field tmp2 = null;
			try 
			{
				tmp1 = RegionFile.class.getDeclaredField("d");
				tmp2 = RegionFile.class.getDeclaredField("c");
				tmp1.setAccessible(true);
				tmp2.setAccessible(true);
			} 
			catch (NoSuchFieldException | SecurityException e) 
			{
				e.printStackTrace();
			}
			offsetField = tmp1;
			bytesField  = tmp2;
		}
		
		
		/**
		 * Creates a new Access object to hold references to the private variables {@code int[] d} 
		 * and {@code RandomAccessFile c} within the given RegionFile. Variables are accessed by 
		 * reflection. TODO write more
		 * 
		 * @param  regionFile the RegionFile whose chunks are to be accessed
		 * 
		 * @throws FileNotFoundException
		 * @throws IllegalAccessException
		 * @throws IllegalArgumentException
		 */
		Access(RegionFile regionFile) throws FileNotFoundException,
		                                     IllegalAccessException,
		                                     IllegalArgumentException
		{
			offset =      (int[])       offsetField.get(regionFile);
			bytes  = (RandomAccessFile) bytesField .get(regionFile);
		}
	}
}
