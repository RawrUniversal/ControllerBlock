package me.operon.controllerblockwe;

import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

public class CRedstoneListener implements Listener {
	private ControllerBlock parent;

	public CRedstoneListener(ControllerBlock controllerBlock) {
		parent = controllerBlock;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockRedstoneChange(BlockRedstoneEvent e) {
		CBlock conBlock = null;
		if (parent.getConfigu().getBool(Config.Option.QuickRedstoneCheck)) {
			conBlock = parent.getCBlock(e.getBlock()
					.getRelative(BlockFace.DOWN).getLocation());
		}
		if (conBlock == null) {
			return;
		}
		
		conBlock.doRedstoneCheck(e.getBlock());
	}
}