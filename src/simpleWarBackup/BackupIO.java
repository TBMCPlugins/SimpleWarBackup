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
	
	/**
	 * Defines two File variables: for the plugin's data folder, and for the "Backup Files" 
	 * folder. Does not create either directory. The directories themselves are created 
	 * as-needed, elsewhere in the plugin, during the process of saving a backup.
	 * 
	 * @param plugin
	 */
	static void initialize(JavaPlugin plugin) //called from Main onEnable()
	{
		pluginDir  = plugin.getDataFolder();
		backupsDir = new File(pluginDir, "Backup Files");
	}
	
	
	/**
	 * Returns the parent directory containing the "region" destination directory, where
	 * the .mca files are stored. The path proceeds: backups > backup > town > world. 
	 * This method returns the world directory.<p>
	 * 
	 * Note: this method does not create any of these directories. All directories are
	 * created as-needed, elsewhere in the plugin, during the process of saving a backup.
	 * 
	 * @param backup TODO
	 * @param town
	 * @param world
	 * @return
	 */
	private static File getDir(String backup, String town, CraftWorld world)
	{
		return new File(
		       new File(
		       new File(backupsDir, backup          ) 
		                          , town            )
		                          , world.getName() );
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
			String backup, String town, CraftWorld world, CraftChunk... chunks) 
			throws IOException
	{
		final World            worldNMS  = world.getHandle();
		      Chunk            chunkNMS;
		      NBTTagCompound   chunkNBT;  // to be serialized
		
		final File             worldDir = getDir(backup, town, world);
		      int              x, z;
		      RegionFile       regionFile;
		      DataOutputStream chunkPipe; // serialized to this
		
		/* chunkPipe is a series of data outputs pointing to a ChunkBuffer. 
		 * ChunkBuffer is an internal class defined within RegionFile that 
		 * extends ByteArrayOutputStream. It holds a byte[] buffer and two 
		 * int values representing the chunk's coordinates.
		 */
		      
		for (CraftChunk chunk : chunks)
		{
			chunkNMS   = chunk.getHandle();
			chunkNBT   = ChunkNBTTools.save(chunkNMS, worldNMS);
			
			x          = chunk.getX();
			z          = chunk.getZ();
			regionFile = RegionFileCache.get(worldDir, x, z);
			chunkPipe  = regionFile.b(x & 31, z & 31);
			
			/* below serializes chunkNBT, and writes those bytes to chunkPipe, 
			 * which then, on close(), writes the bytes into this RegionFile's 
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
	 * @param chunks  (chunk coords, larger half is z)
	 * 
	 * @throws IOException 
	 */
	private static void restore(
			String backup, String town, CraftWorld world, long... chunks) 
			throws IOException
	{
		final File             worldDir = getDir(backup, town, world);
		
		      int              x, z, i = 0;
		      
		      RegionFile       source;
		      RegionFile       target;
		      DataInputStream  dataInput;
		      DataOutputStream dataOutput;
		      
		      int              length;
		      byte[]           buf;
		
		/* dataInput is the end of a series of inputs, which proceed in 
		 * the following order: ByteArrayInputStream > GZIPInputStream >
		 * BufferedInputStream > DataInputStream.
		 * 
		 * dataOutput is a series of outputs pointing to a ChunkBuffer. 
		 * ChunkBuffer is an internal class defined within RegionFile that 
		 * extends ByteArrayOutputStream. It holds a byte[] buffer and two 
		 * int values representing the chunk's coordinates.
		 */
		for (long chunk : chunks)
		{
			x          = (int)   chunk;
			z          = (int) ( chunk >> 32 );
			
			source     = RegionFileCache.get(worldDir, x, z);
			target     = RegionFileCache.getFromMinecraft(world, x, z);
			dataInput  = source.a(x & 31, z & 31);
			dataOutput = target.b(x & 31, z & 31);
			
			length     = RegionFileCache.getChunkLength(source, x, z);
			
			/* getChunkLength(...) attempts to read from RegionFile source's 
			 * private .mca file. It will return length 0 if there is an error
			 * or there is no chunk data. Otherwise, it will return the length 
			 * value recorded in the file.
			 * 
			 * below writes the backup chunk data into the target region file,
			 * overwriting the current chunk data in use by the game.
			 */
			if (length > 0)
			{
				buf = new byte[length];
				dataInput.readFully(buf);
				dataOutput.write(buf);
				
				chunks[i] = 9223372034707292159L; 
				
				/* the value 9223372034707292159 is equivalent to the expression:
				 * ((long) Integer.MAX_VALUE) | (((long) Integer.MAX_VALUE) << 32)
				 * 
				 * coordinates in long[] 'chunks' are set to this value when they
				 * have been written successfully back into the game world. This
				 * value is outside the range of natural chunk coordinate pairs.
				 */
			}
			dataOutput.close(); //writes to the target file
			dataInput.close();
			i++;
		}
	}
	
	
	/**
	 * TODO
	 * 
	 * @param world
	 * @param backup
	 */
	private static void restoreAll(CraftWorld world, String backup)
	{
		
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
