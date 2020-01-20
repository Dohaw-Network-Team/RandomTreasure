package me.caleb.RandomTreasure;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

	public static Main plugin = Main.getMain();
	
	public static FileConfiguration config = plugin.getConfig();
	
	public static World getWorld() {
		try {
			if(Bukkit.getWorld(config.getString("World")) != null) {
				return Bukkit.getWorld(config.getString("World"));
			}
		}catch(IllegalArgumentException e) {
			Bukkit.getLogger().severe("The config field World is not set properly! Cannot generate Treasure Shrines!");
			plugin.getPluginLoader().disablePlugin(plugin);
		}

		return null;
	}
	
	public static void addShrine(Location loc) {
		
		List<String> shrines;
		
		if(config.getStringList("Structures").isEmpty()) {
			shrines = new ArrayList<String>();
		}else {
			shrines = config.getStringList("Structures");
		}
		
		shrines.add("X:" + loc.getX() + " Y:" + loc.getY() + " Z:" + loc.getZ());
		
		config.set("Structures", shrines);
		plugin.saveConfig();
	}
	
	public static double getMinBound() {
		return Double.parseDouble(config.getString("MinBound"));
	}
	
	public static double getMaxBound() {
		return Double.parseDouble(config.getString("MaxBound"));
	}
	
	public static List<String> getStructures(){
		return config.getStringList("Structures");
	}
	
}
