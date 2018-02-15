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
	private static final int cap = 16; //capacity - how many regionFiles to keep cached
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
	 * @param backupDir  where to look for a "region" folder
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
		RegionFileCache.cacheRAF(mcaFile, regionFile);
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
		RegionFileCache.cacheRAF.clear();
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
	
	
	/**
	 * TODO
	 * 
	 * @param world
	 * @param x
	 * @param z
	 * @return
	 */
	public static RegionFile getFromMinecraft(World world, int x, int z)
	{
		/* root directory of the world, the directory
		 * sharing the world's name and holding its 
		 * data. 'region' folder is found in here.
		 */
		File dir = world.getDataManager().getDirectory();

		return net.minecraft.server.v1_12_R1.RegionFileCache.a(dir, x, z);
	}
	
	
	
	/*---------------------------------Chunk Access---------------------------------*/
	
	/* RegionFiles keep their actual .mca files private, but those bytes can be useful 
	 * to read. The auxiliary cache below stores, for each cached RegionFile, a read-only 
	 * RandomAccessFile pointing to the .mca file, and an int[] storing the offset values
	 * for each chunk's bytes within the file.
	 */
	
	private static Map<RegionFile, 
	                   ChunkAccess> cacheRAF = new HashMap<RegionFile, 
	                                                       ChunkAccess>();
	
	/**
	 * TODO
	 */
	public static class ChunkAccess
	{
		//private static Field offsetField = RegionFile.class.getDeclaredField("d");
			
		final RandomAccessFile  bytes;
		final int[]             offsets;
		
		ChunkAccess(RandomAccessFile bytes, int[] chunkOffsets)
		{
			this.bytes   = bytes;
			this.offsets = chunkOffsets;
		}
	}
	
	/**
	 * TODO
	 * 
	 * @param file
	 * @param regionFile
	 */
	private static void cacheRAF(File file, RegionFile regionFile)
	{
		
	}
	
	
	/**
	 * TODO
	 * 
	 * @param regionFile
	 * @return
	 */
	public static RandomAccessFile getRAF(RegionFile regionFile)
	{
		return null;//TODO
	}
	
	
	/**
	 * TODO
	 * 
	 * @param regionFile
	 * @param x
	 * @param z
	 * @return
	 */
	public static int getChunkByteLength(RegionFile regionFile, int x, int z)
	{
		return 0;//TODO
	}
}
