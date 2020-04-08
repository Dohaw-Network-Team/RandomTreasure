package me.caleb.RandomTreasure;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.caleb.Clan.managers.ClanConfigManager;
import me.caleb.Clan.managers.ClanManager;

public class ShrineManager implements Listener{

	private Main plugin;
	private static me.caleb.Clan.Main ClanPlugin = me.caleb.Clan.Main.getPlugin();
	private static me.caleb.Clan.managers.ClanConfigManager ccm = new ClanConfigManager();
	private static ClanManager cm = new ClanManager(ClanPlugin);
	private List<Location> shrines = ConfigManager.getTreasureShrineLocs();
	private int shrineRadius = 20;
	private List<Location> shrineLocs = ConfigManager.getTreasureShrineLocs();
	
	final Material contentHolder = Material.BARREL;
	
	public ShrineManager(Main plugin) {
		this.plugin = plugin;
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public List<Player> getPlayersInYourClan(List<Entity> entities, Player firstPlayer){
		
		List<Player> players = new ArrayList<Player>();
		
		for(Entity e : entities) {
			if(e instanceof Player) {
				Player p = (Player) e;
				if(ccm.inSameClan(p.getName(), firstPlayer.getName())) {
					players.add(p);
				}
			}
		}
		return players;
	}
	
	//Notifies players that a shrine is being conquered
	public void notifyPlayers(Player p, String clanName) {
		for(Player pl : Bukkit.getOnlinePlayers()) {
			if(!ccm.inSameClan(p.getName(), pl.getName())) {
				Chat.sendPlayerMessage("&c&l" + clanName + " &rhas started conquering a shrine at &a&lX:" + p.getLocation().getX() + " Y: " + p.getLocation().getY() + " Z: " + p.getLocation().getZ(), true, pl, plugin.getConfig().getString("Prefix"));
				Chat.sendPlayerMessage("They will claim the shrine in &a&l" + plugin.getConfig().getInt("HoldTime") + " minutes!", true, pl, plugin.getConfig().getString("Prefix"));
			}
		}
	}
	
	public void addBossBar(List<String> players) {
		BossBar bar = Bukkit.getServer().createBossBar("Conquering Timer", BarColor.BLUE, BarStyle.SOLID, BarFlag.CREATE_FOG);
		
		for(int x = 0; x < players.size(); x++) {
			if(Bukkit.getOfflinePlayer(players.get(x)).isOnline()) {
				bar.addPlayer(Bukkit.getPlayer(players.get(x)));
				bar.setProgress(0.0);
			}else {
				players.remove(x);
			}
		}
		
		double lengthToConquer = plugin.getConfig().getDouble("HoldTime") * 60;
		double progressPerSecond = 1 / lengthToConquer;
		
		new BukkitRunnable() {
			int counter = 0;
			@Override
			public void run() {
	
				double prog = bar.getProgress();
				try {
					bar.setProgress(prog + progressPerSecond);
				}catch(IllegalArgumentException e) {
					
					bar.removeAll();
					this.cancel();
					
					for(String n : players) {
						Chat.sendPlayerMessage("You have conquered this shrine! Collect your treasures!", true, Bukkit.getPlayer(n), plugin.getConfig().getString("Prefix"));			
					}
					
					return;
				}
				
				counter++;
				
				if(lengthToConquer - counter == 120) {
					for(String n : players) {
						if(Bukkit.getPlayer(n).isOnline()) {
							Chat.sendPlayerMessage("You have 2 more minutes to go!", true, Bukkit.getPlayer(n), plugin.getConfig().getString("Prefix"));
						}
					}
				}else if(lengthToConquer - counter == 60) {
					for(String n : players) {
						if(Bukkit.getPlayer(n).isOnline()) {
							Chat.sendPlayerMessage("You have 1 more minute to go!", true, Bukkit.getPlayer(n), plugin.getConfig().getString("Prefix"));
						}
					}
				}else if(lengthToConquer - counter == 30) {
					for(String n : players) {
						if(Bukkit.getPlayer(n).isOnline()) {
							Chat.sendPlayerMessage("You have 30 more seconds to go!", true, Bukkit.getPlayer(n), plugin.getConfig().getString("Prefix"));
						}
					}
				}else if(lengthToConquer - counter == 10) {
					for(String n : players) {
						Chat.sendPlayerMessage("You have 10 more seconds to go! Hold on!", true, Bukkit.getPlayer(n), plugin.getConfig().getString("Prefix"));
					}
				}
			}
			
		}.runTaskTimer(plugin, 0L, 20L);
		
	}
	
	public Location getNearestShrine(Location loc) {
		
		Location nearestShrine = shrineLocs.get(0);
		
		for(Location l : shrineLocs) {
			if(loc.distance(nearestShrine) > l.distance(loc)) {
				nearestShrine = l;
			}
		}
		
		return nearestShrine;
	}
	
	//Is the clicked block on the shrine
	public boolean isOnShrine(Location loc) {
		for(Location l : shrineLocs) {
			if(getNearestShrine(loc).distance(loc) < 5) {
				return true;
			}
		}
		return false;
	}
	
	@EventHandler
	public void onBarrelRightClick(PlayerInteractEvent e) {

		Action a = e.getAction();
		Material itemRightClicked = e.getClickedBlock().getType();
		Player firstPlayer = e.getPlayer();
		
		if(a.equals(Action.RIGHT_CLICK_BLOCK) && itemRightClicked.equals(contentHolder) && isOnShrine(e.getClickedBlock().getLocation())) {
			int numShrine = shrineLocs.indexOf(getNearestShrine(e.getClickedBlock().getLocation())) + 1;
			e.setCancelled(true);
			
			if(ConfigManager.isBeingConquered(numShrine) && ConfigManager.getConquerer(numShrine).equalsIgnoreCase("none")) {
				Chat.sendPlayerMessage("This shrine is already being conquered!", true, firstPlayer, plugin.getConfig().getString("Prefix"));
				return;
			}else if(!ConfigManager.isBeingConquered(numShrine) && !ConfigManager.getConquerer(numShrine).equalsIgnoreCase("none")) {
				Chat.sendPlayerMessage("This shrine has already been conquered!", true, firstPlayer, plugin.getConfig().getString("Prefix"));
				return;
			}
			
			List<String> playersInClan = new ArrayList<String>();
			String clanName;
			
			if(ccm.isInClan(firstPlayer.getName())) {
				clanName = cm.getPlayerClan(firstPlayer.getName());
				playersInClan = ccm.getMembers(clanName);
				for(String name : playersInClan) {
					if(Bukkit.getOfflinePlayer(name).isOnline()) {
						Chat.sendPlayerMessage("Your clan has started conquering a shrine at &a&lX: " + e.getClickedBlock().getLocation().getX() + " Y: " + e.getClickedBlock().getLocation().getY() + " Z: " + e.getClickedBlock().getLocation().getZ(), true, Bukkit.getPlayer(name), plugin.getConfig().getString("Prefix"));
						Chat.sendPlayerMessage("You must hold the shrine down for &a&l" + plugin.getConfig().getDouble("HoldTime") + " minutes &rto access the resources in the shrine!", true, Bukkit.getPlayer(name), plugin.getConfig().getString("Prefix"));
					}else {
						continue;
					}
				}
				addBossBar(playersInClan);
				
				ConfigManager.setBeingConquered(numShrine);
				
			}else {
				Chat.sendPlayerMessage("You have started conquering this shrine!", true,firstPlayer, plugin.getConfig().getString("Prefix"));
				Chat.sendPlayerMessage("You must hold the shrine down for &a&l" + plugin.getConfig().getInt("HoldTime") + " minutes &rto access the resources in the shrine!", true, firstPlayer, plugin.getConfig().getString("Prefix"));
				playersInClan.add(firstPlayer.getName());
				addBossBar(playersInClan);
				
				ConfigManager.setBeingConquered(numShrine);
			}
			
			
			notifyPlayers(firstPlayer, cm.getPlayerClan(firstPlayer.getName()));
			
			
		}
		
	}
	
	
}
