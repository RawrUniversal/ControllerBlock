package me.operon.controllerblockwe;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

public class BlockDesc {
	public Location blockLoc;
	public BlockData blockData;

	public BlockDesc(Location l, BlockData b) {
		blockLoc = l;
		blockData = b;
	}
}