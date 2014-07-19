/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package me.ibhh.xpShop;

import me.ibhh.MoneyLib.NoiConomyPluginFound;
import me.ibhh.xpShop.Exceptions.PlayerNotOnlineException;
import me.ibhh.xpShop.Exceptions.PlayerWasNeverOnlineException;
import me.ibhh.xpShop.Tools.Tools;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * 
 * @author ibhh
 */
public class SignHandler {

    private xpShop plugin;

    public SignHandler(xpShop pl) {
	plugin = pl;
    }

    public void Create(SignChangeEvent event) {
	Player p = event.getPlayer();
	String[] line = event.getLines();
	plugin.Logger("First Line [xpShop]", "Debug");
	if (!plugin.Blacklistcode.startsWith("1", 10)) {
	    plugin.Logger(plugin.Blacklistcode, "Debug");
	    try {
		if (plugin.ListenerShop.blockIsValid(line, "create", p)) {
		    plugin.Logger("Createing Shop: ", "Debug");
		    plugin.Logger("Line 1: " + line[0], "Debug");
		    plugin.Logger("Line 2: " + line[1], "Debug");
		    plugin.Logger("Line 3: " + line[2], "Debug");
		    plugin.Logger("Line 4: " + line[3], "Debug");
		    plugin.Logger("Sign is valid", "Debug");
		    if (!line[1].equalsIgnoreCase("AdminShop") && p.getName().length() < 16) {
			if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.create.own")) {
			    plugin.Logger("First line != null", "Debug");
			    event.setLine(0, "[xpShop]");
			    event.setLine(1, event.getPlayer().getName());
			    plugin.PlayerLogger(event.getPlayer(), "Successfully created xpShop!", "");
			    if (plugin.config.ConnectionofSafetoShop) {
				Block eventblock = event.getBlock();
				if (eventblock != null) {
				    Block[] b = plugin.ListenerShop.DrueberDrunter(eventblock);
				    Sign sign = plugin.ListenerShop.getCorrectsafeSign(b);
				    if (sign != null) {
					if (sign.getLine(0).equalsIgnoreCase("[xpShopSafe]")) {
					} else {
					    plugin.PlayerLogger(event.getPlayer(), plugin.config.safepleaseaddSafe, "Warning");
					}
				    } else {
					plugin.PlayerLogger(event.getPlayer(), plugin.config.safepleaseaddSafe, "Warning");
				    }
				}
			    }
			    MTLocation loc = MTLocation.getMTLocationFromLocation(event.getBlock().getLocation());
			    if (!MetricsHandler.Shop.containsKey(loc)) {
				MetricsHandler.Shop.put(loc, event.getPlayer().getName());
				plugin.Logger("Added Shop to list!", "Debug");
			    }
			} else {
			    plugin.Logger("Player " + p.getName() + " has no permission: xpShop.create.own", "Debug");
			    plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed!", "Error");
			    event.setCancelled(true);
			}
		    } else if (line[1].equalsIgnoreCase("AdminShop")) {
			if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.create.admin")) {
			    plugin.Logger("Player " + p.getName() + " has permission: xpShop.create.admin", "Debug");
			    event.setLine(0, "[xpShop]");
			    event.setLine(1, "AdminShop");
			    MTLocation loc = MTLocation.getMTLocationFromLocation(event.getBlock().getLocation());
			    if (!MetricsHandler.Shop.containsKey(loc)) {
				MetricsHandler.Shop.put(loc, event.getPlayer().getName());
				plugin.Logger("Added Shop to list!", "Debug");
			    }
			    plugin.PlayerLogger(event.getPlayer(), "Successfully created xpShop!", "");
			} else {
			    plugin.Logger("Player " + p.getName() + " has no permission: xpShop.create.admin", "Debug");
			    plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed!", "Error");
			    event.setCancelled(true);
			}
		    } else if (p.getName().length() >= 16) {
			plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed! Username too long!", "Error");
			event.setCancelled(true);
		    }
		} else {
		    plugin.Logger("Sign is not valid", "Debug");
		    plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed!", "Error");
		    event.setCancelled(true);
		}

	    } catch (Exception e) {
		e.printStackTrace();
		event.setCancelled(true);
		plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed!", "Error");
	    }
	} else {
	    plugin.Logger(plugin.Blacklistcode, "Debug");
	    plugin.blacklistLogger(p);
	    event.setCancelled(true);
	}
    }

    public void RechtsKlick(PlayerInteractEvent event, String[] line, Player p, Sign s) {
	xpShopSignRechts(event, line, p, s);
    }

    public void LinksKlick(PlayerInteractEvent event, String[] line, Player p, Sign s) {
	xpShopSignLinks(event, line, p, s);
    }

    public void xpShopSignLinks(PlayerInteractEvent event, String[] line, Player p, Sign s) {
	if (plugin.config.debug) {
	    plugin.Logger(" first line [xpShop] and leftklick!", "Debug");
	}
	if (!plugin.Blacklistcode.startsWith("1", 11)) {
	    plugin.Logger(" not blacklisted!", "Debug");
	    if (plugin.ListenerShop.blockIsValid(line, "Interact", p)) {
		plugin.Logger("LinksShop: ", "Debug");
		plugin.Logger("Line 1: " + line[0], "Debug");
		plugin.Logger("Line 2: " + line[1], "Debug");
		plugin.Logger("Line 3: " + line[2], "Debug");
		plugin.Logger("Line 4: " + line[3], "Debug");
		plugin.Logger(" Block is valid!", "Debug");
		Player player = event.getPlayer();
		if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.use")) {
		    plugin.Logger("Player: " + p.getName() + " has the permission: xpShop.use", "Debug");
		    if (line[1].equalsIgnoreCase("AdminShop")) {
			xpShopSignBuyAdmin(player, line, s);
		    } else {
			if (plugin.config.ConnectionofSafetoShop) {
			    Sign sign = plugin.ListenerShop.findSign(event.getClickedBlock());
			    if (sign != null) {
				plugin.Logger("sign != null!", "Debug");
				if (sign.getLine(0).equalsIgnoreCase("[xpShopSafe]")) {
				    plugin.Logger("Safe found!", "Debug");
				    xpShopSignBuyCon(player, line, s, sign);
				} else {
				    plugin.PlayerLogger(p, plugin.config.safenoSafeonShop, "Error");
				}
			    } else {
				plugin.PlayerLogger(p, plugin.config.safenoSafeonShop, "Error");
				plugin.Logger("Sign == null!", "Debug");
			    }
			} else {
			    xpShopSignBuy(player, line, s);
			}
		    }
		}
	    }
	} else {
	    plugin.blacklistLogger(p);
	    event.setCancelled(true);
	}
    }

    public void xpShopSignBuyCon(Player player, String[] line, Sign s, Sign sign) {
	String playername = player.getName();
	plugin.Logger("BuyShopCon: ", "Debug");
	plugin.Logger("Line 1: " + line[0], "Debug");
	plugin.Logger("Line 2: " + line[1], "Debug");
	plugin.Logger("Line 3: " + line[2], "Debug");
	plugin.Logger("Line 4: " + line[3], "Debug");
	if (!playername.equalsIgnoreCase(line[1])) {
	    Player empfaenger = null;
	    try {
		empfaenger = Tools.getmyOfflinePlayer(plugin, line, 1);
	    } catch (PlayerNotOnlineException e) {
		plugin.PlayerLogger(player, e.getMessage(), "Error");
		return;
	    } catch (PlayerWasNeverOnlineException e) {
		plugin.PlayerLogger(player, e.getMessage(), "Error");
		return;
	    }
	    if (empfaenger != null) {
		if (Integer.parseInt(sign.getLine(2)) >= Integer.parseInt(line[2])) {
		    if (plugin.ListenerShop.getPrice(s, player, true) > 0) {
			double price = plugin.ListenerShop.getPrice(s, player, true);
			try {
			    if ((plugin.moneyHandler.getBalance(player) - price) >= 0) {
				plugin.moneyHandler.substract(price, player);
				plugin.UpdateXP(player, (Integer.parseInt(s.getLine(2))), "Sign");
				plugin.PlayerLogger(player, String.format(plugin.config.Shopsuccessbuy, s.getLine(2), s.getLine(1), price), "");
				// plugin.UpdateXP(empfaenger,
				// -(Integer.parseInt(s.getLine(2))), "Sign");
				plugin.moneyHandler.addmoney(price, empfaenger);
				// empfaenger.saveData();
				int Erg = Integer.parseInt(sign.getLine(2)) - Integer.parseInt(line[2]);
				StringBuilder bui = new StringBuilder();
				bui.append(Erg);
				plugin.Logger("Erg: line2 = " + bui.toString(), "Debug");
				sign.setLine(2, bui.toString());
				sign.update();
				plugin.metricshandler.xpShopSignBuy++;
				plugin.PlayerLogger(empfaenger, String.format(plugin.config.Shopsuccesssellerbuy, s.getLine(2), player.getName(), price), "");
			    } else {
				plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughmoneyconsumer, "Error");
			    }
			} catch (NumberFormatException e) {
			    e.printStackTrace();
			} catch (IndexOutOfBoundsException e) {
			    e.printStackTrace();
			} catch (NoiConomyPluginFound e) {
			    plugin.PlayerLogger(player, e.getMessage(), "Error");
			}
		    } else {
			plugin.PlayerLogger(player, plugin.config.Shoperrorcantbuyhere, "Error");
		    }
		} else {
		    plugin.PlayerLogger(player, plugin.config.safenotenoughXPinShop, "Error");
		}
	    }
	} else {
	    plugin.PlayerLogger(player, "That is your Shop", "Error");
	}
    }

    public void xpShopSignBuyAdmin(Player player, String[] line, Sign s) {
	plugin.Logger("ShopAdmin: ", "Debug");
	plugin.Logger("Line 1: " + line[0], "Debug");
	plugin.Logger("Line 2: " + line[1], "Debug");
	plugin.Logger("Line 3: " + line[2], "Debug");
	plugin.Logger("Line 4: " + line[3], "Debug");
	double price = plugin.ListenerShop.getPrice(s, player, true);
	if (price > 0) {
	    try {
		if ((plugin.moneyHandler.getBalance(player) - price) >= 0) {
		    plugin.moneyHandler.substract(price, player);
		    plugin.UpdateXP(player, (Integer.parseInt(s.getLine(2))), "Sign");
		    plugin.metricshandler.xpShopSignBuy++;
		    plugin.PlayerLogger(player, String.format(plugin.config.Shopsuccessbuy, s.getLine(2), "Admin", price), "");
		} else {
		    plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughmoneyconsumer, "Error");
		}
	    } catch (NumberFormatException e) {
		e.printStackTrace();
	    } catch (IndexOutOfBoundsException e) {
		e.printStackTrace();
	    } catch (NoiConomyPluginFound e) {
		plugin.PlayerLogger(player, e.getMessage(), "Error");
	    }
	} else {
	    plugin.PlayerLogger(player, plugin.config.Shoperrorcantbuyhere, "Error");
	}
    }

    public void xpShopSignBuy(Player player, String[] line, Sign s) {
	String playername = player.getName();
	plugin.Logger("ShopBuy: ", "Debug");
	plugin.Logger("Line 1: " + line[0], "Debug");
	plugin.Logger("Line 2: " + line[1], "Debug");
	plugin.Logger("Line 3: " + line[2], "Debug");
	plugin.Logger("Line 4: " + line[3], "Debug");
	if (!playername.equalsIgnoreCase(line[1])) {
	    Player empfaenger = null;
	    try {
		empfaenger = Tools.getmyOfflinePlayer(plugin, line, 1);
	    } catch (PlayerNotOnlineException e) {
		plugin.PlayerLogger(player, e.getMessage(), "Error");
		return;
	    } catch (PlayerWasNeverOnlineException e) {
		plugin.PlayerLogger(player, e.getMessage(), "Error");
		return;
	    }
	    if (empfaenger != null) {
		if (empfaenger.getTotalExperience() >= Integer.parseInt(line[2])) {
		    if (plugin.ListenerShop.getPrice(s, player, true) > 0) {
			double price = plugin.ListenerShop.getPrice(s, player, true);
			try {
			    if ((plugin.moneyHandler.getBalance(player) - price) >= 0) {
				plugin.moneyHandler.substract(price, player);
				plugin.moneyHandler.addmoney(price, empfaenger);
				plugin.UpdateXP(player, (Integer.parseInt(s.getLine(2))), "Sign");
				plugin.PlayerLogger(player, String.format(plugin.config.Shopsuccessbuy, s.getLine(2), s.getLine(1), price), "");
				plugin.UpdateXP(empfaenger, -(Integer.parseInt(s.getLine(2))), "Sign");
				empfaenger.saveData();
				plugin.metricshandler.xpShopSignBuy++;
				plugin.PlayerLogger(empfaenger, String.format(plugin.config.Shopsuccesssellerbuy, s.getLine(2), playername, price), "");
			    } else {
				plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughmoneyconsumer, "Error");
			    }
			} catch (NumberFormatException e) {
			    e.printStackTrace();
			} catch (IndexOutOfBoundsException e) {
			    e.printStackTrace();
			} catch (NoiConomyPluginFound e) {
			    plugin.PlayerLogger(player, e.getMessage(), "Error");
			}
		    } else {
			plugin.PlayerLogger(player, plugin.config.Shoperrorcantbuyhere, "Error");
		    }
		} else {
		    plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughxpseller, "Error");
		}
	    }
	} else {
	    plugin.PlayerLogger(player, "That is your Shop", "Error");
	}
    }

    public void xpShopSignRechts(PlayerInteractEvent event, String[] line, Player p, Sign s) {
	if (!plugin.Blacklistcode.startsWith("1", 11)) {
	    Player player = p;
	    plugin.Logger("Shop rechts: ", "Debug");
	    plugin.Logger("Line 1: " + line[0], "Debug");
	    plugin.Logger("Line 2: " + line[1], "Debug");
	    plugin.Logger("Line 3: " + line[2], "Debug");
	    plugin.Logger("Line 4: " + line[3], "Debug");
	    if (plugin.ListenerShop.blockIsValid(line, "Interact", p)) {
		if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.use")) {
		    if (line[1].equalsIgnoreCase("AdminShop")) {
			xpShopSignSellAdmin(player, line, s);
		    } else {
			if (plugin.config.ConnectionofSafetoShop) {
			    Sign sign = plugin.ListenerShop.findSign(event.getClickedBlock());

			    if (sign != null) {
				plugin.Logger("Safe:!", "Debug");
				plugin.Logger("Block sign : Y: " + sign.getY() + " X: " + sign.getX() + "Z: " + sign.getZ(), "Debug");
				plugin.Logger("sign != null!", "Debug");
				if (sign.getLine(0).equalsIgnoreCase("[xpShopSafe]")) {
				    plugin.Logger("Safe found!", "Debug");
				    xpShopSignSellCon(player, line, s, sign);
				} else {
				    plugin.PlayerLogger(p, plugin.config.safenoSafeonShop, "Error");
				}
			    } else {
				plugin.PlayerLogger(p, plugin.config.safenoSafeonShop, "Error");
			    }
			} else {
			    xpShopSignSell(player, line, s);
			}
		    }
		}
	    }
	} else {
	    plugin.blacklistLogger(p);
	    event.setCancelled(true);
	}
    }

    public void xpShopSignSellCon(Player player, String[] line, Sign s, Sign sign) {
	String playername = player.getName();
	plugin.Logger("ShopSellCon: ", "Debug");
	plugin.Logger("Line 1: " + line[0], "Debug");
	plugin.Logger("Line 2: " + line[1], "Debug");
	plugin.Logger("Line 3: " + line[2], "Debug");
	plugin.Logger("Line 4: " + line[3], "Debug");
	if (!playername.equalsIgnoreCase(line[1])) {
	    Player empfaenger = null;
	    try {
		empfaenger = Tools.getmyOfflinePlayer(plugin, line, 1);
	    } catch (PlayerNotOnlineException e) {
		plugin.PlayerLogger(player, e.getMessage(), "Error");
		return;
	    } catch (PlayerWasNeverOnlineException e) {
		return;
	    }
	    if (empfaenger != null) {
		if (player.getTotalExperience() >= Integer.parseInt(line[2])) {
		    if (plugin.ListenerShop.getPrice(s, player, false) > 0) {
			double price = plugin.ListenerShop.getPrice(s, player, false);
			try {
			    if ((plugin.moneyHandler.getBalance(empfaenger) - price) >= 0) {
				plugin.moneyHandler.substract(price, empfaenger);
				// plugin.UpdateXP(empfaenger,
				// (Integer.parseInt(s.getLine(2))), "Sign");
				plugin.PlayerLogger(empfaenger, String.format(plugin.config.Shopsuccesssellerselled, s.getLine(2), player.getName(), price), "");
				plugin.moneyHandler.addmoney(price, player);
				plugin.UpdateXP(player, -(Integer.parseInt(s.getLine(2))), "Sign");
				int Erg = Integer.parseInt(sign.getLine(2)) + Integer.parseInt(line[2]);
				StringBuilder bui = new StringBuilder();
				bui.append(Erg);
				plugin.Logger("Erg: line2 = " + bui.toString(), "Debug");
				sign.setLine(2, bui.toString());
				sign.update();
				empfaenger.saveData();
				plugin.metricshandler.xpShopSignSell++;
				plugin.PlayerLogger(player, String.format(plugin.config.Shopsuccesssell, s.getLine(2), s.getLine(1), price), "");
			    } else {
				plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughmoneyconsumer, "Error");
			    }
			} catch (NumberFormatException e) {
			    e.printStackTrace();
			} catch (IndexOutOfBoundsException e) {
			    e.printStackTrace();
			} catch (NoiConomyPluginFound e) {
			    plugin.PlayerLogger(player, e.getMessage(), "Error");
			}
		    } else {
			plugin.PlayerLogger(player, plugin.config.Shoperrorcantsellhere, "Error");
		    }
		} else {
		    plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughxpconsumer, "Error");
		}
	    }
	} else {
	    plugin.PlayerLogger(player, "That is your Shop", "Error");
	}
    }

    public void xpShopSignSellAdmin(Player player, String[] line, Sign s) {
	plugin.Logger("AdminShopSell: ", "Debug");
	plugin.Logger("Line 1: " + line[0], "Debug");
	plugin.Logger("Line 2: " + line[1], "Debug");
	plugin.Logger("Line 3: " + line[2], "Debug");
	plugin.Logger("Line 4: " + line[3], "Debug");
	if (player.getTotalExperience() >= Integer.parseInt(line[2])) {
	    if (plugin.ListenerShop.getPrice(s, player, false) > 0) {
		double price = plugin.ListenerShop.getPrice(s, player, false);
		plugin.moneyHandler.addmoney(price, player);
		plugin.UpdateXP(player, -(Integer.parseInt(s.getLine(2))), "Sign");
		plugin.metricshandler.xpShopSignSell++;
		plugin.PlayerLogger(player, String.format(plugin.config.Shopsuccesssell, s.getLine(2), "Admin", price), "");
	    } else {
		plugin.PlayerLogger(player, plugin.config.Shoperrorcantsellhere, "Error");
	    }
	} else {
	    plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughxpconsumer, "Error");
	}
    }

    public void xpShopSignSell(Player player, String[] line, Sign s) {
	String playername = player.getName();
	plugin.Logger("ShopSell: ", "Debug");
	plugin.Logger("Line 1: " + line[0], "Debug");
	plugin.Logger("Line 2: " + line[1], "Debug");
	plugin.Logger("Line 3: " + line[2], "Debug");
	plugin.Logger("Line 4: " + line[3], "Debug");
	if (!playername.equalsIgnoreCase(line[1])) {
	    Player empfaenger;
	    try {
		empfaenger = Tools.getmyOfflinePlayer(plugin, line, 1);
	    } catch (PlayerNotOnlineException e) {
		plugin.PlayerLogger(player, e.getMessage(), "Error");
		return;
	    } catch (PlayerWasNeverOnlineException e) {
		plugin.PlayerLogger(player, e.getMessage(), "Error");
		return;
	    }
	    if (empfaenger != null) {
		if (player.getTotalExperience() >= Integer.parseInt(line[2])) {
		    if (plugin.ListenerShop.getPrice(s, player, false) > 0) {
			double price = plugin.ListenerShop.getPrice(s, player, false);
			try {
			    if ((plugin.moneyHandler.getBalance(empfaenger) - price) >= 0) {
				plugin.moneyHandler.substract(price, empfaenger);
				plugin.UpdateXP(empfaenger, (Integer.parseInt(s.getLine(2))), "Sign");
				plugin.PlayerLogger(empfaenger, String.format(plugin.config.Shopsuccesssellerselled, s.getLine(2), playername, price), "");
				plugin.moneyHandler.addmoney(price, player);
				plugin.UpdateXP(player, -(Integer.parseInt(s.getLine(2))), "Sign");
				empfaenger.saveData();
				plugin.metricshandler.xpShopSignSell++;
				plugin.PlayerLogger(player, String.format(plugin.config.Shopsuccesssell, s.getLine(2), s.getLine(1), price), "");
			    } else {
				plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughmoneyconsumer, "Error");
			    }
			} catch (NumberFormatException e) {
			    e.printStackTrace();
			} catch (IndexOutOfBoundsException e) {
			    e.printStackTrace();
			} catch (NoiConomyPluginFound e) {
			    plugin.PlayerLogger(player, e.getMessage(), "Error");
			}
		    } else {
			plugin.PlayerLogger(player, plugin.config.Shoperrorcantsellhere, "Error");
		    }
		} else {
		    plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughxpconsumer, "Error");
		}
	    }
	} else {
	    plugin.PlayerLogger(player, "That is your Shop", "Error");
	}
    }
}
