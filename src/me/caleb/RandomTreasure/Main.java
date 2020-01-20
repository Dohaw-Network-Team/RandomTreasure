package me.caleb.RandomTreasure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;

import io.netty.util.internal.ThreadLocalRandom;
import net.md_5.bungee.api.ChatColor;

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
		Location spawnLoc = w.getSpawnLocation();
		Location startLoc = new Location(w, getRandX(), 100, getRandZ());
		startLoc = adjustShrine(startLoc);
		shrines.add(startLoc);
		
		for(int x = 1; x <= 10; x++) {
			int index = shrines.size()-1;
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
				pastSchematic(w, newShrine);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
		
		//4 blocks back
		//5 blocks left'
		
		//Select 2
		//7 blocks forward
		//6 blocks right
		//6 blocks up
		
		WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		
		Location s1Loc = new Location(w, (startLoc.getX() - 5), startLoc.getY(), (startLoc.getZ() + 4));
		Location s2Loc = new Location(w, (startLoc.getX() + 6), (startLoc.getY() + 6), (startLoc.getZ() - 7));
		
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
			Extent source = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(w), 99999);
			Extent destination = source;
			ForwardExtentCopy copy = new ForwardExtentCopy(source, region, clipboard.getOrigin(), destination, BlockVector3.at(s1Loc.getX(), s1Loc.getY(), s1Loc.getZ()));
			copy.setSourceMask(new ExistingBlockMask(source));
			Operations.completeLegacy(copy);
			
			ClipboardFormat format = ClipboardFormats.findByFile(schemFile);
			
			try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(schemFile))) {
			    writer.write(clipboard);
			}
			
		}catch(IOException | MaxChangedBlocksException e) {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void pastSchematic(World w, Location shrine) throws FileNotFoundException, IOException {
		
		File schemFile = new File(this.getDataFolder() + File.separator + "/Shrine.schem");	
		ClipboardFormat format = ClipboardFormats.findByFile(schemFile);
		ClipboardReader reader = format.getReader(new FileInputStream(schemFile));
		Clipboard clipboard = reader.read();
		
		com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(w);
		
		EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(adaptedWorld, 999999);
		
		Operation operation = new ClipboardHolder(clipboard).createPaste(editSession).to(BlockVector3.at(shrine.getX(), shrine.getY(), shrine.getZ())).ignoreAirBlocks(true).build();
		
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
