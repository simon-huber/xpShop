/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ibhh.xpShop;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 *
 * @author Simon
 */
public class SignConSafeHandler {

    private xpShop plugin;
    private SignHandler signHandler;

    public SignConSafeHandler(xpShop pl) {
        plugin = pl;
        signHandler = new SignHandler(pl);
    }

    public void CreatexpShop(SignChangeEvent event) {
        Player p = event.getPlayer();
        String[] line = event.getLines();
        plugin.Logger("First Line [xpShop]", "Debug");
        if (!plugin.Blacklistcode.startsWith("1", 10)) {
            plugin.Logger(plugin.Blacklistcode, "Debug");
            try {
                plugin.Logger("Createing Shop: ", "Debug");
                plugin.Logger("Line 1: "+ line[0], "Debug");
                plugin.Logger("Line 2: "+ line[1], "Debug");
                plugin.Logger("Line 3: "+ line[2], "Debug");
                plugin.Logger("Line 4: "+ line[3], "Debug");
                if (plugin.ListenerShop.blockIsValid(line, "create", p)) {
                    plugin.Logger("Sign is valid", "Debug");
                    if (!line[1].equalsIgnoreCase("AdminShop") && p.getName().length() < 16) {
                        if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.create.own")) {
                            plugin.Logger("First line != null", "Debug");
                            plugin.PlayerLogger(event.getPlayer(), "Successfully created xpShop!", "");
                            Block eventblock = event.getBlock();
                            if (eventblock != null) {
                                Block[] b = plugin.ListenerShop.DrueberDrunter(eventblock);
                                Sign sign = plugin.ListenerShop.getCorrectsafeSign(b);
                                if (sign != null) {
                                    if (sign.getLine(0).equalsIgnoreCase("[xpShopSafe]")) {
                                    } else {
                                        plugin.PlayerLogger(event.getPlayer(), plugin.config.safecanaddSafe, "Warning");
                                    }
                                } else {
                                    plugin.PlayerLogger(event.getPlayer(), plugin.config.safecanaddSafe, "Warning");
                                }
                            }
                            event.setLine(0, "[xpShop]");
                            event.setLine(1, event.getPlayer().getName());
                            MTLocation loc = MTLocation.getMTLocationFromLocation(event.getBlock().getLocation());
                            if (!plugin.metricshandler.Shop.containsKey(loc)) {
                                plugin.metricshandler.Shop.put(loc, event.getPlayer().getName());
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
                            MTLocation loc = MTLocation.getMTLocationFromLocation(event.getBlock().getLocation());
                            if (!plugin.metricshandler.Shop.containsKey(loc)) {
                                plugin.metricshandler.Shop.put(loc, "AdminShop");
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

    public void xpShopSignRechts(PlayerInteractEvent event, String[] line, Player p, Sign s) {
        if (!plugin.Blacklistcode.startsWith("1", 11)) {
            Player player = p;
            if (plugin.ListenerShop.blockIsValid(line, "Interact", p)) {
                plugin.Logger("RechtsShop: ", "Debug");
                plugin.Logger("Line 1: "+ line[0], "Debug");
                plugin.Logger("Line 2: "+ line[1], "Debug");
                plugin.Logger("Line 3: "+ line[2], "Debug");
                plugin.Logger("Line 4: "+ line[3], "Debug");
                if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.use")) {
                    if (line[1].equalsIgnoreCase("AdminShop")) {
                        signHandler.xpShopSignSellAdmin(player, line, s);
                    } else {
                        Sign sign = plugin.ListenerShop.findSign(event.getClickedBlock());
                        if (sign != null) {
                            plugin.Logger("Safe:!", "Debug");
                            plugin.Logger("Block sign : Y: " + sign.getY() + " X: " + sign.getX() + "Z: " + sign.getZ(), "Debug");
                            plugin.Logger("sign != null!", "Debug");
                            if (sign.getLine(0).equalsIgnoreCase("[xpShopSafe]")) {
                                plugin.Logger("Safe found!", "Debug");
                                if (plugin.config.usedbtomanageXP) {
                                    signHandler.xpShopSignSellConDB(player, line, s, sign);
                                } else {
                                    signHandler.xpShopSignSellCon(player, line, s, sign);
                                }

                            } else {
                                plugin.PlayerLogger(p, plugin.config.safenoSafeonShop, "Error");
                            }
                        } else {
                            if (plugin.config.usedbtomanageXP) {
                                signHandler.xpShopSignSellDB(player, line, s);
                            } else {
                                signHandler.xpShopSignSell(player, line, s);
                            }
                        }
                    }
                }
            }
        } else {
            plugin.blacklistLogger(p);
            event.setCancelled(true);
        }
    }

    public void xpShopSignLinks(PlayerInteractEvent event, String[] line, Player p, Sign s) {
        if (plugin.config.debug) {
            plugin.Logger(" first line [xpShop] and leftklick!", "Debug");
        }
        if (!plugin.Blacklistcode.startsWith("1", 11)) {
            plugin.Logger(" not blacklisted!", "Debug");
            if (plugin.ListenerShop.blockIsValid(line, "Interact", p)) {
                plugin.Logger("Shop Links:: ", "Debug");
                plugin.Logger("Line 1: "+ line[0], "Debug");
                plugin.Logger("Line 2: "+ line[1], "Debug");
                plugin.Logger("Line 3: "+ line[2], "Debug");
                plugin.Logger("Line 4: "+ line[3], "Debug");
                plugin.Logger(" Block is valid!", "Debug");
                Player player = event.getPlayer();
                if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.use")) {
                    plugin.Logger("Player: " + p.getName() + " has the permission: xpShop.use", "Debug");
                    if (line[1].equalsIgnoreCase("AdminShop")) {
                        signHandler.xpShopSignBuyAdmin(player, line, s);
                    } else {
                        Sign sign = plugin.ListenerShop.findSign(event.getClickedBlock());
                        if (sign != null) {
                            plugin.Logger("sign != null!", "Debug");
                            if (sign.getLine(0).equalsIgnoreCase("[xpShopSafe]")) {
                                plugin.Logger("Safe found!", "Debug");
                                if (plugin.config.usedbtomanageXP) {
                                    signHandler.xpShopSignBuyConDB(player, line, s, sign);
                                } else {
                                    signHandler.xpShopSignBuyCon(player, line, s, sign);
                                }
                            } else {
                                plugin.PlayerLogger(p, plugin.config.safenoSafeonShop, "Error");
                            }
                        } else {
                            if (plugin.config.usedbtomanageXP) {
                                signHandler.xpShopSignBuyDB(player, line, s);
                            } else {
                                signHandler.xpShopSignBuy(player, line, s);
                            }
                        }
                    }
                }
            }
        } else {
            plugin.blacklistLogger(p);
            event.setCancelled(true);
        }
    }
}
