package simpleWarBackup;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.plugin.java.JavaPlugin;

import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.RegionFile;
import net.minecraft.server.v1_12_R1.World;

public class BackupIO 
{
	private static File pluginDir, backupsDir;
	
	protected static void initialize(JavaPlugin plugin)
	{
		pluginDir  = plugin.getDataFolder();
		backupsDir = new File(pluginDir, "Backup Files");
	}
	
	
	/**
	 * TODO
	 * 
	 * @param world
	 * @param backup
	 * @return
	 */
	private static File getBackupDir(CraftWorld world, String backup)
	{
		return new File(new File(backupsDir, world.getName()), backup);
	}
	
	
	/**
	 * TODO
	 * 
	 * @param world
	 * @param backup
	 * @param chunks
	 * @throws IOException
	 */
	public static void backup(
			CraftWorld world, String backup, CraftChunk... chunks) 
			throws IOException
	{
		final World            worldNMS  = world.getHandle();
		      Chunk            chunkNMS;
		      NBTTagCompound   chunkNBT;  // to be serialized
		
		final File             backupDir = getBackupDir(world, backup);
		      int              x, z;
		      RegionFile       regionFile;
		      DataOutputStream chunkPipe; // serialized to this
		
		/* chunkPipe is a series of data outputs pointing to a ChunkBuffer. 
		 * The internal class ChunkBuffer is defined within RegionFile, and 
		 * extends ByteArrayOutputStream. It holds a byte[] buffer and two 
		 * int values representing the chunk's coordinates.
		 */
		      
		for (CraftChunk chunk : chunks)
		{
			chunkNMS   = chunk.getHandle();
			chunkNBT   = ChunkNBTTools.save(chunkNMS, worldNMS);
			
			x          = chunk.getX();
			z          = chunk.getZ();
			regionFile = RegionFileCache.get(backupDir, x, z);
			chunkPipe  = regionFile.b(x & 31, z & 31);
			
			/* below serializes chunkNBT and writes the bytes to chunkPipe, 
			 * which then, on close(), writes those bytes into the RegionFile's 
			 * internal RandomAccessFile, which points to the actual .mca file.
			 */
			NBTCompressedStreamTools.a(chunkNBT, (DataOutput) chunkPipe);
			chunkPipe.close();
		}
	}
	
	
	/**
	 * TODO
	 * 
	 * @param world
	 * @param backup
	 * @param chunks
	 */
	private static void restoreWhenSafe(CraftWorld world, String backup, long[] chunks)
	{
		
	}
	
	
	/**
	 * TODO
	 * 
	 * @param world
	 * @param backup
	 */
	private static void restoreWhenSafe(CraftWorld world, String backup)
	{
		/*
		File worldDir  = new File(backupsDir, world.getName());
		File backupDir = new File(worldDir, backup);
		File regionDir = new File(backupDir, "region");
		
		if (!regionDir.exists() ||
		    !regionDir.isDirectory()) 
			return;
		
		String[] regionFileNames = regionDir.list();
		if (regionFileNames == null)
			return;
		
		for (String regionFileName : regionFileNames)
		{
			//TODO BLAHAHAHABLHABLHBALJHBFLJDHBFKWYBFKUHEBF
		}
		*/
	}
	
	
	
	public static boolean restoreTest(int x, int z) throws IOException
	{
		CraftWorld world = (CraftWorld) Bukkit.getWorld("world");
		
		File worldDir  = new File(backupsDir, "world");
		File backupDir = new File(worldDir, "test");
		File regionDir = new File(backupDir, "region");
		File mcaFile   = new File(regionDir, "r.1.1.mca");
		
		RegionFile regionFileSource = RegionFileCache.get(backupDir, x, z);
		RegionFile regionFileTarget = RegionFileCache.getFromMinecraft(world, x, z);
		
		DataInputStream  dataInput  = regionFileSource.a(x & 31, z & 31);
		DataOutputStream dataOutput = regionFileTarget.b(x & 31, z & 31);
		
		/* Look in RegionFile to find how to get length.
		 * Length is recorded in the first 4 bytes of each
		 * chunk's data, in the body of the region file. 
		 * 
		 * Read header for chunk data location, seek to
		 * that location and read first 4 bytes. This is
		 * the length for byte[] buf below.
		 */
		/*int length;
		{
			RandomAccessFile raf = RegionFileCache.getRAF(regionFileSource);
			if (raf == null) return false;
			...
		}
		
		byte[] buf = new byte[length];
		
		dataInput.readFully(buf);
		dataOutput.write(buf);
		dataOutput.close();*/
		
		if (dataInput == null)
			return false;
		
		NBTCompressedStreamTools.a(
				NBTCompressedStreamTools.a(dataInput), 
				(java.io.DataOutput) dataOutput);
		
		dataOutput.close();
		
		return true;
	}
}
