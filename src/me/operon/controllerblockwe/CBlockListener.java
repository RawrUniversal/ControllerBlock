package me.operon.controllerblockwe;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.PlayerInventory;

public class CBlockListener implements Runnable, Listener {
	private ControllerBlock parent;

	public CBlockListener(ControllerBlock controllerBlock) {
		parent = controllerBlock;
	}

	public Player getPlayerEditing(CBlock c) {
		for (Map.Entry<Player, CBlock> e : parent.map.entrySet()) {
			if (((CBlock) e.getValue()).equals(c)) {
				return (Player) e.getKey();
			}
		}
		return null;
	}

	public void removePlayersEditing(CBlock c) {
		Player p;
		while ((p = getPlayerEditing(c)) != null) {
			parent.map.remove(p);
		}
	}

	public boolean isRedstone(Block b) {
		Material t = b.getType();

		return (t.equals(Material.REDSTONE_WIRE)) || (t.equals(Material.REDSTONE_TORCH))
				|| (t.equals(Material.REDSTONE_WALL_TORCH));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent e) {
		if (e.isCancelled()) {
			return;
		}
		Player player = (Player) e.getPlayer();
		Block b = e.getBlock();
		PlayerInventory inv = player.getInventory();
		Material item = inv.getItemInMainHand().getType();

		if ((player.getGameMode().equals(GameMode.CREATIVE)) && (item.isBlock())) {
			CBlock conBlock = parent.map.get(player);

			if (item.equals(Material.NETHERITE_PICKAXE) || item.equals(Material.WOODEN_PICKAXE) || item.equals(Material.STONE_PICKAXE)
					|| item.equals(Material.IRON_PICKAXE) || item.equals(Material.GOLDEN_PICKAXE)
					|| item.equals(Material.DIAMOND_PICKAXE)) {
				return;
			}
			if (conBlock != null) {
				if (parent.isControlBlock(b.getLocation())) {
					conBlock.editBlock(false);
					parent.map.remove(player);

					if (Util.locEquals(conBlock.getLoc(), b.getLocation())) {
						player.sendMessage(parent.prefix + "Finished editing ControllerBlock");
						e.setCancelled(true);
						return;
					}

					player.sendMessage(parent.prefix + "Finished editing previous ControllerBlock");
					e.setCancelled(true);
					conBlock = null;
				}

			}

			if (conBlock == null) {
				conBlock = parent.getCBlock(b.getLocation());
				if (conBlock == null) {
					if (!isRedstone(b.getRelative(BlockFace.UP))) {
						return;
					}
					byte cBType;
					String cBTypeStr = null;
					if (b.getType() == parent.getCBlockType()) {
						cBTypeStr = "protected";
						cBType = 0;
					} else {
						if (b.getType() == parent.getSemiProtectedCBlockType()) {
							cBTypeStr = "semi-protected";
							cBType = 1;
						} else {
							if (b.getType() == parent.getUnProtectedCBlockType()) {
								cBTypeStr = "unprotected";
								cBType = 2;
							} else {
								return;
							}
						}
					}
					if (!parent.getPerm().canCreate(player)) {
						player.sendMessage(parent.prefix + "You're not allowed to create " + cBTypeStr + " ControllerBlocks");
						e.setCancelled(true);
						return;
					}
					if (parent.isControlledBlock(b.getLocation())) {
						player.sendMessage(parent.prefix + "This block is controlled, controlled blocks can't be controllers");
						e.setCancelled(true);
						return;
					}
					conBlock = parent.createCBlock(b.getLocation(), player.getName(), cBType);
					player.sendMessage(parent.prefix + "Created " + cBTypeStr + " controller block");
					e.setCancelled(true);
				}

				if (conBlock == null) {
					return;
				}
				if (!parent.getPerm().canModify(player, conBlock)) {
					player.sendMessage(parent.prefix + "You're not allowed to modify this ControllerBlock");
					e.setCancelled(true);
					return;
				}

				if (item.equals(Material.STICK)) {
					parent.movingCBlock.put(player.getName(), conBlock);
					player.sendMessage(parent.prefix + 
							"ControllerBlock is registered as the next to move.   Right-Click the position where to move it.");
					e.setCancelled(true);
					return;
				}

				if (conBlock.numBlocks() == 0) {
					if (!parent.isValidMaterial(item)) {
						player.sendMessage(parent.prefix + "Can't set the ControllerBlock type to " + item);
						e.setCancelled(true);
						return;
					}

					if (((conBlock.protectedLevel == 1) || (conBlock.protectedLevel == 2))
							&& (!parent.isUnprotectedMaterial(item))) {
						player.sendMessage(parent.prefix + 
								"The Material is protected, can't use with (semi-)unprotected ControllerBlocks.");
						e.setCancelled(true);
						return;
					}
					conBlock.setType(item);
				}

				if ((item != Material.AIR) && (item != conBlock.getType())) {
					player.sendMessage(parent.prefix + "This ControllerBlock needs to be edited with " + conBlock.getType());
					e.setCancelled(true);
					return;
				}

				parent.map.put(player, conBlock);
				conBlock.editBlock(true);
				player.sendMessage(parent.prefix + "You're now editing this block with " + conBlock.getType() + " "
						+ Util.formatBlockCount(conBlock));
				e.setCancelled(true);
				return;
			}
		}
		CBlock conBlock = parent.getCBlock(b.getLocation());
		if (conBlock != null) {
			if (!parent.getPerm().canDestroy(player, conBlock)) {
				player.sendMessage(parent.prefix + "You're not allowed to destroy this ControllerBlock");
				e.setCancelled(true);
				return;
			}
			conBlock = parent.destroyCBlock(b.getLocation());
			if (conBlock != null) {
				player.sendMessage(parent.prefix + "Destroyed controller block");
				removePlayersEditing(conBlock);
			}
		}

		conBlock = parent.map.get(player);
		if ((conBlock != null) && (conBlock.hasBlock(b.getLocation())) && (conBlock.getType().equals(b.getType()))) {
			if (conBlock.delBlock(b)) {
				player.sendMessage(parent.prefix + "Block removed from controller " + Util.formatBlockCount(conBlock));
			}
		} else if ((conBlock = parent.getControllerBlockFor(null, b.getLocation(), b.getBlockData(), null)) != null) {
			switch (((BlockProtectMode) parent.getConfigu().getOpt(Config.Option.BlockProtectMode))) {
			case protect:
				if ((conBlock.protectedLevel != 0) && ((conBlock.isOn()) || (conBlock.protectedLevel == 2))) {
					break;
				}
				player.sendMessage(parent.prefix + "This block is controlled by a controller block at " + conBlock.getLoc().getBlockX()
						+ ", " + conBlock.getLoc().getBlockY() + ", " + conBlock.getLoc().getBlockZ());
				e.setCancelled(true);
				break;
			case remove:
				conBlock.delBlock(b);
			case none:
				break;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockDamage(PlayerInteractEvent e) {
		Player player = (Player) e.getPlayer();
		if (e.getItem() == null)
			return;
		if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getItem().getType().isBlock()) {
			if (e.getClickedBlock().getType().equals(Material.AIR)) {
				CBlock conBlock;
				if ((conBlock = parent.destroyCBlock(e.getClickedBlock().getLocation())) != null) {
					player.sendMessage(parent.prefix + "Destroyed controller block with superpickaxe?");
					removePlayersEditing(conBlock);
				}
			}
			if (player.getGameMode().equals(GameMode.CREATIVE)) {
				return;
			}
			Material item = e.getItem().getType();
			Block b = e.getClickedBlock();
			CBlock conBlock = parent.map.get(player);

			if (item.equals(Material.NETHERITE_PICKAXE) || item.equals(Material.WOODEN_PICKAXE) || item.equals(Material.STONE_PICKAXE)
					|| item.equals(Material.IRON_PICKAXE) || item.equals(Material.GOLDEN_PICKAXE)
					|| item.equals(Material.DIAMOND_PICKAXE)) {
				return;
			}
			if (conBlock != null) {
				if (parent.isControlBlock(b.getLocation())) {
					conBlock.editBlock(false);
					parent.map.remove(player);

					if (Util.locEquals(conBlock.getLoc(), b.getLocation())) {
						player.sendMessage(parent.prefix + "Finished editing ControllerBlock");
						return;
					}
					player.sendMessage(parent.prefix + "Finished editing previous ControllerBlock");
					conBlock = null;
				}

			}

			if (conBlock == null) {
				conBlock = parent.getCBlock(b.getLocation());
				if (conBlock == null) {
					if (!isRedstone(b.getRelative(BlockFace.UP))) {
						return;
					}
					byte cBType;
					String cBTypeStr = null;
					if (b.getType() == parent.getCBlockType()) {
						cBTypeStr = "protected";
						cBType = 0;
					} else {
						if (b.getType() == parent.getSemiProtectedCBlockType()) {
							cBTypeStr = "semi-protected";
							cBType = 1;
						} else {
							if (b.getType() == parent.getUnProtectedCBlockType()) {
								cBTypeStr = "unprotected";
								cBType = 2;
							} else {
								return;
							}
						}
					}
					if (!parent.getPerm().canCreate(player)) {
						player.sendMessage(parent.prefix + "You're not allowed to create " + cBTypeStr + " ControllerBlocks");
						return;
					}
					if (parent.isControlledBlock(b.getLocation())) {
						player.sendMessage(parent.prefix + "This block is controlled, controlled blocks can't be controllers");
						return;
					}
					conBlock = parent.createCBlock(b.getLocation(), player.getName(), cBType);
					player.sendMessage(parent.prefix + "Created " + cBTypeStr + " controller block");
				}

				if (conBlock == null) {
					return;
				}
				if (!parent.getPerm().canModify(player, conBlock)) {
					player.sendMessage(parent.prefix + "You're not allowed to modify this ControllerBlock");
					return;
				}

				if (item.equals(Material.STICK)) {
					parent.movingCBlock.put(player.getName(), conBlock);
					player.sendMessage(parent.prefix + 
							"ControllerBlock is registered as the next to move.   Right-Click the position where to move it.");
					return;
				}

				if (conBlock.numBlocks() == 0) {
					if (!parent.isValidMaterial(item)) {
						player.sendMessage(parent.prefix + "Can't set the ControllerBlock type to " + item);
						return;
					}

					if (((conBlock.protectedLevel == 1) || (conBlock.protectedLevel == 2))
							&& (!parent.isUnprotectedMaterial(item))) {
						player.sendMessage(parent.prefix + 
								"The Material is protected, can't use with (semi-)unprotected ControllerBlocks.");
						return;
					}
					conBlock.setType(item);
				}

				if ((item != Material.AIR) && (item != conBlock.getType())) {
					player.sendMessage(parent.prefix + "This ControllerBlock needs to be edited with " + conBlock.getType());
					return;
				}

				parent.map.put(player, conBlock);
				conBlock.editBlock(true);
				player.sendMessage(parent.prefix + "You're now editing this block with " + conBlock.getType() + " "
						+ Util.formatBlockCount(conBlock));
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent e) {
		Player player = (Player) e.getPlayer();
		CBlock conBlock = parent.map.get(player);
		if (conBlock == null) {
			return;
		}

		if ((parent.getConfigu().getInt(Config.Option.MaxBlocksPerController).intValue() != 0)
				&& (conBlock.numBlocks() >= parent.getConfigu().getInt(Config.Option.MaxBlocksPerController).intValue())
				&& (!parent.getPerm().isAdminPlayer(player))) {
			player.sendMessage(parent.prefix + "Controller block is full " + Util.formatBlockCount(conBlock));
			return;
		}

		if ((parent.getConfigu().getInt(Config.Option.MaxDistanceFromController).intValue() != 0)
				&& (conBlock.getType().equals(e.getBlock().getType())) && (!parent.getPerm().isAdminPlayer(player))
				&& (Util.getDistanceBetweenLocations(conBlock.getLoc(), e.getBlock().getLocation()) > parent
						.getConfigu().getInt(Config.Option.MaxDistanceFromController).intValue())) {
			player.sendMessage(parent.prefix + "This block is too far away from the controller block to be controlled");
			return;
		}

		if (conBlock.addBlock(e.getBlock())) {
			player.sendMessage(parent.prefix + "Added block to controller " + Util.formatBlockCount(conBlock));
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent e) {
		CBlock conBlock = parent.getControllerBlockFor(null, e.getBlock().getLocation(), null, Boolean.valueOf(true));
		if (conBlock == null) {
			return;
		}
		if (conBlock.isBeingEdited()) {
			if (!parent.blockPhysicsEditCheck) {
				return;
			}

			Player player = (Player) getPlayerEditing(conBlock);

			if (!Util.typeEquals(conBlock.getType(), e.getChangedType())) {
				parent.log.debug("Block at " + Util.formatLocation(e.getBlock().getLocation()) + " was changed to "
						+ e.getChangedType() + " but is supposed to be " + conBlock.getType() + ", dupe!");
				conBlock.delBlock(e.getBlock());
				player.sendMessage(parent.prefix + 
						"Removing block due to changed type while editing " + Util.formatBlockCount(conBlock));
			}
		} else {
			BlockProtectMode protect = (BlockProtectMode) parent.getConfigu()
					.getOpt(Config.Option.BlockPhysicsProtectMode);
			if (protect.equals(BlockProtectMode.protect)) {
				e.setCancelled(true);
			} else if (protect.equals(BlockProtectMode.remove)) {
				conBlock.delBlock(e.getBlock());
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockFromTo(BlockFromToEvent e) {
		CBlock conBlock = parent.getControllerBlockFor(null, e.getToBlock().getLocation(), null, Boolean.valueOf(true));
		if (conBlock == null) {
			return;
		}
		if (conBlock.isBeingEdited()) {
			if (!parent.blockPhysicsEditCheck) {
				return;
			}
			Player player = (Player) getPlayerEditing(conBlock);
			parent.log.debug("Block at " + Util.formatLocation(e.getToBlock().getLocation())
					+ " was drowned while editing and removed from a controller");
			conBlock.delBlock(e.getToBlock());
			player.sendMessage(parent.prefix + "Removing block due to change while editing " + Util.formatBlockCount(conBlock));
		} else {
			BlockProtectMode protect = (BlockProtectMode) parent.getConfigu()
					.getOpt(Config.Option.BlockFlowProtectMode);
			if (protect.equals(BlockProtectMode.protect)) {
				e.setCancelled(true);
			} else if (protect.equals(BlockProtectMode.remove)) {
				conBlock.delBlock(e.getBlock());
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		if (((Boolean) parent.getConfigu().getOpt(Config.Option.PistonProtection)).booleanValue()) {
			Block b = event.getBlock();
			CBlock conBlock = parent.getCBlock(b.getLocation());
			if (conBlock != null) {
				event.setCancelled(true);
				return;
			}
			List<?> pblocks = event.getBlocks();
			for (int i = 0; i < pblocks.size(); i++) {
				Block block = (Block) pblocks.get(i);
				if ((!parent.isControlledBlock(block.getLocation(), block.getBlockData()))
						|| (parent.isUnprotectedMaterial(block.getType()))) {
					continue;
				}
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
		if (((Boolean) parent.getConfigu().getOpt(Config.Option.PistonProtection)).booleanValue()) {
			Block b = event.getBlock();
			CBlock conBlock = parent.getCBlock(b.getLocation());
			if (conBlock != null) {
				event.setCancelled(true);
				return;
			}
			if (event.isSticky()) {
				for (Block block : event.getBlocks()) {
					if ((parent.isControlledBlock(block.getLocation(), block.getBlockData()))
							&& (!parent.isUnprotectedMaterial(block.getType()))) {
						event.setCancelled(true);
						return;
					}
				}
			}
		}
	}

	public void run() {
		if (!parent.getConfigu().getBool(Config.Option.DisableEditDupeProtection)) {
			for (@SuppressWarnings("rawtypes")
			Map.Entry e : parent.map.entrySet()) {
				@SuppressWarnings("rawtypes")
				Iterator i = ((CBlock) e.getValue()).getBlocks();
				while (i.hasNext()) {
					Block b = Util.getBlockAtLocation(((BlockDesc) i.next()).blockLoc);
					if (!Util.typeEquals(b.getType(), ((CBlock) e.getValue()).getType())) {
						parent.log.debug("Block at " + Util.formatLocation(b.getLocation()) + " was " + b.getType()
								+ " but expected " + ((CBlock) e.getValue()).getType() + ", dupe!");
						i.remove();
						((Player) e.getKey()).sendMessage(parent.prefix + "Removing block due to changed while editing "
								+ Util.formatBlockCount((CBlock) e.getValue()));
						return;
					}
				}
			}
		}
		for (@SuppressWarnings("rawtypes")
		Map.Entry e : parent.map.entrySet()) {
			@SuppressWarnings("rawtypes")
			Iterator i = ((CBlock) e.getValue()).getBlocks();
			while (i.hasNext()) {
				BlockDesc d = (BlockDesc) i.next();
				Block b = Util.getBlockAtLocation(d.blockLoc);
				d.blockData = b.getBlockData();
			}
		}
	}
}