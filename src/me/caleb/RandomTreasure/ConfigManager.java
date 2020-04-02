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
		if(Bukkit.getWorlds().contains(Bukkit.getWorld("World"))) {
			return Bukkit.getWorld(config.getString("World"));
		}else {
			return null;
		}
	}
	
	public static void addShrine(Location loc, int num) {
		config.set("FirstShrines.Shrine" + num + ".Location", "X:" + loc.getX() + " Y:" + loc.getY() + " Z:" + loc.getZ());
		config.set("FirstShrines.Shrine" + num + ".ConqueredBy", "none");
		plugin.saveConfig();
	}
	
	public static int getNumShrines() {
		return config.getInt("NumShrines");
	}
	
	public static double getMinBound() {
		return Double.parseDouble(config.getString("MinBound"));
	}
	
	public static double getMaxBound() {
		return Double.parseDouble(config.getString("MaxBound"));
	}
	
	public static List<String> getTreasureShrineLocs() {
		List<String> shrines = new ArrayList<String>();
		
		for(int x = 1; x <= getNumShrines(); x++) {
			shrines.add(config.getString("FirstShrines.Shrine" + x + ".Location"));
		}
		
		return shrines;
	}
	
}
