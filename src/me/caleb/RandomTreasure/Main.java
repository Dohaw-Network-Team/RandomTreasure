package me.caleb.RandomTreasure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin{

	static Main instance;
	
	public void onEnable() {
		instance = this;
		
		File[] files = {new File(this.getDataFolder(), "config.yml")};
		
		for(File f : files) {
			if(!f.exists()) {
				this.saveResource(f.getName(), true);
			}
		}
		
		if(ConfigManager.getTreasureShrineLocs().isEmpty()) {
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
		
		World w;
		
		try {
			w = ConfigManager.getWorld();
		}catch(NullPointerException e) {
			Bukkit.getLogger().severe("Cannot generate Shrines. Please fix the world field in the config!");
			return;
		}
		
		Location spawnLoc;
		try {
			spawnLoc = w.getSpawnLocation();
		}catch(NullPointerException e) {
			Bukkit.getLogger().severe("The config field World is not set properly! Cannot generate Treasure Shrines!");
			return;
		}
		
		Location startLoc = new Location(w, getRandX(), 100, getRandZ());
		startLoc = adjustShrine(startLoc);
		shrines.add(startLoc);
		
		for(int x = 1; x <= ConfigManager.getNumShrines(); x++) {
			Location newShrine = new Location(w, getRandX(), 100, getRandZ());
			
			if(shrines.size() != 1) {
				for(int i = 0; i < shrines.size(); i++) {
					if(shrines.get(i).distance(newShrine) > 750 && newShrine.distance(spawnLoc) > 750) {
						newShrine = new Location(w, getRandX(), 100, getRandZ());
					}
				}
			}
			
			newShrine = adjustShrine(newShrine);
			
			try {
				savePreviousArea(newShrine, x);
				pasteShrineSchematic(w, newShrine);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			LocationSerializer ls = new LocationSerializer(this.getConfig());
			ls.storeLocation("FirstShrines." + "Shrine" + x + ".Location", newShrine);
			
			this.getConfig().set("FirstShrines." + "Shrine" + x + ".ConqueredBy", "none");
			
			this.saveConfig();
			
			
			
			shrines.add(newShrine);
		}
		
	}
	
	public void takeDownStructures() {
		
		Bukkit.getConsoleSender().sendMessage("Taking down shrines...");
		List<Location> shrines = ConfigManager.getTreasureShrineLocs();
		
		int counter = 1;
		double x,y,z;
		
		World w = ConfigManager.getWorld();
		
		for(Location loc : shrines) {
			
			z = loc.getZ() - 8;
			x = loc.getX() - 6;
			y = loc.getY();
			
			Location shrine = new Location(ConfigManager.getWorld(),x, y, z);
			
			if(counter == 11) {
				return;
			}else {
				try {
					pasteRestoreSchematic(w, shrine, counter);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch(NullPointerException e) {
					Bukkit.getLogger().severe("Cannot generate Shrines. Please fix the world field in the config!");
					return;
				}
			}

			counter++;		
		}
			
	}
	
	public double getRandX() {
		return ThreadLocalRandom.current().nextDouble(ConfigManager.getMinBound(), ConfigManager.getMaxBound());
	}
	
	public double getRandZ() {
		return ThreadLocalRandom.current().nextDouble(ConfigManager.getMinBound(), ConfigManager.getMaxBound());
	}
	
	public Location adjustShrine(Location loc) {
		
		Location startLoc = loc;
		Material currentBlock = startLoc.getBlock().getType();
		Material[] temp = {Material.ACACIA_LEAVES,Material.BIRCH_LEAVES,Material.DARK_OAK_LEAVES,Material.JUNGLE_LEAVES,Material.OAK_LEAVES,Material.SPRUCE_LEAVES};
		List<Material> leaves = Arrays.asList(temp);
		
		while(currentBlock.equals(Material.AIR) || leaves.contains(currentBlock)) {
			startLoc.setY(startLoc.getY()-1);
			currentBlock = startLoc.getBlock().getType();
			if(!currentBlock.equals(Material.AIR) && !leaves.contains(currentBlock)) {
				return new Location(startLoc.getWorld(),startLoc.getX(),(startLoc.getY()+1),startLoc.getZ());
			}
		}
		return new Location(startLoc.getWorld(),startLoc.getX(),(startLoc.getY()+1),startLoc.getZ());
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
	
	public void savePreviousArea(Location loc, int counter) {
		
		World w = ConfigManager.getWorld();
		com.sk89q.worldedit.world.World weWorld = new BukkitWorld(w);
		
		Location startLoc = loc;
		
		Location s1Loc = new Location(w, (startLoc.getX() - 6), startLoc.getY(), (startLoc.getZ() + 5));
		Location s2Loc = new Location(w, (startLoc.getX() + 7), (startLoc.getY() + 6), (startLoc.getZ() - 8));
		
		CuboidRegion region = new CuboidRegion(weWorld, BlockVector3.at(s1Loc.getX(), s1Loc.getY(), s1Loc.getZ()), BlockVector3.at(s2Loc.getX(), s2Loc.getY(), s2Loc.getZ()));
		
		File restoresFolder = new File(this.getDataFolder(), "restores");
		File schemFile = new File(restoresFolder, "restore" + counter + ".schem");
		
		if(!restoresFolder.exists()) {
			makeRestoreFolder(restoresFolder);
		}
		
		if(!schemFile.exists()) {
			makeRestoreFile(schemFile);
		}
		
		try {
		
			BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

			EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(region.getWorld(), -1);
			
			ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
			forwardExtentCopy.setCopyingEntities(true);
			Operations.complete(forwardExtentCopy);
			
			try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(schemFile))) {
			    writer.write(clipboard);
			}
			
		}catch(IOException | WorldEditException e) {
			e.printStackTrace();
		}
		
	}
	
	public void makeRestoreFolder(File restoreFolder) {
		restoreFolder.mkdir();
	}
	
	public void makeRestoreFile(File schemFile) {
		try {
			schemFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void pasteShrineSchematic(World w, Location shrine) throws FileNotFoundException, IOException {
		
		File schemFile = new File(this.getDataFolder() + File.separator + "/Shrine.schem");	
		ClipboardFormat format = ClipboardFormats.findByFile(schemFile);
		ClipboardReader reader = format.getReader(new FileInputStream(schemFile));
		Clipboard clipboard = reader.read();
		
		com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(w);
		
		EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(adaptedWorld, 999999);
		
		Operation operation = new ClipboardHolder(clipboard).createPaste(editSession).to(BlockVector3.at(shrine.getX(), shrine.getY(), shrine.getZ())).ignoreAirBlocks(false).build();
		
		//https://bukkit.org/threads/best-way-to-roll-back-an-area.275169/
		try { // This simply completes our paste and then cleans up.
	        Operations.complete(operation);
	        editSession.flushSession();
	    } catch (WorldEditException e) {
	        Bukkit.broadcastMessage(ChatColor.RED + "OOPS! Something went wrong, please contact an administrator");
	        e.printStackTrace();
	    }
		
	}
	
	public void pasteRestoreSchematic(World w, Location shrine, int counter) throws FileNotFoundException, IOException {
		
		File restoresFolder = new File(this.getDataFolder(), "restores");
		File restoreFile = new File(restoresFolder, "restore" + counter + ".schem");
		
		ClipboardFormat format = ClipboardFormats.findByFile(restoreFile);
		ClipboardReader reader = format.getReader(new FileInputStream(restoreFile));
		Clipboard clipboard = reader.read();
		
		com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(w);
		
		EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(adaptedWorld, 999999);
		
		Operation operation = new ClipboardHolder(clipboard).createPaste(editSession).to(BlockVector3.at(shrine.getX(), shrine.getY()-1, shrine.getZ())).ignoreAirBlocks(false).build();
		
		//https://bukkit.org/threads/best-way-to-roll-back-an-area.275169/
		try { // This simply completes our paste and then cleans up.
	        Operations.complete(operation);
	        editSession.flushSession();
	    } catch (WorldEditException e) {
	        Bukkit.broadcastMessage(ChatColor.RED + "OOPS! Something went wrong, please contact an administrator");
	        e.printStackTrace();
	    }
		
	}
	
}
