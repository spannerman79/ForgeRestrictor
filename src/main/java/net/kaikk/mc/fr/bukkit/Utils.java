package net.kaikk.mc.fr.bukkit;

import org.bukkit.Location;

public class Utils {
	public static String locationToString(Location location) {
		return "[" + location.getWorld().getName() + ", " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "]";
	}
}
