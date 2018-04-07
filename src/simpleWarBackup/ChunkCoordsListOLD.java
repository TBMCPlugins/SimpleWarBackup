package simpleWarBackup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ChunkCoordsListOLD 
{
	private static final YamlConfiguration yml = new YamlConfiguration();
	private static final char ps = yml.options().pathSeparator();
	private static File file;
	
	/**
	 * TODO
	 * 
	 * @param plugin
	 */
	private static void initialize(JavaPlugin plugin)
	{
		file = new File(plugin.getDataFolder(), "Chunks.yml");
		
		/* "Garbage Bin" contains the names of backups that have already been fully
		 * written back into the game world, and are presumably no longer needed.
		 * Each entry includes a timestamp, and is deleted after 30 days.
		 * 
		 * "Storage" lists by coordinates the chunks stored in each of the backups. 
		 * Coordinates are stored as longs, with the smaller half representing x and 
		 * the larger half representing z. 
		 * 
		 * "Queue" lists by coordinates the chunks in each of the backups that are
		 * slated to be written back into the game world. Coordinates are stored
		 * in the same manner as in "Storage."
		 */
		try
		{
			if (file.exists()) 
			{ 
				yml.load(file);
				
				if (!yml.contains("Garbage Bin")) yml.createSection("Garbage Bin");
				if (!yml.contains("Storage"))     yml.createSection("Storage");
				if (!yml.contains("Queue"))       yml.createSection("Queue");
			}
			else
			{
				yml.createSection("Garbage Bin");
				yml.createSection("Storage");
				yml.createSection("Queue");
			}
			yml.save(file);
		}
		catch(IOException | InvalidConfigurationException e) { e.printStackTrace(); }
	}
	
	
	
	private static void write(String path, int chunkX)
	{
		
	}
	
	
	/**
	 * TODO
	 * 
	 * @param backup
	 * @param town
	 * @param world
	 * @param chunks  (chunk coords)
	 */
	static void backup(String backup, String town, String world, long... chunks)
	{
		String  path = "Storage" + ps + backup + ps + town + ps + world;
		
		int     x, z;
		int[]   rows;
		
		for (long chunk : chunks)
		{
			x = (int)   chunk;
			z = (int) ( chunk >> 32 );
			
			//write(path + ps + (x >> 5) + ", " + (z >> 5) + ps + (z & 31));
			
			
		}
	}
	
	
	static void queue(String backup, String town, String world, long... chunks)
	{
		yml.set("Queue" + ps + backup + ps + town + ps + world, chunks);
	}
}
