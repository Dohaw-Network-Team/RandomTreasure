package me.caleb.RandomTreasure;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

/*
 * Created by c10coding. Plugin SpongeX
 * 
 * Description: A plugin used to claim areas via sponge.
 */

public class LocationSerializer {

	private FileConfiguration config;
	
	public LocationSerializer(FileConfiguration config) {
		this.config = config;
	}
	
	public void storeLocation(String path, Location loc) {
		config.set(path, "World: " + loc.getWorld().getName() + ";X: " + loc.getX() + ";Y: " + loc.getY() + ";Z: " + loc.getZ());
	}
	
	public void saveConfig() {
		try {
			config.save(config.getName() + ".yml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Location toLocation(String line) {
		World w;
		double x,y,z;
		
		String[] arrLine = line.split(";");
		w =  Bukkit.getWorld(arrLine[0].substring(7));
		x = Double.parseDouble(arrLine[1].substring(3));
		y = Double.parseDouble(arrLine[2].substring(3));
		z = Double.parseDouble(arrLine[3].substring(3));
		
		return new Location(w, x, y, z);
	}
	
}
