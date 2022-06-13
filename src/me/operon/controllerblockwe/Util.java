package me.operon.controllerblockwe;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class Util {
	public static double getDistanceBetweenLocations(Location l1, Location l2) {
		if (!l1.getWorld().equals(l2.getWorld())) {
			return -1.0D;
		}
		return Math.sqrt(Math.pow(l1.getX() - l2.getX(), 2.0D)
				+ Math.pow(l1.getY() - l2.getY(), 2.0D)
				+ Math.pow(l1.getZ() - l2.getZ(), 2.0D));
	}

	public static Block getBlockAtLocation(Location l) {
		return getBlockAtLocation(l, Integer.valueOf(0), Integer.valueOf(0),
				Integer.valueOf(0));
	}

	public static String getSaveFileName(Location l) {
		String x = String.valueOf(l.getBlockX());
		String y = String.valueOf(l.getBlockY());
		String z = String.valueOf(l.getBlockZ());
		return x + "," + y + "," + z;
	}

	public static Block getBlockAtLocation(Location l, Integer x, Integer y,
			Integer z) {
		return l.getWorld().getBlockAt(l.getBlockX() + x.intValue(),
				l.getBlockY() + y.intValue(), l.getBlockZ() + z.intValue());
	}

	public static String formatBlockCount(CBlock c) {
		if (c.getParent().getConfigu()
				.getInt(Config.Option.MaxBlocksPerController).intValue() > 0) {
			return "("
					+ c.numBlocks()
					+ "/"
					+ c.getParent().getConfigu()
							.getInt(Config.Option.MaxBlocksPerController)
					+ " blocks)";
		}
		return "(" + c.numBlocks() + " blocks)";
	}

	public static String formatLocation(Location l) {
		return "<" + l.getWorld().getName() + "," + l.getBlockX() + ","
				+ l.getBlockY() + "," + l.getBlockZ() + ">";
	}

	public static boolean typeEquals(Material t1, Material t2) {
		if ((t1.equals(Material.DIRT))
				|| ((t1.equals(Material.GRASS)) && (t2.equals(Material.DIRT)))
				|| (t2.equals(Material.GRASS))) {
			return true;
		}

		if ((t1.equals(Material.REDSTONE_TORCH))
				|| ((t1.equals(Material.REDSTONE_WALL_TORCH)) && (t2
						.equals(Material.REDSTONE_TORCH)))
				|| (t2.equals(Material.REDSTONE_WALL_TORCH))) {
			return true;
		}
		return t1.equals(t2);
	}

	public static boolean locEquals(Location l1, Location l2) {
		return (l1.getWorld().getName() == l2.getWorld().getName())
				&& (l1.getBlockX() == l2.getBlockX())
				&& (l1.getBlockY() == l2.getBlockY())
				&& (l1.getBlockZ() == l2.getBlockZ());
	}
}