package me.operon.controllerblockwe;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class CPlayerListener implements Listener {
	ControllerBlock parent;

	public CPlayerListener(ControllerBlock parent) {
		this.parent = parent;
	}

	public boolean isRedstone(Block b) {
		Material t = b.getType();

		return (t.equals(Material.REDSTONE_WIRE)) || (t.equals(Material.REDSTONE_TORCH));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player player = (Player) e.getPlayer();
		Block block = e.getClickedBlock();
		if ((e.getItem() != null) && (player.getGameMode().equals(GameMode.SURVIVAL))) {
			if ((e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && (e.getItem().getType().equals(Material.STICK))
					&& parent.isControlBlock(block.getLocation())) {
				if (parent.movingCBlock.containsKey(player.getName())) {
					CBlock c = parent.movingCBlock.get(player.getName());
					if (c.getLoc().getBlock().getType().equals(block.getType())) {
						parent.moveHere.put(player.getName(), block.getLocation());
						if (parent.moveControllerBlock(c, block.getLocation()) != null) {
							player.sendMessage(parent.prefix + "ControllerBlock successfully moved");
						} else {
							player.sendMessage(parent.prefix + "The ControllerBlock needs to be turned off");
						}
					}
				} else {
					player.sendMessage(parent.prefix + "First Left-Click the ControllerBlock you want to move");
				}
			}
		}
	}
}