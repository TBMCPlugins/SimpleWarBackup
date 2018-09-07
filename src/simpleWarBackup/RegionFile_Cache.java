package simpleWarBackup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import net.minecraft.server.v1_12_R1.RegionFile;
import net.minecraft.server.v1_12_R1.World;

/**
 * TODO write javadoc
 */
public final class RegionFile_Cache 
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
	 * Method copied from net.minecraft.server.RefionFileCache.a(File, int, int).<p>
	 * 
	 * If there is a cached RegionFile for the given file and coordinates, returns this. 
	 * Otherwise, creates, caches, then returns a new RegionFile for the given file and
	 * coordinates.
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
		RegionFile_Cache.cache.put(mcaFile, regionFile);
		ChunkAccess_Cache.createAndPut(mcaFile, regionFile);
		return regionFile;
	}
	
	
	/**
	 * Method Copied from net.minecraft.server.RegionFileCache.a().<p>
	 * 
	 * Called by {@link #get(File, int, int) RegionFile_Cache.get(File, int, int)}.<p>
	 * 
	 * Removes all entries from the cache.
	 */
	private static synchronized void clearCache()
	{
		Iterator<RegionFile> iterator = RegionFile_Cache.cache.values().iterator();
		
		/* regionFile.c() closes the private RandomAccessFile 
		 * inside the regionFile object, and that's all it does.
		 * The entire method: if (file != null) file.close();
		 */
		while (iterator.hasNext()) 
		{
			RegionFile regionFile = iterator.next();
			try { if (regionFile != null) regionFile.c(); } 
			catch (IOException e) { e.printStackTrace(); }
		}
		RegionFile_Cache.cache.clear();
		ChunkAccess_Cache.cache.clear();
	}
	
	
	/**
	 * TODO write javadoc, explain what method a(...) does
	 * 
	 * @param world
	 * @param x
	 * @param z
	 * @return
	 */
	public static RegionFile getFromMinecraft(CraftWorld world, int x, int z)
	{
		/* root directory of the world: the directory
		 * sharing the world's name and holding its 
		 * data. 'region' folder is found in here.
		 */
		File dir = world.getHandle().getDataManager().getDirectory();

		return net.minecraft.server.v1_12_R1.RegionFileCache.a(dir, x, z);
	}
	
	/**
	 * TODO write javadoc, explain what method a(...) does
	 * 
	 * @param world
	 * @param x
	 * @param z
	 * @return
	 */
	public static RegionFile getFromMinecraft(World world, int x, int z)
	{
		/* root directory of the world: the directory
		 * sharing the world's name and holding its 
		 * data. 'region' folder is found in here.
		 */
		File dir = world.getDataManager().getDirectory();

		return net.minecraft.server.v1_12_R1.RegionFileCache.a(dir, x, z);
	}
}
