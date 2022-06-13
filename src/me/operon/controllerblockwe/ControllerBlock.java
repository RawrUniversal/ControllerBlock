package me.operon.controllerblockwe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;

public class ControllerBlock extends JavaPlugin implements Runnable {
	private static String configFile = "ControllerBlock.ini";
	public Logger log = new Logger(this, "Minecraft");
	private Config config = new Config();
	private PermissionHandler permissionHandler = new PermissionHandler(this);
	private final CBlockListener blockListener = new CBlockListener(this);
	private final CRedstoneListener redstoneListener = new CRedstoneListener(this);
	private final CPlayerListener playerListener = new CPlayerListener(this);
	private final CBlockRedstoneCheck checkRunner = new CBlockRedstoneCheck(this);

	public boolean blockPhysicsEditCheck = false;
	private boolean beenLoaded = false;
	private boolean beenEnabled = false;

	public String prefix = ChatColor.translateAlternateColorCodes('&', "&b&l[&3&lControllerBlockWE&b&l]&r ");
	
	public HashMap<Player, CBlock> map = new HashMap<Player, CBlock>();

	public List<CBlock> blocks = new ArrayList<CBlock>();

	HashMap<String, CBlock> movingCBlock = new HashMap<String, CBlock>();
	HashMap<String, Location> moveHere = new HashMap<String, Location>();
	private Material CBlockType;
	private Material semiProtectedCBlockType;
	private Material unProtectedCBlockType;
	private List<Material> DisallowedTypesAll = new ArrayList<Material>();
	private List<Material> UnprotectedBlocks = new ArrayList<Material>();

	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
	}

	public void onLoad() {
		if (!beenLoaded) {
			log.info(getDescription().getVersion()
					+ " by Zero9195 (Original by Hell_Fire). Updated for R6 by Sorklin, Edited for WorldEdit by Techzune, Updated to 1.13 by Me_Goes_RAWR");
			checkPluginDataDir();
			loadConfig();
			beenLoaded = true;
		}

	}

	public void onEnable() {
		if (!beenEnabled) {
			getServer().getPluginManager().registerEvents(blockListener, this);
			getServer().getPluginManager().registerEvents(playerListener, this);
			if (getServer().getScheduler().scheduleSyncRepeatingTask(this, blockListener, 1L, 1L) == -1) {
				log.warning("Scheduling BlockListener anti-dupe check failed, falling back to old BLOCK_PHYSICS event");
				blockPhysicsEditCheck = true;
			}
			if (config.getBool(Config.Option.DisableEditDupeProtection)) {
				log.warning("Edit dupe protection has been disabled, you're on your own from here");
			}
			if (!config.getBool(Config.Option.QuickRedstoneCheck)) {
				if (getServer().getScheduler().scheduleSyncRepeatingTask(this, checkRunner, 1L, 1L) == -1) {
					log.warning(
							"Scheduling CBlockRedstoneCheck task failed, falling back to quick REDSTONE_CHANGE event");
					config.setOpt(Config.Option.QuickRedstoneCheck, Boolean.valueOf(true));
				}
			}
			if (config.getBool(Config.Option.QuickRedstoneCheck)) {
				getServer().getPluginManager().registerEvents(redstoneListener, this);
			}
			if (getServer().getScheduler().scheduleSyncDelayedTask(this, this, 1L) == -1) {
				log.severe("Failed to schedule loadData, loading now, will probably not work with multiworld plugins");
				loadData();
			}
			log.info("Events registered");
			beenEnabled = true;
		}
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if ((sender instanceof Player)) {
			Player player = (Player) sender;
			if (player.hasPermission("cblock.commands")) {
				if (label.equals("cblock")) {
					if (args[0].equals("reload")) {
						if (player.isOp()) {
							loadConfig();
							log.info("Config reloaded");
							player.sendMessage(prefix + "Config reloaded");
						} else {
							player.sendMessage(ChatColor.DARK_RED + "You need to be OP for this command!");
						}
					} else if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("we")
							|| args[0].equalsIgnoreCase("a")) {
						Region reg = null;
						try {
							reg = getwe().getSessionManager().findByName(player.getName())
									.getSelection(new BukkitWorld(player.getWorld()));
						} catch (Exception e) {
							e.printStackTrace();
						}
						BlockVector3 min = reg.getMinimumPoint();
						BlockVector3 max = reg.getMaximumPoint();
						int affected = 0;
						int minX = min.getBlockX();
						int minY = min.getBlockY();
						int minZ = min.getBlockZ();
						int maxX = max.getBlockX();
						int maxY = max.getBlockY();
						int maxZ = max.getBlockZ();
						for (int x = minX; x <= maxX; x++) {
							for (int y = minY; y <= maxY; y++) {
								for (int z = minZ; z <= maxZ; z++) {
									Location fpt = new Location(player.getWorld(), x, y, z);
									Block dablock = fpt.getBlock();
									CBlock conBlock = map.get(player);
									if (!conBlock.hasBlock(fpt)) {
										if (conBlock.addBlock(dablock)) {
											affected++;
										}
									}
								}
							}
						}
						sender.sendMessage(prefix + affected + " blocks added to ControllerBlock");
					} else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("s")) {
						Region reg = null;
						try {
							reg = getwe().getSessionManager().findByName(player.getName())
									.getSelection(new BukkitWorld(player.getWorld()));
						} catch (Exception e) {
							e.printStackTrace();
						}
						BlockVector3 min = reg.getMinimumPoint();
						BlockVector3 max = reg.getMaximumPoint();
						int affected = 0;
						int minX = min.getBlockX();
						int minY = min.getBlockY();
						int minZ = min.getBlockZ();
						int maxX = max.getBlockX();
						int maxY = max.getBlockY();
						int maxZ = max.getBlockZ();
						for (int x = minX; x <= maxX; x++) {
							for (int y = minY; y <= maxY; y++) {
								for (int z = minZ; z <= maxZ; z++) {
									Location fpt = new Location(player.getWorld(), x, y, z);
									Block dablock = fpt.getBlock();
									CBlock conBlock = map.get(player);
									player.sendMessage(String.valueOf(dablock.getBlockData().getAsString()));
									if (!conBlock.hasBlock(fpt)) {
										if (conBlock.delBlock(dablock)) {
											affected++;
										}
									}
								}
							}
						}
						sender.sendMessage(prefix + affected + " blocks removed from ControllerBlock");
					}
				}
			}
		} else if ((sender instanceof ConsoleCommandSender)) {
			if (label.equals("cblock")) {
				if (args[0].equals("reload")) {
					loadConfig();
					log.info("Config reloaded");
				}
			}
		}

		return true;
	}

	public Config getConfigu() {
		return config;
	}

	public PermissionHandler getPerm() {
		return permissionHandler;
	}

	public CBlock createCBlock(Location l, String o, byte pl) {
		CBlock c = new CBlock(this, l, o, pl);
		blocks.add(c);
		return c;
	}

	private WorldEdit getwe() {
		return WorldEdit.getInstance();
	}

	public CBlock destroyCBlock(Location l, boolean drops) {
		CBlock block = getCBlock(l);
		if (block == null) {
			return block;
		}
		if (drops) {
			block.destroy();
		} else {
			block.destroyWithOutDrops();
		}
		blocks.remove(block);
		deleteData(l);
		return block;
	}

	public CBlock destroyCBlock(Location l) {
		CBlock block = getCBlock(l);
		if (block == null) {
			return block;
		}
		block.destroy();
		blocks.remove(block);
		deleteData(l);
		return block;
	}

	public CBlock getCBlock(Location l) {
		for (Iterator<CBlock> i = blocks.iterator(); i.hasNext();) {
			CBlock block = i.next();
			if (Util.locEquals(block.getLoc(), l)) {
				return block;
			}
		}
		return null;
	}

	public boolean isControlBlock(Location l) {
		return getCBlock(l) != null;
	}

	public boolean isControlledBlock(Location l) {
		return getControllerBlockFor(null, l, null, null) != null;
	}

	public boolean isControlledBlock(Location l, BlockData m) {
		return getControllerBlockFor(null, l, m, null) != null;
	}

	public CBlock getControllerBlockFor(CBlock c, Location l, BlockData m, Boolean o) {
		for (Iterator<CBlock> i = blocks.iterator(); i.hasNext();) {
			CBlock block = i.next();

			if ((c != block) && ((m == null) || (m.getMaterial().equals(block.getType())))
					&& ((o == null) || (o.equals(block.isOn()))) && (block.hasBlock(l))) {
				return block;
			}
		}
		return null;
	}

	public CBlock moveControllerBlock(CBlock c, Location l) {
		Iterator<?> oldBlockDescs = c.getBlocks();
		CBlock newC = createCBlock(l, c.getOwner(), c.protectedLevel);
		newC.setType(c.getType());
		if (c.isOn()) {
			while (oldBlockDescs.hasNext()) {
				newC.addBlock(((BlockDesc) oldBlockDescs.next()).blockLoc.getBlock());
			}
			destroyCBlock(c.getLoc(), false);
			return newC;
		}
		return null;
	}

	private void checkPluginDataDir() {
		log.debug("Checking plugin data directory " + getDataFolder());
		File dir = getDataFolder();
		if (!dir.isDirectory()) {
			log.debug("Isn't a directory");
			if (!dir.mkdir()) {
				log.severe("Couldn't create plugin data directory " + getDataFolder());
				return;
			}
		}
	}

	public void loadData() {
		int v = 1;
		String s = "";
		Integer i = Integer.valueOf(0);
		Integer l = Integer.valueOf(1);
		File folder = new File(getDataFolder() + "/" + "blocks");
		File[] listofBlocks = folder.listFiles();
		if (listofBlocks != null) {
			for (int i1 = 0; i1 < listofBlocks.length; i1++) {
				if (listofBlocks[i1].isFile()) {
					v = 1;
					s = "";
					try {
						BufferedReader in = new BufferedReader(
								new InputStreamReader(
										new FileInputStream(
												getDataFolder() + "/" + "blocks" + "/" + listofBlocks[i1].getName()),
										"UTF-8"));
						String version = in.readLine().trim();
						if (version.equals("# v2")) {
							v = 2;
						} else if (version.equals("# v3")) {
							v = 3;
						} else if (version.equals("# v4")) {
							v = 4;
						} else {
							l = Integer.valueOf(l.intValue() - 1);
						}
						if ((s = in.readLine()) != null) {
							if (!s.trim().isEmpty()) {
								CBlock newBlock = new CBlock(this, v, s.trim());
								if (newBlock.getLoc() != null) {
									blocks.add(newBlock);
								}
							}
						}
						in.close();
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (FileNotFoundException localFileNotFoundException) {
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		log.info("Loaded v" + v + " data - " + i + " ControllerBlocks loaded");
	}

	public void saveData(CBlock cblock) {
		log.debug("Saving ControllerBlock data");
		String dump = "# v4" + "\n" + cblock.serialize();
		try {
			new File(getDataFolder() + "/" + "blocks").mkdir();
			Writer out = new OutputStreamWriter(
					new FileOutputStream(
							getDataFolder() + "/" + "blocks" + "/" + Util.getSaveFileName(cblock.getLoc()) + ".tmp"),
					"UTF-8");
			out.write(dump);
			out.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			log.severe("ERROR: Couldn't open the file to write ControllerBlock data to!");
			log.severe("       Check your server installation has write access to " + getDataFolder());
			e.printStackTrace();
		} catch (IOException e) {
			log.severe("ERROR: Couldn't save ControllerBlock data! Possibly corrupted/incomplete data");
			log.severe("       Check if the disk is full, then edit/finish editing a ControllerBlock");
			log.severe("       in game to try to save again.");
			e.printStackTrace();
		}
		File newData = new File(
				getDataFolder() + "/" + "blocks" + "/" + Util.getSaveFileName(cblock.getLoc()) + ".tmp");
		File curData = new File(
				getDataFolder() + "/" + "blocks" + "/" + Util.getSaveFileName(cblock.getLoc()) + ".dat");
		if (!curData.delete()) {
			log.warning("Error when attempting to delete block: " + Util.getSaveFileName(cblock.getLoc()));
		}
		if (!newData.renameTo(curData)) {
			log.severe("ERROR: Couldn't move temporary save file over current save file");
			log.severe("       Check that your server installation has write access to: " + getDataFolder() + "/"
					+ "blocks" + "/" + Util.getSaveFileName(cblock.getLoc()) + ".dat");
		}
	}

	public void deleteData(Location l) {
		File f1 = new File(getDataFolder() + "/" + "blocks" + "/" + Util.getSaveFileName(l) + ".dat");
		boolean success = f1.delete();
		if (!success) {
			log.warning("Error when attempting to delete block: " + Util.getSaveFileName(l) + ".dat");
		} else {

		}
	}

	public Material getCBlockType() {
		return CBlockType;
	}

	public Material getSemiProtectedCBlockType() {
		return semiProtectedCBlockType;
	}

	public Material getUnProtectedCBlockType() {
		return unProtectedCBlockType;
	}

	public boolean isValidMaterial(Material m) {
		if (!m.isBlock()) {
			return false;
		}
		Iterator<Material> i = DisallowedTypesAll.iterator();
		while (i.hasNext()) {
			if (i.next().equals(m)) {
				return false;
			}
		}
		return true;
	}

	public boolean isUnprotectedMaterial(Material m) {
		if (!m.isBlock()) {
			return false;
		}
		Iterator<Material> i = UnprotectedBlocks.iterator();
		while (i.hasNext()) {
			if (i.next().equals(m)) {
				return true;
			}
		}
		return false;
	}

	private void loadError(String cmd, String arg, Integer line, String def) {
		if (def.length() != 0) {
			def = "defaulting to " + def;
		} else {
			def = "it has been skipped";
		}
		log.warning("Couldn't parse " + cmd + " " + arg + " on line " + line + ", " + def);
	}

	private void loadConfig() {
		Integer oldConfigLine = Integer.valueOf(-1);
		Integer l = Integer.valueOf(0);
		ConfigSections c = ConfigSections.oldConfig;
		List<String> configText = new ArrayList<String>();
		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(new FileInputStream(getDataFolder() + "/" + configFile), "UTF-8"));
			String s;
			while ((s = in.readLine()) != null) {
				configText.add(s.trim());
				l = Integer.valueOf(l.intValue() + 1);
				if ((s.trim().isEmpty()) || (s.startsWith("#"))) {
					continue;
				}
				if (s.toLowerCase().trim().equals("[general]")) {
					c = ConfigSections.general;
				} else if (s.toLowerCase().trim().equals("[adminplayers]")) {
					c = ConfigSections.adminPlayers;
				} else if (s.toLowerCase().trim().equals("[disallowed]")) {
					c = ConfigSections.disallowedAll;
				} else if (s.toLowerCase().trim().equals("[unprotected]")) {
					c = ConfigSections.unprotectedBlocks;
				} else if (c.equals(ConfigSections.general)) {
					String[] line = s.split("=", 2);
					if (line.length >= 2) {
						String cmd = line[0].toLowerCase();
						String arg = line[1];
						if (cmd.equals("ControllerBlockType".toLowerCase())) {
							CBlockType = Material.getMaterial(arg);
							if (CBlockType == null) {
								loadError("ControllerBlockType", arg, l, "IRON_BLOCK");
								CBlockType = Material.IRON_BLOCK;
							}
							config.setOpt(Config.Option.ControllerBlockType, CBlockType);
						} else if (cmd.equals("SemiProtectedControllerBlockType".toLowerCase())) {
							semiProtectedCBlockType = Material.getMaterial(arg);
							if (semiProtectedCBlockType == null) {
								loadError("SemiProtectedControllerBlockType", arg, l, "GOLD_BLOCK");
								semiProtectedCBlockType = Material.GOLD_BLOCK;
							}
							config.setOpt(Config.Option.SemiProtectedControllerBlockType, semiProtectedCBlockType);
						} else if (cmd.equals("UnProtectedControllerBlockType".toLowerCase())) {
							unProtectedCBlockType = Material.getMaterial(arg);
							if (unProtectedCBlockType == null) {
								loadError("UnProtectedControllerBlockType", arg, l, "DIAMOND_BLOCK");
								unProtectedCBlockType = Material.DIAMOND_BLOCK;
							}
							config.setOpt(Config.Option.UnProtectedControllerBlockType, unProtectedCBlockType);
						} else if (cmd.equals("QuickRedstoneCheck".toLowerCase())) {
							config.setOpt(Config.Option.QuickRedstoneCheck, Boolean.valueOf(Boolean.parseBoolean(arg)));
						} else if (cmd.equals("BlockProtectMode".toLowerCase())) {
							config.setOpt(Config.Option.BlockProtectMode, BlockProtectMode.valueOf(arg.toLowerCase()));
						} else if (cmd.equals("BlockEditProtectMode".toLowerCase())) {
							config.setOpt(Config.Option.BlockEditProtectMode,
									BlockProtectMode.valueOf(arg.toLowerCase()));
						} else if (cmd.equals("BlockPhysicsProtectMode".toLowerCase())) {
							config.setOpt(Config.Option.BlockPhysicsProtectMode,
									BlockProtectMode.valueOf(arg.toLowerCase()));
						} else if (cmd.equals("BlockFlowProtectMode".toLowerCase())) {
							config.setOpt(Config.Option.BlockFlowProtectMode,
									BlockProtectMode.valueOf(arg.toLowerCase()));
						} else if (cmd.equals("DisableEditDupeProtection".toLowerCase())) {
							config.setOpt(Config.Option.DisableEditDupeProtection,
									Boolean.valueOf(Boolean.parseBoolean(arg)));
						} else if (cmd.equals("PistonProtection".toLowerCase())) {
							config.setOpt(Config.Option.PistonProtection, Boolean.valueOf(Boolean.parseBoolean(arg)));
						} else if (cmd.equals("MaxBlocksPerController".toLowerCase())) {
							config.setOpt(Config.Option.MaxBlocksPerController, Integer.valueOf(Integer.parseInt(arg)));
						} else if (cmd.equals("MaxDistanceFromController".toLowerCase())) {
							config.setOpt(Config.Option.MaxDistanceFromController,
									Integer.valueOf(Integer.parseInt(arg)));
						} else if (cmd.equals("DisableNijikokunPermissions".toLowerCase())) {
							config.setOpt(Config.Option.DisablePermissions,
									Boolean.valueOf(Boolean.parseBoolean(arg)));
						} else if (cmd.equals("ServerOpIsAdmin".toLowerCase())) {
							config.setOpt(Config.Option.ServerOpIsAdmin, Boolean.valueOf(Boolean.parseBoolean(arg)));
						} else if (cmd.equals("AnyoneCanCreate".toLowerCase())) {
							config.setOpt(Config.Option.AnyoneCanCreate, Boolean.valueOf(Boolean.parseBoolean(arg)));
						} else if (cmd.equals("AnyoneCanModifyOther".toLowerCase())) {
							config.setOpt(Config.Option.AnyoneCanModifyOther,
									Boolean.valueOf(Boolean.parseBoolean(arg)));
						} else if (cmd.equals("AnyoneCanDestroyOther".toLowerCase())) {
							config.setOpt(Config.Option.AnyoneCanDestroyOther,
									Boolean.valueOf(Boolean.parseBoolean(arg)));
						}
					}
				} else if (c.equals(ConfigSections.adminPlayers)) {
					permissionHandler.addBuiltinAdminPlayer(s.trim());
				} else if (c.equals(ConfigSections.disallowedAll)) {
					Material m = Material.getMaterial(s.trim());
					if (m == null) {
						loadError("disallowed type", s.trim(), l, "");
					} else {
						DisallowedTypesAll.add(m);
					}
				} else if (c.equals(ConfigSections.unprotectedBlocks)) {
					Material m = Material.getMaterial(s.trim());
					if (m == null) {
						loadError("disallowed type", s.trim(), l, "");
					} else {
						UnprotectedBlocks.add(m);
					}

				} else if (c.equals(ConfigSections.oldConfig)) {
					if (oldConfigLine.intValue() == -1) {
						CBlockType = Material.getMaterial(s.trim());
						if (CBlockType == null) {
							log.warning(
									"Couldn't parse ControllerBlock type " + s.trim() + ", defaulting to IRON_BLOCK");
							CBlockType = Material.IRON_BLOCK;
						}
						config.setOpt(Config.Option.ControllerBlockType, CBlockType);
						oldConfigLine = Integer.valueOf(oldConfigLine.intValue() + 1);
					} else {
						Material m = Material.getMaterial(s.trim());
						if (m == null) {
							log.warning("Couldn't parse disallowed type " + s.trim() + ", it has been skipped");
						} else {
							DisallowedTypesAll.add(m);
							oldConfigLine = Integer.valueOf(oldConfigLine.intValue() + 1);
						}
					}
				}
			}
			writeConfig(configText);
			in.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			log.warning("No config found, using defaults, writing defaults out to " + configFile);
			writeConfig(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		CBlockType = ((Material) config.getOpt(Config.Option.ControllerBlockType));
		log.info("Using " + CBlockType + " (" + CBlockType.name() + ") as ControllerBlock, loaded "
				+ DisallowedTypesAll.size() + " disallowed types from config");
	}

	private String writePatch(ConfigSections c) {
		String dump = "";
		if (c == null) {
			return dump;
		}
		if (c.equals(ConfigSections.general)) {
			if (!config.hasOption(Config.Option.ControllerBlockType)) {
				dump = dump + "\n";
				dump = dump + "# ControllerBlockType is the material allowed of new ControllerBlocks\n";
				dump = dump + "# Doesn't affect already assigned ControllerBlocks\n";
				dump = dump + "ControllerBlockType=" + config.getOpt(Config.Option.ControllerBlockType) + "\n";
			}
			if (!config.hasOption(Config.Option.SemiProtectedControllerBlockType)) {
				dump = dump + "\n";
				dump = dump + "# SemiProtectedControllerBlockType is the material that semi-protected\n";
				dump = dump + "# Controller Blocks are made from, this block will turn on in a protected\n";
				dump = dump + "# state, but when turned off, blocks controlled won't disappear, instead\n";
				dump = dump + "# they lose their protection and can be destroyed\n";
				dump = dump + "SemiProtectedControllerBlockType="
						+ config.getOpt(Config.Option.SemiProtectedControllerBlockType) + "\n";
			}
			if (!config.hasOption(Config.Option.UnProtectedControllerBlockType)) {
				dump = dump + "\n";
				dump = dump + "# UnProtectedControllerBlockType is the material that unprotected\n";
				dump = dump + "# Controller Blocks are made from, blocks controlled by this will create\n";
				dump = dump + "# when turned on, but won't disappear when turned off, much like the\n";
				dump = dump + "# semi-protected controlled blocks, however, blocks controlled have no\n";
				dump = dump + "# protection against being broken even in the on state\n";
				dump = dump + "UnProtectedControllerBlockType="
						+ config.getOpt(Config.Option.UnProtectedControllerBlockType) + "\n";
			}
			if (!config.hasOption(Config.Option.QuickRedstoneCheck)) {
				dump = dump + "\n";
				dump = dump
						+ "# QuickRedstoneCheck to false enables per-tick per-controllerblock isBlockPowered() checks\n";
				dump = dump + "# This is potentially laggier, but blocks can be powered like regular redstone blocks\n";
				dump = dump + "# If set to true, wire needs to be run on top of the controller block\n";
				dump = dump + "QuickRedstoneCheck=" + config.getOpt(Config.Option.QuickRedstoneCheck) + "\n";
			}
			if (!config.hasOption(Config.Option.BlockProtectMode)) {
				dump = dump + "\n";
				dump = dump + "# BlockProtectMode changes how we handle destroying controlled blocks\n";
				dump = dump + "# It has 3 modes:\n";
				dump = dump + "# protect - default, tries to prevent controlled blocks from being destroyed\n";
				dump = dump + "# remove - removes controlled blocks from controller if destroyed\n";
				dump = dump + "# none - don't do anything, this effectively makes controlled blocks dupable\n";
				dump = dump + "BlockProtectMode=" + config.getOpt(Config.Option.BlockProtectMode) + "\n";
			}
			if (!config.hasOption(Config.Option.BlockPhysicsProtectMode)) {
				dump = dump + "\n";
				dump = dump + "# BlockPhysicsProtectMode changes how we handle changes against controlled blocks\n";
				dump = dump + "# It has 3 modes:\n";
				dump = dump + "# protect - default, stops physics interactions with controlled blocks\n";
				dump = dump + "# remove - removes controlled blocks from controller if changed\n";
				dump = dump + "# none - don't do anything, could have issues with some blocks\n";
				dump = dump + "BlockPhysicsProtectMode=" + config.getOpt(Config.Option.BlockPhysicsProtectMode) + "\n";
			}
			if (!config.hasOption(Config.Option.BlockFlowProtectMode)) {
				dump = dump + "\n";
				dump = dump
						+ "# BlockFlowProtectMode changes how we handle water/lava flowing against controlled blocks\n";
				dump = dump + "# It has 3 modes:\n";
				dump = dump + "# protect - default, tries to prevent controlled blocks from being interacted\n";
				dump = dump + "# remove - removes controlled blocks from controller if flow event on it\n";
				dump = dump + "# none - don't do anything, things that drop when flowed over can be dupable\n";
				dump = dump + "BlockFlowProtectMode=" + config.getOpt(Config.Option.BlockFlowProtectMode) + "\n";
			}
			if (!config.hasOption(Config.Option.DisableEditDupeProtection)) {
				dump = dump + "\n";
				dump = dump + "# DisableEditDupeProtection set to true disables all the checks for changes while in\n";
				dump = dump + "# edit mode, this will make sure blocks placed in a spot will always be in that spot\n";
				dump = dump + "# even if they get removed by some kind of physics/flow event in the meantime\n";
				dump = dump + "DisableEditDupeProtection=" + config.getOpt(Config.Option.DisableEditDupeProtection)
						+ "\n";
			}
			if (!config.hasOption(Config.Option.PistonProtection)) {
				dump = dump + "\n";
				dump = dump + "# PistonProtection set to true disables the ability of Pistons to move\n";
				dump = dump + "# ControllerBlocks or controlled Blocks.\n";
				dump = dump + "PistonProtection=" + config.getOpt(Config.Option.PistonProtection) + "\n";
			}
			if (!config.hasOption(Config.Option.MaxDistanceFromController)) {
				dump = dump + "\n";
				dump = dump + "# MaxDistanceFromController sets how far away controlled blocks are allowed\n";
				dump = dump + "# to be attached and controlled to a controller block - 0 for infinte/across worlds\n";
				dump = dump + "MaxDistanceFromController=" + config.getOpt(Config.Option.MaxDistanceFromController)
						+ "\n";
			}
			if (!config.hasOption(Config.Option.MaxBlocksPerController)) {
				dump = dump + "\n";
				dump = dump + "# MaxControlledBlocksPerController sets how many blocks are allowed to be attached\n";
				dump = dump + "# to a single controller block - 0 for infinite\n";
				dump = dump + "MaxBlocksPerController=" + config.getOpt(Config.Option.MaxBlocksPerController) + "\n";
			}
			if (!config.hasOption(Config.Option.DisablePermissions)) {
				dump = dump + "\n";
				dump = dump + "# Permissions support\n";
				dump = dump + "# The nodes for permissions are:\n";
				dump = dump + "# controllerblock.admin - user isn't restricted by block counts or distance, able to\n";
				dump = dump + "#                         create/modify/destroy other users controllerblocks\n";
				dump = dump + "# controllerblock.create - user is allowed to setup controllerblocks\n";
				dump = dump
						+ "# controllerblock.modifyOther - user is allowed to modify other users controllerblocks\n";
				dump = dump
						+ "# controllerblock.destroyOther - user is allowed to destroy other users controllerblocks\n";
				dump = dump + "#\n";
				dump = dump + "# DisablePermissions will disable any lookups against Permissions if you\n";
				dump = dump + "# do have it installed, but want to disable this plugins use of it anyway\n";
				dump = dump + "# Note: You don't have to do this, the plugin isn't dependant on Permissions\n";
				dump = dump + "DisableNijikokunPermissions=" + config.getOpt(Config.Option.DisablePermissions)
						+ "\n";
			}
			if (!config.hasOption(Config.Option.ServerOpIsAdmin)) {
				dump = dump + "\n";
				dump = dump + "# Users listed in ops.txt (op through server console) counts as an admin\n";
				dump = dump + "ServerOpIsAdmin=" + config.getOpt(Config.Option.ServerOpIsAdmin) + "\n";
			}
			if (!config.hasOption(Config.Option.AnyoneCanCreate)) {
				dump = dump + "\n";
				dump = dump + "# Everyone on the server can create new ControllerBlocks\n";
				dump = dump + "AnyoneCanCreate=" + config.getOpt(Config.Option.AnyoneCanCreate) + "\n";
			}
			if (!config.hasOption(Config.Option.AnyoneCanModifyOther)) {
				dump = dump + "\n";
				dump = dump + "# Everyone can modify everyone elses ControllerBlocks\n";
				dump = dump + "AnyoneCanModifyOther=" + config.getOpt(Config.Option.AnyoneCanModifyOther) + "\n";
			}
			if (!config.hasOption(Config.Option.AnyoneCanDestroyOther)) {
				dump = dump + "\n";
				dump = dump + "# Everyone can destroy everyone elses ControllerBlocks\n";
				dump = dump + "AnyoneCanDestroyOther=" + config.getOpt(Config.Option.AnyoneCanDestroyOther) + "\n";
			}
		}
		if (dump.length() != 0) {
			dump = dump + "\n";
		}
		return dump;
	}

	private void writeConfig(List<String> prevConfig) {
		String dump = "";
		if (prevConfig == null) {
			dump = "# ControllerBlock configuration file\n";
			dump = dump + "\n";
			dump = dump + "# Blank lines and lines starting with # are ignored\n";
			dump = dump
					+ "# Material names can be found: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html\n";
			dump = dump + "\n";
		}
		boolean hasGeneral = false;
		boolean hasAdminPlayers = false;
		boolean hasDisallowed = false;
		ConfigSections c = null;

		if (prevConfig != null) {
			Iterator<String> pci = prevConfig.listIterator();
			while (pci.hasNext()) {
				String line = pci.next();
				if (line.toLowerCase().trim().equals("[general]")) {
					dump = dump + writePatch(c);
					c = ConfigSections.general;
					hasGeneral = true;
				} else if (line.toLowerCase().trim().equals("[adminplayers]")) {
					dump = dump + writePatch(c);
					c = ConfigSections.adminPlayers;
					hasAdminPlayers = true;
				} else if (line.toLowerCase().trim().equals("[disallowed]")) {
					dump = dump + writePatch(c);
					c = ConfigSections.disallowedAll;
					hasDisallowed = true;
				}
				dump = dump + line + "\n";
			}
			pci = null;
			dump = dump + writePatch(c);
		}

		if (!hasGeneral) {
			dump = dump + "[general]\n";
			dump = dump + writePatch(ConfigSections.general);
			dump = dump + "\n";
		}
		if (!hasAdminPlayers) {
			dump = dump + "[adminPlayers]\n";
			dump = dump + "# One name per line, users listed here are admins, and can\n";
			dump = dump + "# create/modify/destroy all ControllerBlocks on the server\n";
			dump = dump + "# Block restrictions don't apply to admins\n";
			dump = dump + "\n";
		}
		if (!hasDisallowed) {
			dump = dump + "[disallowed]\n";
			dump = dump + "# Add disallowed blocks here, one Material per line.\n";
			dump = dump
					+ "# Item Materials that are not blocks are excluded automatically due to failing Material.isBlock() check\n";
			dump = dump + "#POPPY\n#DANDELION\n#RED_MUSHROOM\n#BROWN_MUSHROOM\n";
			dump = dump + "\n";
			Iterator<Material> i = DisallowedTypesAll.listIterator();
			while (i.hasNext()) {
				dump = dump + i.next() + "\n";
			}
			dump = dump + "[unprotected]\n";
			dump = dump + "# Add unprotected blocks here, one Material per line.\n";
			dump = dump
					+ "# Item Materials that are not blocks are excluded automatically due to failing Material.isBlock() check\n";
			dump = dump
					+ "# These Blocks ARE allowed to be pushed by Pistons and to be used with (semi) unprotected CBlocks.\n";
			dump = dump + "#POPPY\n#DANDELION\n#RED_MUSHROOM\n#BROWN_MUSHROOM\n";
			dump = dump + "\n";
			i = UnprotectedBlocks.listIterator();
			while (i.hasNext()) {
				dump = dump + i.next() + "\n";
			}
		}
		try {
			Writer out = new OutputStreamWriter(new FileOutputStream(getDataFolder() + "/" + configFile), "UTF-8");
			out.write(dump);
			out.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		loadData();
	}
}