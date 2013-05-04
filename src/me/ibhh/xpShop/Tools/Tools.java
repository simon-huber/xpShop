package me.ibhh.xpShop.Tools;

import me.ibhh.xpShop.xpShop;
import me.ibhh.xpShop.Exceptions.InvalidXPAmountException;
import me.ibhh.xpShop.Exceptions.PlayerNotOnlineException;
import me.ibhh.xpShop.Exceptions.PlayerWasNeverOnlineException;
import me.ibhh.xpShop.send.sql.XPSend;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * 
 * @author ibhh
 */
public abstract class Tools {

	/**
	 * Determines if all packages in a String array are within the Classpath
	 * This is the best way to determine if a specific plugin exists and will be
	 * loaded. If the plugin package isn't loaded, we shouldn't bother waiting
	 * for it!
	 * 
	 * @param packages
	 *            String Array of package names to check
	 * @return Success or Failure
	 */
	public static boolean packagesExists(String... packages) {
		try {
			for (String pkg : packages) {
				Class.forName(pkg);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isInteger(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	public static boolean isFloat(String input) {
		try {
			Float.parseFloat(input);
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	public static String[] stringtoArray(String s, String sep) {
		// convert a String s to an Array, the elements
		// are delimited by sep
		// NOTE : for old JDK only (<1.4).
		// for JDK 1.4 +, use String.split() instead
		StringBuffer buf = new StringBuffer(s);
		int arraysize = 1;
		for (int i = 0; i < buf.length(); i++) {
			if (sep.indexOf(buf.charAt(i)) != -1) {
				arraysize++;
			}
		}
		String[] elements = new String[arraysize];
		int y, z = 0;
		if (buf.toString().indexOf(sep) != -1) {
			while (buf.length() > 0) {
				if (buf.toString().indexOf(sep) != -1) {
					y = buf.toString().indexOf(sep);
					if (y != buf.toString().lastIndexOf(sep)) {
						elements[z] = buf.toString().substring(0, y);
						z++;
						buf.delete(0, y + 1);
					} else if (buf.toString().lastIndexOf(sep) == y) {
						elements[z] = buf.toString().substring(0, buf.toString().indexOf(sep));
						z++;
						buf.delete(0, buf.toString().indexOf(sep) + 1);
						elements[z] = buf.toString();
						z++;
						buf.delete(0, buf.length());
					}
				}
			}
		} else {
			elements[0] = buf.toString();
		}
		buf = null;
		return elements;
	}

	public static Player getmyOfflinePlayer(xpShop plugin, String[] args, int index) throws PlayerNotOnlineException, PlayerWasNeverOnlineException {
		return getmyOfflinePlayer(plugin, args[index]);
	}

	public static Player getmyOfflinePlayer(xpShop plugin, String playername) throws PlayerNotOnlineException, PlayerWasNeverOnlineException {
		plugin.Logger("Empfaenger: " + playername, "Debug");
		Player player = plugin.getServer().getPlayer(playername);
		if (player != null) {
			if (player.isOnline()) {
				plugin.Logger("Player is online!", "Debug");
				return player;
			} else {
				throw new PlayerNotOnlineException(plugin, playername);
			}
		}
		boolean wasonline = false;
		for (OfflinePlayer p : Bukkit.getServer().getOfflinePlayers()) {
			if (p.getName().toLowerCase().equals(playername.toLowerCase())) {
				if (p != null) {
					if (p.hasPlayedBefore()) {
						wasonline = true;
						plugin.Logger("Player has Played before: " + p.getName(), "Debug");
					}
					break;
				}
			}
		}
		if (!wasonline) {
			throw new PlayerWasNeverOnlineException(plugin, playername);
		} else {
			throw new PlayerNotOnlineException(plugin, playername);
		}
	}

	public static boolean addDB(final xpShop plugin, final String player, final String sender, final int amount) throws InvalidXPAmountException {
		if (amount > 0) {
			final XPSend send = new XPSend(player, sender, amount, String.format(plugin.config.commandsuccessrecievedxp, amount, sender), 0);
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

				@Override
				public void run() {
					plugin.getSendDatabase().InsertSend(send);
				}
			});
		} else {
			throw new InvalidXPAmountException(amount);
		}
		return true;
	}

}
