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

import simpleWarBackup.RegionChunkList.Coord;
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
		
		File        folder = new File(plugin.getDataFolder(), name);
		Server      server = plugin.getServer();
		List<World> worlds = server.getWorlds();
		
		maketree(TownyUniverse.getDataSource().getTowns(), worlds, server, folder);
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
	
	
	/**
	 * TODO write javadoc
	 * 
	 * @param towns
	 * @param worlds
	 * @param server
	 * @param folder
	 * @throws IOException 
	 */
	private void maketree(List<Town> towns, List<World> worlds, Server server, File folder) throws IOException
	{
		 
		
		HashMap<String,  HashMap<Coord, RegionChunkList>> townbranch;
		                 HashMap<Coord, RegionChunkList>  worldbranch;
		
		RegionChunkList  chunklist;
		Coord            coord;
		int              x,z;
		
		String           worldname;
		
		File             townDir, 
		                 worldDir, 
		                 chunksDir,
		                 chlistDir;
		
		/*
		 * 
		 */
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
				
				x = block.getX() >> 4;
				z = block.getZ() >> 4;
				
				coord = new Coord(x >> 5, z >> 5);
				chunklist = worldbranch.get(coord);
				
				if (chunklist == null)
				{
					townDir   = new File(folder, town.getUID().toString());
					worldDir  = new File(townDir, worldname);
					chunksDir = new File(worldDir, "region chunk lists");
					chlistDir = new File(chunksDir, "r."+coord.x+"."+coord.z+".chunklist");
					
					chunklist = new RegionChunkList(chlistDir);
					worldbranch.put(coord, chunklist);
				}
			}
			
			tree.put(town.getUID(), townbranch);
		}
	}
	
	

	/**
	 * Holds region coordinates. A region is 32x32 chunks. A chunk is 16x16 blocks.<p>
	 * 
	 * The chunk coordinates of a block are (block x >> 4, block z >> 4).<p> 
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
