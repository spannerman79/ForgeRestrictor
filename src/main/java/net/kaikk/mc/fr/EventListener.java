package net.kaikk.mc.fr;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;

class EventListener implements Listener {
	private ForgeRestrictor instance;
	static ArrayList<ConfiscatedInventory> confiscatedInventories;
	
	EventListener(ForgeRestrictor instance) {
		this.instance = instance;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
		// ignore stepping onto or into a block 
		if (event.getAction()==Action.PHYSICAL) {
			return;
		}
		
		final Player player = event.getPlayer();

		// ignore empty hand
		if (player.getItemInHand().getType()==Material.AIR) {
			return;
		}
		
		Block block=event.getClickedBlock();
		
		// ignore edible items use
		if (player.getItemInHand().getData().getItemType().isEdible()) {
			return;
		}
		
		// whitelisted items in hand
		ItemStack itemInHand=player.getItemInHand();
		if (this.instance.config.matchWhitelistItem(itemInHand.getType(), itemInHand.getData().getData(), player.getWorld().getName()) !=null) {
			return;
		}
		
		// special aoe items list (needs to check a wide area...)
		ListedRangedItem item = this.instance.config.matchAoEItem(itemInHand.getType(), itemInHand.getData().getData(), player.getWorld().getName());
		if (item!=null) {
			// check players location
			for (ProtectionHandler protection : ProtectionPlugins.getHandlers()) {
				if (!protection.canUseAoE(player, player.getLocation(), item.range)) {
					event.setUseInteractedBlock(Result.DENY);
					event.setUseItemInHand(Result.DENY);
					event.setCancelled(true);
					this.confiscateInventory(player);
					return;
				}
			}
			return;
		}
		
		if (block==null) {
			// check if the item in hand is a ranged item
			item = this.instance.config.matchRangedItem(itemInHand.getType(), itemInHand.getData().getData(), player.getWorld().getName());
			if (item!=null) {
				block=getTargetBlock(player, item.range);
			}
			
		}
		
		Location targetLocation;
		if (block==null) {
			targetLocation=player.getLocation();
		} else {
			targetLocation=block.getLocation();
		}

		// check permissions on that location
		for (ProtectionHandler protection : ProtectionPlugins.getHandlers()) {
			if (!protection.canInteract(player, targetLocation)) {
				event.setUseInteractedBlock(Result.DENY);
				event.setUseItemInHand(Result.DENY);
				event.setCancelled(true);
				this.confiscateInventory(player);
				return;
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getDamager().getType()==EntityType.PLAYER) {
			final Player damager = (Player) event.getDamager();
			
			if (damager.getName().startsWith("[")) {
				return;
			}
			
			final Entity damaged = event.getEntity();
			
			for (ProtectionHandler protection : ProtectionPlugins.getHandlers()) {
				if (!protection.canAttack(damager, damaged)) {
					event.setCancelled(true);
					this.confiscateInventory(damager);
					return;
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onBlockPlace(BlockPlaceEvent event) {
		final Player player = event.getPlayer();
		if (player.getName().startsWith("[")) {
			return;
		}
		
		ItemStack itemInHand=player.getItemInHand();
		
		// special aoe items list (needs to check a wide area...)
		ListedRangedItem item = this.instance.config.getAoEItem(itemInHand.getType(), itemInHand.getData().getData(), player.getWorld().getName());
		if (item!=null) {
			Location blockLocation = event.getBlock().getLocation();
			for (ProtectionHandler protection : ProtectionPlugins.getHandlers()) {
				if (!protection.canUseAoE(player, blockLocation, item.range)) {
					event.setBuild(false);
					event.setCancelled(true);
					this.confiscateInventory(player);
					return;
				}
			}
		}
	}
	
	// blocks projectiles explosions
	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void onExplosionPrime(ExplosionPrimeEvent event) {
		final Entity entity = event.getEntity();
		
		if (entity instanceof Projectile) {
			final Projectile projectile = (Projectile) entity;
			if (projectile.getShooter() instanceof Player) {
				if (this.projectileCheck(projectile, projectile.getLocation())) {
					event.setCancelled(true);
					event.setRadius(0);
				}
			}
		}
	}
	
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		final Projectile projectile = event.getEntity();
		if (projectile.getShooter() instanceof Player) {
			final Player player = (Player) projectile.getShooter();
			Block targetBlock = getTargetBlock(player, 100); // TODO max distance to config
			
			if (targetBlock==null) { 
				event.setCancelled(true);
				projectile.remove(); // In order to prevent targeting any far away protected area, remove the projectile. (TODO use a items list for this feature?)
			} else {
				if (this.projectileCheck(projectile, targetBlock.getLocation())) { // Check if the target block can be hit by this player
					event.setCancelled(true);
					this.confiscateInventory(player);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onProjectileHit(ProjectileHitEvent event) {
		final Projectile projectile = event.getEntity();
		
		if (projectile.getShooter() instanceof Player) {
			this.projectileCheck(projectile, projectile.getLocation());
		}
	}
	
	private boolean projectileCheck(Projectile projectile, Location location) {
		final Player player = (Player) projectile.getShooter();
		for (ProtectionHandler protection : ProtectionPlugins.getHandlers()) {
			if (!protection.canProjectileHit(player, projectile.getLocation())) {
				projectile.remove();
				return true;
			}
		}
		return false;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPluginEnable(PluginEnableEvent event) {
		this.pluginEnable(event.getPlugin().getName());
	}
	
	void pluginEnable(String pluginName) {
		ProtectionPlugins protectionPlugin;
		try {
			protectionPlugin = ProtectionPlugins.valueOf(pluginName);
		} catch (Exception e1) {
			return;
		}
		
		if (protectionPlugin.isEnabled()) {
			try {
				this.instance.getLogger().info("Loading protection plugin: "+pluginName);
				protectionPlugin.createHandler();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPluginDisable(PluginDisableEvent event) {
		ProtectionPlugins protectionPlugin;
		try {
			protectionPlugin = ProtectionPlugins.valueOf(event.getPlugin().getName());
		} catch (Exception e) {
			return;
		}

		this.instance.getLogger().info("Unloading protection plugin: "+event.getPlugin().getName());
		protectionPlugin.removeHandler();
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (confiscatedInventories!=null) {
			for (ConfiscatedInventory cis : confiscatedInventories) {
				if (cis.getPlayer()==event.getPlayer()) {
					cis.release();
					break;
				}
            }
		}
	}
	
	void confiscateInventory(Player player) {
		if (player.getName().startsWith("[") || !player.isOnline() || isInventoryEmpty(player)) {
			return;
		}
		
		if (confiscatedInventories==null) {
			confiscatedInventories=new ArrayList<ConfiscatedInventory>();
			
			new BukkitRunnable() {
	            @Override
	            public void run() {
	                for (ConfiscatedInventory cis : EventListener.confiscatedInventories) {
	                	cis.release();
	                }
	                
	                EventListener.confiscatedInventories=null;
	            }
	        }.runTaskLater(this.instance, 3);
		} else {
			// check if this player has his inventory already confiscated
			for (ConfiscatedInventory ci : confiscatedInventories) {
				if (player==ci.getPlayer()){
					return;
				}
			}
		}
		this.instance.getLogger().info("Confiscated "+player.getName()+" items for 3 ticks");
		confiscatedInventories.add(new ConfiscatedInventory(player));
	}

	private static boolean isInventoryEmpty(Player player) {
		for(ItemStack is : player.getInventory()) {
			if (is!=null && is.getType()!=Material.AIR) {
				return false;
			}
		}
		return true;
	}

	public static Block getTargetBlock(Player player, int maxDistance) {
		Block result = player.getLocation().getBlock().getRelative(BlockFace.UP);
		try {
			BlockIterator iterator = new BlockIterator(player.getLocation(), player.getEyeHeight(), maxDistance);
			
			while (iterator.hasNext()) {
				result = iterator.next();
				if (result.getType() != Material.AIR && result.getType() != Material.STATIONARY_WATER) {
					return result;
				}
			}
		} catch (Exception e) {  }
		return result;
	}
}
