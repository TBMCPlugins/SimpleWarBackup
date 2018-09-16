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
	// --------------------- STATIC ---------------------
	
	/**
	 * TODO write javadoc
	 */
	static File pluginDir;
	
	/**
	 * TODO write javadoc
	 */
	static File backupsDir;
	
	/**
	 * Defines two File variables: for the plugin's data folder, and for the "Backup Files" 
	 * folder. Does not create either directory. The directories themselves are created 
	 * as-needed, elsewhere in the plugin, during the process of saving a backup.
	 * 
	 * @param plugin
	 */
	static void initialize(JavaPlugin plugin)
	{
		pluginDir  = plugin.getDataFolder();
		backupsDir = new File(pluginDir, "Backup Files");
	}
	
	
	// -------------------- INSTANCE --------------------
	
	
	public void onEnable()
	{
		initialize(this);
		
		getCommand("warbackup").setExecutor(this);
	}
	
	
	
	/**
	 * TODO write javadoc
	 */
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		//world.isChunkLoaded(x,z);
		return true;
	}
}
