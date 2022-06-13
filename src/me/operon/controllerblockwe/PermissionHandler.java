package me.operon.controllerblockwe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.entity.Player;

public class PermissionHandler {
	private ControllerBlock parent = null;

	private List<String> builtinAdminPlayers = new ArrayList<String>();

	public PermissionHandler(ControllerBlock p) {
		parent = p;
	}

	public boolean checkPermissionsEx(Player p, String perm) {
		if(p.hasPermission(perm)){
			return true;
		}
		return false;
	}

	public void addBuiltinAdminPlayer(String name) {
		builtinAdminPlayers.add(name);
	}

	public boolean isAdminPlayer(Player p) {
		parent.log.debug("Checking if " + p.getName() + " is a CB admin");
		if ((parent.getConfigu().getBool(Config.Option.ServerOpIsAdmin))
				&& (p.isOp())) {
			parent.log.debug(p.getName()
					+ " is a server operator, and serverOpIsAdmin is set");
			return true;
		}

		if (checkPermissionsEx(p, "controllerblock.admin")) {
			parent.log.debug("Permissions said " + p.getName()
					+ " has admin permissions");
			return true;
		}

		String pn = p.getName();
		Iterator<String> i = builtinAdminPlayers.iterator();
		while (i.hasNext()) {
			if (i.next().equals(pn)) {
				parent.log.debug(p.getName()
						+ " is listed in the ControllerBlock.ini as an admin");
				return true;
			}
		}
		parent.log.debug(p.getName() + " isn't an admin");
		return false;
	}

	public boolean canCreate(Player p) {
		if (isAdminPlayer(p)) {
			parent.log.debug(p.getName() + " is an admin, can create");
			return true;
		}

		if (checkPermissionsEx(p, "controllerblock.create")) {
			parent.log.debug("Permissions said " + p.getName()
					+ " can create");
			return true;
		}

		if (parent.getConfigu().getBool(Config.Option.AnyoneCanCreate)) {
			parent.log.debug("Anyone is allowed to create, letting "
					+ p.getName() + " create");
		}
		return parent.getConfigu().getBool(Config.Option.AnyoneCanCreate);
	}

	public boolean canModify(Player p) {
		if (isAdminPlayer(p)) {
			parent.log.debug(p.getName() + " is an admin, can modify");
			return true;
		}

		if (checkPermissionsEx(p, "controllerblock.modifyOther")) {
			parent.log.debug("Permissions says " + p.getName()
					+ " has global modify permissions");
			return true;
		}

		if (parent.getConfigu().getBool(Config.Option.AnyoneCanModifyOther)) {
			parent.log
					.debug("Anyone is allowed to modify anyones blocks, allowing "
							+ p.getName() + " to modify");
		}
		return parent.getConfigu().getBool(Config.Option.AnyoneCanModifyOther);
	}

	public boolean canModify(Player p, CBlock c) {
		if (p.getName().equals(c.getOwner())) {
			parent.log.debug(p.getName()
					+ " owns this controller, allowing to modify");
			return true;
		}
		return canModify(p);
	}

	public boolean canDestroy(Player p) {
		if (isAdminPlayer(p)) {
			parent.log.debug(p.getName() + " is an admin, allowing destroy");
			return true;
		}

		if (checkPermissionsEx(p, "controllerblock.destroyOther")) {
			parent.log.debug("Permissions says " + p.getName()
					+ " has global destroy permissions");
			return true;
		}

		if (parent.getConfigu().getBool(Config.Option.AnyoneCanDestroyOther)) {
			parent.log
					.debug("Anyone is allowed to destroy anyones blocks, allowing "
							+ p.getName() + " to destroy");
		}
		return parent.getConfigu().getBool(Config.Option.AnyoneCanDestroyOther);
	}

	public boolean canDestroy(Player p, CBlock c) {
		if (p.getName().equals(c.getOwner())) {
			parent.log.debug(p.getName()
					+ "owns this controller, allowing them to destroy it");
			return true;
		}
		return canDestroy(p);
	}
}