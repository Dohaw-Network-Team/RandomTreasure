package me.caleb.RandomTreasure;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import io.netty.util.internal.ThreadLocalRandom;

public class Main extends JavaPlugin{

	static Main instance;
	
	public void onEnable() {
		instance = this;
		this.saveResource("config.yml", false);
		generateStructures();
		
		if(this.getConfig().getStringList("Structures").isEmpty()) {
			generateStructures();
		}else {
			takeDownStructures();
			generateStructures();
		}
		
	}
	
	public void onDisable() {
		
	}
	
	public static Main getMain() {
		return instance;
	}
	
	public void generateStructures() {
		Bukkit.getLogger().fine("Generating shrines...");
		ArrayList<Location> shrines = new ArrayList<Location>();
		
		World w = ConfigManager.getWorld();
		Location startLoc = w.getSpawnLocation().add(ThreadLocalRandom.current().nextDouble(500, 1500), 0, ThreadLocalRandom.current().nextDouble(500, 1500));
		shrines.add(startLoc);
		
		for(int x = 1; x <= 10; x++) {
			int index = shrines.size()-1;
			Location newShrine = shrines.get(index).add(getRandX(), 5, getRandZ());
			newShrine.getBlock().setType(Material.DIAMOND_BLOCK);
			shrines.add(newShrine);
			ConfigManager.addShrine(newShrine);
		}
		
	}
	
	public void takeDownStructures() {
		Bukkit.getLogger().fine("Taking down old shrines...");
		List<String> shrines = ConfigManager.getStructures();
		for(String line : shrines) {
			String[] arrLine = line.split(" ");
			Location shrine = new Location(ConfigManager.getWorld(),getLineX(arrLine), getLineY(arrLine), getLineZ(arrLine));
			shrine.getBlock().setType(Material.AIR);
		}
		this.getConfig().set("Structures", new ArrayList<String>());
	}
	
	public double getRandX() {
		return ThreadLocalRandom.current().nextDouble(ConfigManager.getMinBound(), ConfigManager.getMaxBound());
	}
	
	public double getRandZ() {
		return ThreadLocalRandom.current().nextDouble(ConfigManager.getMinBound(), ConfigManager.getMaxBound());
	}
	
	public double getLineX(String[] arrLine) {
		return Double.parseDouble(arrLine[0].substring(2));
	}
	
	public double getLineY(String[] arrLine) {
		return Double.parseDouble(arrLine[1].substring(2));
	}
	
	public double getLineZ(String[] arrLine) {
		return Double.parseDouble(arrLine[2].substring(2));
	}
	
}
