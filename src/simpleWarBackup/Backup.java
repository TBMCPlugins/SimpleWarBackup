package simpleWarBackup;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import simpleWarBackup.exceptions.NameCollisionException;

/**
 * TODO write javadoc
 */
public class Backup 
{
	/**
	 * TODO write javadoc
	 */
	private final HashMap<Integer, //town
	                  HashMap<UUID, //world
	                      HashMap<Long, //region
	                          RegionChunkList>>> tree 
	        = new HashMap<Integer,
	                  HashMap<UUID,
	                      HashMap<Long,
	                          RegionChunkList>>>();
	
	/**
	 * TODO write javadoc
	 */
	private final File directory;
	private final String name;
		
	/**
	 * TODO write javadoc (this is the default constructor)
	 * 
	 * @param name 
	 * @throws NameCollisionException
	 */
	Backup(String name, Main plugin) throws NameCollisionException
	{
		checkName(this.name = name); //throws NameCollisionException
		
		Server      server = plugin.getServer();
		List<World> worlds = server.getWorlds();
		
		maketree(TownyUniverse.getDataSource().getTowns(), worlds, server);
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
	
	
	
	private void maketree(List<Town> towns, List<World> worlds, Server server)
	{
		HashMap<UUID, HashMap<Long, RegionChunkList>> townbranch;
		              HashMap<Long, RegionChunkList>  worldbranch;
		
		for (Town town : towns)
		{
			townbranch = new HashMap<UUID, HashMap<Long, RegionChunkList>>();
			splitbranch(townbranch, worlds);
			addleaves(townbranch, town, worlds, server);
			tree.put(town.getUID(), townbranch);
		}
	}
	
	
	/**
	 * TODO write javadoc
	 * 
	 * @param townbranch
	 * @param worlds
	 */
	private static void splitbranch(
			HashMap<UUID, HashMap<Long, RegionChunkList>> townbranch,
			List<World> worlds)
	{
		for (World world : worlds)
		{
			townbranch.put(world.getUID(), new HashMap<Long, RegionChunkList>());
		}
	}
	
	
	/**
	 * TODO write javadoc
	 * 
	 * @param townbranch
	 * @param town
	 * @param worlds
	 * @param server
	 */
	private static void addleaves(
			HashMap<UUID, HashMap<Long, RegionChunkList>> townbranch,
			Town town, List<World> worlds, Server server)
	{
		long region;
		RegionChunkList chunklist;
		HashMap<Long, RegionChunkList> worldbranch;
		for (TownBlock block : town.getTownBlocks())
		{
			worldbranch = townbranch.get(server.getWorld(block.getWorld().getName()).getUID());
			
			int x = block.getX();
			int z = block.getZ();
			
			//round down to the nearest multiple of 16
			if (x < 0) x -= 16 + (x % 16); else x -= x % 16;
			if (z < 0) z -= 16 + (z % 16); else z -= z % 16;
			
			//TODO finish this fucking method, and this fucking plugin
		}
	}
}
