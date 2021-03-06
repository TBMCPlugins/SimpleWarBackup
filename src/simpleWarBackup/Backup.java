package simpleWarBackup;

import java.io.File;
import java.io.IOException;
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
	               HashMap<String, //world
	                HashMap<Coord, //region
	                 RegionChunkList>>> tree 
	               
	        = new HashMap<Integer,
	               HashMap<String,
	                HashMap<Coord,
	                 RegionChunkList>>>();
	
	/**
	 * TODO write javadoc
	 */
	private final File folder;
	private final String name;
		
	/**
	 * TODO write javadoc (this is the default constructor)
	 * 
	 * @param name 
	 * @throws IOException
	 * @throws NameCollisionException
	 */
	Backup(String name, Main plugin) throws IOException, NameCollisionException
	{
		checkName(this.name = name); //throws NameCollisionException
		
		folder = new File(plugin.getDataFolder(), name);
		
		Server      server = plugin.getServer();
		List<World> worlds = server.getWorlds();
		
		maketree(TownyUniverse.getDataSource().getTowns(), worlds, server);
	}
	
	
	/**
	 * TODO write javadoc
	 * 
	 * @param name
	 * @param plugin
	 * @throws IOException
	 * @throws NameCollisionException
	 */
	Backup(String name, Main plugin, String... townlist) 
			throws IOException, NameCollisionException
	{
		checkName(this.name = name); //throws NameCollisionException
		
		folder = new File(plugin.getDataFolder(), name);
		
		Server      server = plugin.getServer();
		List<World> worlds = server.getWorlds();
		
		maketree(TownyUniverse.getDataSource().getTowns(townlist), worlds, server);
	}
	
	
	/**
	 * TODO write javadoc
	 * 
	 * @param towns
	 * @param worlds
	 * @param server
	 * @param folder
	 * @throws IOException 
	 */
	private void maketree(List<Town> towns, List<World> worlds, Server server) throws IOException
	{
		HashMap<String,  HashMap<Coord, RegionChunkList>> townbranch;
		                 HashMap<Coord, RegionChunkList>  worldbranch;
		
		RegionChunkList  chunklist;
		Coord            coord;
		int              x,z;
		
		String           worldname;
		
		for (Town town : towns)
		{
			townbranch = new HashMap<String, HashMap<Coord, RegionChunkList>>();
			
			//make world sub-branches
			for (World world : worlds)
				townbranch.put(world.getName(), 
				               new HashMap<Coord, RegionChunkList>());
			
			//populate world sub-branches
			for (TownBlock block : town.getTownBlocks())
			{
				worldname = block.getWorld().getName();
				worldbranch = townbranch.get(worldname);
				
				//convert block to chunk
				x = block.getX() >> 4;
				z = block.getZ() >> 4;
				
				coord = new Coord(x >> 5, z >> 5);
				chunklist = worldbranch.get(coord);
				
				//create chunk list if nonexistent
				if (chunklist == null)
				{
					File townDir   = new File(folder, town.getUID().toString());
					File worldDir  = new File(townDir, worldname);
					File chunksDir = new File(worldDir, "region chunk lists");
					File chlistDir = new File(chunksDir, "r."+coord.x+"."+coord.z+".chunklist");
					
					chunklist = new RegionChunkList(chlistDir);
					worldbranch.put(coord, chunklist);
				}
				
				
				chunklist.storeChunk(x, z);
			}
			
			tree.put(town.getUID(), townbranch);
		}
	}
	
	
	
	/**
	 * TODO write javadoc
	 * 
	 * @param name
	 * @throws NameCollisionException
	 */
	private static void checkName(String name) throws NameCollisionException
	{
		for (String filename : Main.backupsDir.list()) if (filename.equals(name))
		{
			throw new NameCollisionException();
		}
	}
	

	/**
	 * Holds region coordinates. A region is 32x32 chunks. A chunk is 16x16 blocks.<p>
	 * 
	 * The chunk coordinates of a block are (block x >> 4, block z >> 4).<p> 
	 * 
	 * The region coordinates of a chunk are (chunk x >> 5, chunk z >> 5).<p>
	 */
	public static class Coord
	{
		final int x, z;
		
		/**
		 * @param x  region x coordinate
		 * @param z  region z coordinate
		 */
		public Coord(int x, int z)
		{
			this.x = x;
			this.z = z;
		}
	}
}
