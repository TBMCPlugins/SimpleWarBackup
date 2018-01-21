package simpleWarBackup;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import net.minecraft.server.v1_12_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.RegionFile;

public class BackupIO 
{
	private static File dataFolder, backupsFolder;
	
	public static final HashMap<World, HashMap<String, File>> backups
	              = new HashMap<World, HashMap<String, File>>();
	
	/**
	 * TODO
	 * 
	 * @param dataFolder
	 * @param backupsFolder
	 */
	public static void initialize(File dataFolder, File backupsFolder)
	{
		BackupIO.dataFolder = dataFolder;
		BackupIO.backupsFolder = backupsFolder;
		BackupIO.dataFolder.mkdir();
		BackupIO.backupsFolder.mkdir();
		
		initializeBackupsMap();
	}
	
	/**
	 * TODO
	 */
	public static void initializeBackupsMap()
	{
		HashMap<String, File> map;
		File worldFolder;
		File backupFolder;
		
		/* Directories that do not exist will be made when
		 * the plugin attempts to save a backup for the
		 * given world.
		 */
		for (World world : Bukkit.getWorlds())
		{
			backups.put(world, map = new HashMap<String, File>());
			worldFolder = new File(backupsFolder, world.getName());
			if (worldFolder.exists() && worldFolder.list() != null)
			{	
				for (String backupName : worldFolder.list())
				{
					backupFolder = new File(worldFolder, backupName);
					map.put(backupName, backupFolder);
				}
			}
		}
	}
	
	
	/**
	 * TODO
	 * 
	 * @param backupFolder
	 * @param chunkX
	 * @param chunkZ
	 * @return
	 */
	public static DataOutputStream chunkDataOutputStream(File backupFolder, int chunkX, int chunkZ)
	{
		return RegionFileCache.get(backupFolder, chunkX, chunkZ)    // get(...) returns a RegionFile
		                      .b(chunkX & 31, chunkZ & 31);         //   b(...) returns a DataOutputStream
	}
	
	
	/**
	 * TODO
	 * 
	 * @param backup
	 * @param world
	 * @param chunks
	 * @throws IOException
	 */
	public static void backupChunks(String backup, World world, Chunk... chunks) throws IOException
	{
		final File folder = backups.get(world).get(backup);
		final net.minecraft.server.v1_12_R1.World worldNMS
			= ((CraftWorld) world).getHandle();
		
		NBTTagCompound chunkNBT;
		DataOutputStream output;
		
		for (Chunk chunk : chunks)
		{
			chunkNBT = ChunkNBTWriter.write(((CraftChunk) chunk).getHandle(), worldNMS);
			output = BackupIO.chunkDataOutputStream(folder, chunk.getX(), chunk.getZ());
			
			NBTCompressedStreamTools.a(chunkNBT, (DataOutput) output);
			output.close();
		}
	}
}
