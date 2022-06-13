package me.operon.controllerblockwe;

import java.util.HashMap;

import org.bukkit.Material;
public class Config {
	private HashMap<Option, Object> options = new HashMap<Option, Object>();

	public void setOpt(Option opt, Object arg) {
		options.put(opt, arg);
	}

	public boolean getBool(Option opt) {
		return ((Boolean) getOpt(opt)).booleanValue();
	}

	public Integer getInt(Option opt) {
		return (Integer) getOpt(opt);
	}

	public Object getOpt(Option opt) {
		if (!hasOption(opt)) {
			switch (opt) {
				case AnyoneCanCreate:
					return Boolean.valueOf(true);
				case AnyoneCanDestroyOther:
					return Boolean.valueOf(false);
				case AnyoneCanModifyOther:
					return Boolean.valueOf(false);
				case MaxBlocksPerController:
					return Integer.valueOf(0);
				case DisablePermissions:
					return Boolean.valueOf(true);
				case SemiProtectedControllerBlockType:
					return Material.GOLD_BLOCK;
				case ServerOpIsAdmin:
					return Boolean.valueOf(true);
				case PistonProtection:
					return Boolean.valueOf(false);
				case ControllerBlockType:
					return Material.IRON_BLOCK;
				case DisableEditDupeProtection:
					return Boolean.valueOf(false);
				case MaxDistanceFromController:
					return Integer.valueOf(0);
				case BlockEditProtectMode:
					return BlockProtectMode.protect;
				case BlockFlowProtectMode:
					return BlockProtectMode.protect;
				case BlockPhysicsProtectMode:
					return BlockProtectMode.protect;
				case QuickRedstoneCheck:
					return Boolean.valueOf(true);
				case UnProtectedControllerBlockType:
					return Material.DIAMOND_BLOCK;
				case BlockProtectMode:
					return BlockProtectMode.protect;
			default:
				break;
			}
		}
		return options.get(opt);
	}

	public boolean hasOption(Option opt) {
		return options.containsKey(opt);
	}

	public static enum Option {
		ControllerBlockType, SemiProtectedControllerBlockType, UnProtectedControllerBlockType, ServerOpIsAdmin, AnyoneCanCreate, AnyoneCanModifyOther, AnyoneCanDestroyOther, MaxBlocksPerController, MaxDistanceFromController, BlockProtectMode, QuickRedstoneCheck, DisablePermissions, DisableEditDupeProtection, BlockEditProtectMode, BlockPhysicsProtectMode, BlockFlowProtectMode, PistonProtection;
	}
}