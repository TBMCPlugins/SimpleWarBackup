package simpleWarBackup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import net.minecraft.server.v1_12_R1.RegionFile;
import net.minecraft.server.v1_12_R1.World;

/**
 * TODO write this
 */
public final class RegionFileCache 
{
	private static final int cap = 32; //capacity - how many regionFiles to keep cached
	private static final Map<File, RegionFile> cache = new HashMap<File, RegionFile>();
	
	/**
	 * Returns name of the region file that would contain the given chunk coordinates. 
	 * Name follows the standard minecraft format: "r.[region x].[region z].mca".
	 * Region coordinate = chunk coordinate >> 5.
	 * 
	 * @param x global chunk coordinate x
	 * @param z global chunk coordinate z
	 * @return
	 */
	public static String path(int x, int z)
	{
		return "r." + (x >> 5) + "." + (z >> 5) + ".mca";
	}
	
	/**
	 * Copied from net.minecraft.server.RefionFileCache.a(File, int, int).<p>
	 * 
	 * Returns any cached RegionFile mapped to the given File. Not finding one, creates 
	 * and caches, then returns, a new RegionFile. Before caching a new RegionFile, checks 
	 * that the cache contains fewer than 16 RegionFiles, and, finding 16 or more, calls 
	 * {@link #clearCache() RegionFileCache.clearCache()}.
	 * 
	 * @param backupDir  parent folder containing the "region" folder
	 * @param x          global chunk coordinate x
	 * @param z          global chunk coordinate z
	 * @return
	 */
	public static synchronized RegionFile get(File backupDir, int x, int z) 
	{
		File regionDir = new File(backupDir, "region");
		File mcaFile   = new File(regionDir, path(x,z));
		
		RegionFile regionFile = cache.get(mcaFile);

		if (regionFile != null) 
			return regionFile;
		if (!regionDir.exists())
			regionDir.mkdirs();
		if (cache.size() >= cap)
			clearCache();

		regionFile = new RegionFile(mcaFile);
		RegionFileCache.cache.put(mcaFile, regionFile);
		RegionFileCache.cacheChunkAccess(mcaFile, regionFile);
		return regionFile;
	}
	
	
	/**
	 * Copied from net.minecraft.server.RegionFileCache.a(). Iterates through all cached
	 * RegionFiles, closing each one's private RandomAccessFile, then clears the cache.
	 */
	public static synchronized void clearCache()
	{
		Iterator<RegionFile> iterator = RegionFileCache.cache.values().iterator();
		
		/* regionFile.c() closes the private RandomAccessFile 
		 * inside the regionFile object, and that's all it does.
		 * The entire method: if (file != null) file.close().
		 */
		while (iterator.hasNext()) 
		{
			RegionFile regionFile = iterator.next();
			try { if (regionFile != null) regionFile.c(); } 
			catch (IOException e) { e.printStackTrace(); }
		}
		RegionFileCache.cache.clear();
		RegionFileCache.cacheChunkAccess.clear();
	}
	
	
	/**
	 * TODO
	 * 
	 * @param world
	 * @param x
	 * @param z
	 * @return
	 */
	public static RegionFile getFromMinecraft(CraftWorld world, int x, int z)
	{
		/* root directory of the world, the directory
		 * sharing the world's name and holding its 
		 * data. 'region' folder is found in here.
		 */
		File dir = world.getHandle().getDataManager().getDirectory();

		return net.minecraft.server.v1_12_R1.RegionFileCache.a(dir, x, z);
	}
	
	
	
	/*=================================Chunk Access=================================*/
	
	/* Secondary cache, for holding references to the private fields "c" and "d" in each
	 * RegionFile cached above. TODO write more
	 */
	
	private static Map<RegionFile, 
	                   ChunkAccess> cacheChunkAccess = new HashMap<RegionFile, 
	                                                               ChunkAccess>();
	
	/**
	 * TODO
	 * 
	 * @param file
	 * @param regionFile
	 */
	private static void cacheChunkAccess(File file, RegionFile regionFile)
	{
		try
		{
			cacheChunkAccess.put(regionFile, new ChunkAccess(regionFile));
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
	public static int getChunkLength(RegionFile regionFile, int x, int z)
	{
		ChunkAccess access = cacheChunkAccess.get(regionFile);
		
		/* "offset" refers to the location of the chunk's bytes
		 * within the .mca file - or how many bytes into the file
		 * to seek when reading - divided by 4096.
		 */
		int offset;
		
		/* The first four bytes at that location, the first four
		 * bytes of the chunk's data, are an int recording the
		 * chunk's length in bytes, not including those four.
		 * 
		 * The next byte represents compression scheme. This byte,
		 * like the four previous bytes, is omitted when the game 
		 * reads a chunk's data. 
		 * 
		 * This method returns the length of data actually read.
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
	 * Gives access to the private fields {@code int[] d}, and {@code RandomAccessFile c}, 
	 * within a RegionFile object. The names of these fields may change in new releases of
	 * Spigot. TODO explain what both of those fields are
	 */
	private static class ChunkAccess
	{
		static final Field            offsetField;
		       final int[]            offset;
		       
		static final Field            bytesField;
		       final RandomAccessFile bytes;
		
		static
		{
			Field tmp1 = null;
			Field tmp2 = null;
			try 
			{
				tmp1 = RegionFile.class.getDeclaredField("d"); 
				tmp1.setAccessible(true);
				
				tmp2 = RegionFile.class.getDeclaredField("c");
				tmp2.setAccessible(true);
			} 
			catch (NoSuchFieldException | SecurityException e) 
			{
				/* TODO take more drastic action? Either
				 * Field missing would mean that Minecraft 
				 * has refactored, breaking the whole plugin
				 */
				e.printStackTrace();
			}
			offsetField = tmp1;
			bytesField  = tmp2;
		}
		
		
		/**
		 * Creates a new DataAccess object, which holds references to the private variables 
		 * {@code int[] d} and {@code RandomAccessFile c} within the given RegionFile. Variables 
		 * are accessed by reflection. TODO write more
		 * 
		 * @param  regionFile the RegionFile whose chunks are to be accessed
		 * 
		 * @throws FileNotFoundException
		 * @throws IllegalAccessException
		 * @throws IllegalArgumentException
		 */
		ChunkAccess(RegionFile regionFile) 
				throws FileNotFoundException,
				       IllegalAccessException,
				       IllegalArgumentException
		{
			this.offset = (int[])            offsetField.get(regionFile);
			this.bytes  = (RandomAccessFile) bytesField .get(regionFile);
		}
	}
	
	
}
