package simpleWarBackup;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.RegionFile;
import net.minecraft.server.v1_12_R1.World;

/**
 * TODO write javadoc
 */
public class BackupIO 
{
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
	private static File getDir(String backup, String town, String world)
	{
		return new File(new File (new File(Main.backupsDir, backup), town), world);
	}
	
	
	/**
	 * TODO
	 * 
	 * @param world
	 * @param backup
	 * @param chunks
	 * @throws IOException
	 */
	public static void backup(File worldDir, World world, Chunk... chunks) 
			throws IOException
	{
		NBTTagCompound   chunkNBT;   // to be serialized
		DataOutputStream dataOutput; // serialized to here
		
		/* dataOutput is the first of a series of outputs, which proceed in
		 * the following order: DataOutputStream > BufferedOutputStream >
		 * DeflaterOutputStream > RegionFile.ChunkBuffer
		 *  
		 * ChunkBuffer extends ByteArrayOutputStream. It holds a byte[] buffer,
		 * and two int values which represent the chunk's coordinates.
		 */
		      
		for (Chunk chunk : chunks)
		{
			chunkNBT  = ChunkNBTTools.save(chunk, world);
			dataOutput = RegionFile_Cache.get(worldDir, chunk.locX, chunk.locZ)
			                             .b(chunk.locX & 31, chunk.locZ & 31);
			
			/* a(...) serializes the chunk, and writes the bytes to chunkPipe.
			 * dataOutput.close() writes the bytes to the actual .mca file.
			 */
			NBTCompressedStreamTools.a(chunkNBT, (DataOutput) dataOutput);
			dataOutput.close();
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
	private static void restore(File worldDir, World world, long[] chunks) 
			throws IOException
	{
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
		 * dataOutput is the first of a series of outputs, which proceed in
		 * the following order: DataOutputStream > BufferedOutputStream >
		 * DeflaterOutputStream > RegionFile.ChunkBuffer
		 *  
		 * ChunkBuffer extends ByteArrayOutputStream. It holds a byte[] buffer,
		 * and two int values which represent the chunk's coordinates.
		 */
		for (long chunk : chunks)
		{
			x          = (int)   chunk;
			z          = (int) ( chunk >> 32 );
			
			source     = RegionFile_Cache.get(worldDir, x, z);
			target     = RegionFile_Cache.getFromMinecraft(world, x, z);
			dataInput  = source.a(x & 31, z & 31);
			dataOutput = target.b(x & 31, z & 31);
			
			length     = ChunkAccess_Cache.getChunkLength(source, x, z);
			
			/* getChunkLength(...) will return length 0 if there is an error
			 * or there is no chunk data. Otherwise, it will return the length 
			 * value recorded in the file.
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
				 * have been written successfully back into the game world, because 
				 * this value is outside the range of natural chunk coordinate pairs.
				 */
			}
			dataOutput.close(); //writes to target's .mca file
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
		//world.isChunkLoaded(x,z);
	}
}
