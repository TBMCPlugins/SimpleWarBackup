package simpleWarBackup;

import java.io.File;
import java.util.HashMap;

import org.bukkit.Bukkit;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;

import simpleWarBackup.exceptions.NameCollisionException;

public class Backup 
{
	private final HashMap<String, //town
	                  HashMap<String, //world
	                      HashMap<Long, //region
	                          RegionChunkList>>> lists 
	        = new HashMap<String,
	                  HashMap<String,
	                      HashMap<Long,
	                          RegionChunkList>>>();
	
	private final String name;
	private final File directory;
		
	/**
	 * TODO write javadoc (this is the default constructor)
	 * 
	 * @param name 
	 * @throws NameCollisionException
	 */
	Backup(String name) throws NameCollisionException
	{
		checkName(this.name = name); //throws NameCollisionException
		
		//com.palmergames.bukkit.towny.object.TownyUniverse.getWorld(null).getTowns();
	}
	
	/**
	 * TODO write javadoc
	 * 
	 * @param name
	 * @throws NameCollisionException
	 */
	private static void checkName(String name) throws NameCollisionException
	{
		for (String filename : Main.backupsDir.list()) if (filename == name)
		{
			throw new NameCollisionException();
		}
	}
}
