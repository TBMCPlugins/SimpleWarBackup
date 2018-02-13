package simpleWarBackup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener
{
	public void onEnable()
	{
		getCommand("testBackupChunk").setExecutor(this);
		getCommand("testRestoreChunk").setExecutor(this);
		
		BackupIO.initialize(this);
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (!(sender instanceof Player))      return false; 
		if (!sender.getName().equals("iie"))  return false;
		
		sender.sendMessage("some command received");
		Location loc = ((Player) sender).getLocation();
		World world  = loc.getWorld();
		Chunk chunk  = loc.getChunk();
		
		//test-chunks to be test-restored
		int[][] xz = {{187, 187}, 
		              {40 , 40 }};
		
		if (command.getName().equals("testBackupChunk"))
		{
			try { BackupIO.backup((CraftWorld) world, "test", (CraftChunk) chunk); }
			catch (IOException e) { e.printStackTrace(); }
		}
		else if (command.getName().equals("testRestoreChunk"))
		{
			sender.sendMessage("testRestoreChunk command received");
			for (int[] coords : xz)
			{
				if (world.isChunkLoaded(coords[0], coords[1]))
				{
					sender.sendMessage("chunk " + coords[0] + ", " + coords[1] + " is still loaded");
					return false;
				}
			}
			{
				sender.sendMessage("chunk is not loaded");
				try 
				{ 
					for (int[] coords : xz)
					{
						sender.sendMessage(
								BackupIO.restoreTest(coords[0], coords[1]) ? "restore worked" : 
								                                             "restore failed");
					}
				} 
			    catch (IOException e) 
				{ 
			    	e.printStackTrace(); 
			    	sender.sendMessage("IOException, restore failed");
			    }
			}
		}
		return true;
	}
}
