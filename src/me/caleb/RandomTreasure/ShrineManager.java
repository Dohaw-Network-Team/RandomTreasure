package me.caleb.RandomTreasure;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ShrineManager extends BukkitRunnable{

	private Main plugin;
	private Location loc;
	
	public ShrineManager(Main plugin, Location loc) {
		this.plugin = plugin;
		this.loc = loc;
	}

	@Override
	public void run() {
		List<Player> nearbyEntities = getNearbyEntities(loc, 15);
		
	}
	
	public List<Player> getNearbyEntities(Location l, int radius) {
	    int chunkRadius = radius < 16 ? 1 : (radius - (radius % 16)) / 16;
	    List<Player> radiusEntities = new ArrayList<Player>();
	    for (int chX = 0 - chunkRadius; chX <= chunkRadius; chX++) {
	        for (int chZ = 0 - chunkRadius; chZ <= chunkRadius; chZ++) {
	            int x = (int) l.getX(), y = (int) l.getY(), z = (int) l.getZ();
	            for (Entity e : new Location(l.getWorld(), x + (chX * 16), y, z
	                    + (chZ * 16)).getChunk().getEntities()) {
	                if (e.getLocation().distance(l) <= radius
	                        && e.getLocation().getBlock() != l.getBlock()) {
	                	if(e instanceof Player) {
	                		
	                	}
	                    radiusEntities.add((Player) e);
	                }
	            }
	        }
	     }
	     return radiusEntities;
	}
	
	
	
}
