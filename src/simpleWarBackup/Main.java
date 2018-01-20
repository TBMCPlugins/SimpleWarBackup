package simpleWarBackup;

import java.io.File;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.ChunkRegionLoader;
import net.minecraft.server.v1_12_R1.DataConverterManager;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.World;

public class Main extends JavaPlugin implements Listener
{
	
	
	private static File dataFolder, 
						backupsFolder;
	
	public static final HashMap<org.bukkit.World, HashMap<String, File>> backups
				  = new HashMap<org.bukkit.World, HashMap<String, File>>();
	
	/**
	 * TODO
	 */
	public static void mapBackupDirectoriesToWorlds()
	{
		/* Directories that do not exist will be made when
		 * the plugin attempts to save a backup for the
		 * given world.
		 */
		
		HashMap<String, File> map;
		
		File worldFolder;
		File backupFolder;
		
		for (org.bukkit.World world : Bukkit.getWorlds())
		{
			backups.put(world, map = new HashMap<String, File>());
			
			worldFolder = new File(backupsFolder, world.getName());
			for (String backupName : worldFolder.list())
			{
				backupFolder = new File(worldFolder, backupName);
				map.put(backupName, backupFolder);
			}
		}
	}
	
	
	public void onEnable()
	{
		dataFolder	  = this.getDataFolder(); 
		backupsFolder = new File(dataFolder, "Backup Files");
		
		dataFolder.mkdir();
		backupsFolder.mkdir();
		
		mapBackupDirectoriesToWorlds();
	}
	
	public boolean backupChunk(org.bukkit.World bukkitWorld, int chunkX, int chunkZ)
	{
		Chunk chunk = ((CraftChunk) bukkitWorld.getChunkAt(chunkX, chunkZ)).getHandle();
		World world = ((CraftWorld) bukkitWorld).getHandle();
		
		return true;
	}
}
