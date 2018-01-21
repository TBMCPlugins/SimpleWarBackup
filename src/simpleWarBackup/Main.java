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
		
		File dataFolder = this.getDataFolder(); 
		BackupIO.initialize(dataFolder, 
		                    new File(dataFolder, 
		                    		"Backup Files"));
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (!(sender instanceof Player) || !sender.getName().equals("iie")) return false;
		
		Location loc = ((Player) sender).getLocation();
		
		World world = loc.getWorld();
		Chunk chunk = loc.getChunk();
		
		if (label.equals("testBackupChunk"))
		{
			try {
				BackupIO.backupChunks("test", world, chunk);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		else if (label.equals("testRestoreChunk"))
		{
			
		}
		return true;
	}
}
