package simpleWarBackup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.server.v1_12_R1.RegionFile;

/**
 * TODO write this
 */
public class RegionFileCache 
{
	private static final Map<File, RegionFile> regionFiles = new HashMap<File, RegionFile>();
	
	/**
	 * Copied from net.minecraft.server.RefionFileCache.a(File, int, int). Returns any
	 * cached RegionFile mapped to the given File. Not finding one, creates and caches, 
	 * then returns, a new RegionFile. Before caching a new RegionFile, checks that the 
	 * cache contains fewer than 16 RegionFiles, and, finding 16 or more, calls 
	 * {@link #clearCache() RegionFileCache.clearCace()}.
	 * 
	 * @param backupFolder  folder containing "region" folder
	 * @param x             chunk X
	 * @param z             chunk Z
	 * @return
	 */
	public static synchronized RegionFile get(File backupFolder, int x, int z) 
	{
		x >>= 5; z >>= 5;
		File regionFolder      = new File(backupFolder, "region");
		File regionFileFile    = new File(regionFolder, "r." + x + "." + z + ".mca");
		RegionFile regionFile  = regionFiles.get(regionFileFile);

		if (regionFile != null) 
			return regionFile;
		if (!regionFolder.exists())
			regionFolder.mkdirs();
		if (regionFiles.size() >= 16)
			clearCache();

		regionFile = new RegionFile(regionFileFile);
		RegionFileCache.regionFiles.put(regionFileFile, regionFile);
		return regionFile;
	}
	
	
	/**
	 * Copied from net.minecraft.server.RegionFileCache.a(). Iterates through all cached
	 * RegionFiles, closing each one's private RandomAccessFile, then clears the cache.
	 */
	public static synchronized void clearCache()
	{
		Iterator<RegionFile> iterator = RegionFileCache.regionFiles.values().iterator();
		
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
		RegionFileCache.regionFiles.clear();
	}
}
