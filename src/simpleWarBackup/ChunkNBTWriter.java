package simpleWarBackup;

import java.util.Iterator;
import java.util.List;

import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.ChunkSection;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.MinecraftKey;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagList;
import net.minecraft.server.v1_12_R1.NextTickListEntry;
import net.minecraft.server.v1_12_R1.NibbleArray;
import net.minecraft.server.v1_12_R1.TileEntity;
import net.minecraft.server.v1_12_R1.World;

/**
 * Methods for writing a chunk's data to NBT. These have been copied from private, static
 * methods in ChunkRegionLoader, an NMS class. Changes have been commented, so that these
 * methods may be more easily updated as Spigot releases newer versions.
 */
public final class ChunkNBTWriter 
{
	/**
	 * To find the new data version after an update,
	 * open ChunkRegionLoader in an editor and search 
	 * for "DataVersion"
	 */
	private static final int DATA_VERSION = 1343;
	
	
	/**
	 * TODO Loosely copied from an NMS void method called saveChunk. 
	 * 
	 * @param chunk
	 * @param world
	 * @param DATA_VERSION
	 * @return
	 */
	public static NBTTagCompound write(Chunk chunk, World world)
	{
		NBTTagCompound chunkNBT = new NBTTagCompound();
        NBTTagCompound levelNBT = new NBTTagCompound();

        chunkNBT.set("Level", levelNBT);
        chunkNBT.setInt("DataVersion", DATA_VERSION);
        
        ChunkNBTWriter.saveEntities(levelNBT, chunk, world);
        ChunkNBTWriter.saveBody(levelNBT, chunk, world);
        
        return chunkNBT;
	}
	
	
	
	/**
	 * Copied from a private method of the same name in the NMS class ChunkRegionLoader,
	 * version 1.12.2. Local variables have been renamed and some passages reformatted.
	 * ChunkRegionLoader 1.12.2 calls this private method only once, in saveChunk(...).
	 * 
	 * This method saves a chunk's entity data to the given NBTTagCompound. The given
	 * compound is assumed to be the chunk's "level" compound, i.e. the compound holding
	 * all of the chunk's information except its Data Version.
	 * 
	 * @param levelNBT
	 * @param chunk
	 * @param world
	 */
	private static void saveEntities(NBTTagCompound levelNBT, Chunk chunk, World world) 
	{
		//the three TagLists to be written to levelNBT
		NBTTagList			entitiesNBT		  = new NBTTagList();
		NBTTagList 			tileEntitiesNBT	  = new NBTTagList();
		NBTTagList			tileTicksNBT;
		
		//reusable iteration variables
		NBTTagCompound		compound;
		Iterator			iterator;
		Entity				entity;
		TileEntity			tileEntity;
		NextTickListEntry	nextTickListEntry;
		MinecraftKey		minecraftKey;
		
		
		//--------------------ENTITIES--------------------
		//chunk.g(false);
		for (int i = 0; i < chunk.getEntitySlices().length; ++i) 
		{
			iterator = chunk.getEntitySlices()[i].iterator();
			while (iterator.hasNext()) 
			{
				entity = (Entity) iterator.next();
				compound = new NBTTagCompound();
				if (entity.d(compound)) 
				{
					//chunk.g(true);
					entitiesNBT.add(compound);
				}
			}
		}
		levelNBT.set("Entities", entitiesNBT);
		
		
		//-----------------TILE ENTITIES------------------
		iterator = chunk.getTileEntities().values().iterator();
		while (iterator.hasNext()) 
		{
			tileEntity = (TileEntity) iterator.next();
			compound = tileEntity.save(new NBTTagCompound());
			tileEntitiesNBT.add(compound);
		}
		levelNBT.set("TileEntities", tileEntitiesNBT);
		
		
		//-------------------TILE TICKS-------------------
		List nextTickList = world.a(chunk, false);
		if (nextTickList != null) 
		{
			tileTicksNBT = new NBTTagList();
			long worldTime = world.getTime();
			
			iterator = nextTickList.iterator();
			while (iterator.hasNext()) 
			{
				nextTickListEntry = (NextTickListEntry) iterator.next();
				minecraftKey = (MinecraftKey) Block.REGISTRY.b(nextTickListEntry.a());
				compound = new NBTTagCompound();

				compound.setString("i", minecraftKey == null ? "" : minecraftKey.toString());
				compound.setInt("x", nextTickListEntry.a.getX());
				compound.setInt("y", nextTickListEntry.a.getY());
				compound.setInt("z", nextTickListEntry.a.getZ());
				compound.setInt("t", (int) (nextTickListEntry.b - worldTime));
				compound.setInt("p", nextTickListEntry.c);
				tileTicksNBT.add(compound);
			}
			levelNBT.set("TileTicks", tileTicksNBT);
		}

	}
	
	
	
	/**
	 * TODO cleanup, add more comments
	 * 
	 * @param levelNBT
	 * @param chunk
	 * @param worldTime
	 * @param worldHasSkyLight
	 */
	private static void saveBody(NBTTagCompound levelNBT, Chunk chunk, World world) 
	{
		/* passed variables worldTime and worldHasSkyLight have been 
		 * replaced with world. The values are now obtained directly:
		 */
		long worldTime = world.getTime();
		boolean worldHasSkyLight = world.worldProvider.m();
		
		levelNBT.setInt("xPos", chunk.locX);
		levelNBT.setInt("zPos", chunk.locZ);
		levelNBT.setLong("LastUpdate", worldTime);
		levelNBT.setIntArray("HeightMap", chunk.r());
		levelNBT.setBoolean("TerrainPopulated", chunk.isDone());
		levelNBT.setBoolean("LightPopulated", chunk.v());
		levelNBT.setLong("InhabitedTime", chunk.x());
		
		ChunkSection[] chunkSections = chunk.getSections();
		ChunkSection[] chunkSectionsCopy = chunkSections;
		
		NBTTagList chunkSectionsNBT = new NBTTagList();
		NBTTagCompound chunkSection;
		int i = chunkSections.length;

		
		for (int j = 0; j < i; ++j) {
			ChunkSection chunksection = chunkSectionsCopy[j];

			if (chunksection != Chunk.a) {
				chunkSection = new NBTTagCompound();
				chunkSection.setByte("Y", (byte) (chunksection.getYPosition() >> 4 & 255));
				byte[] abyte = new byte[4096];
				NibbleArray nibblearray = new NibbleArray();
				NibbleArray nibblearray1 = chunksection.getBlocks().exportData(abyte, nibblearray);

				chunkSection.setByteArray("Blocks", abyte);
				chunkSection.setByteArray("Data", nibblearray.asBytes());
				if (nibblearray1 != null) 
				{
					chunkSection.setByteArray("Add", nibblearray1.asBytes());
				}
				chunkSection.setByteArray("BlockLight", chunksection.getEmittedLightArray().asBytes());
				if (worldHasSkyLight)
					chunkSection.setByteArray("SkyLight", chunksection.getSkyLightArray().asBytes());
				else					
					chunkSection.setByteArray("SkyLight", new byte[chunksection.getEmittedLightArray().asBytes().length]);

				chunkSectionsNBT.add(chunkSection);
			}
		}

		levelNBT.set("Sections", chunkSectionsNBT);
		levelNBT.setByteArray("Biomes", chunk.getBiomeIndex());
	}
}
