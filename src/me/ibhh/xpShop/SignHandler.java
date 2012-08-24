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
                            event.setLine(1, "AdminShop");
                            MTLocation loc = MTLocation.getMTLocationFromLocation(event.getBlock().getLocation());
                            if (!plugin.metricshandler.Shop.containsKey(loc)) {
                                plugin.metricshandler.Shop.put(loc, event.getPlayer().getName());
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
                String playername = p.getName();
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
                                    if (plugin.config.usedbtomanageXP) {
                                        xpShopSignBuyConDB(player, line, s, sign);
                                    } else {
                                        xpShopSignBuyCon(player, line, s, sign);
                                    }
                                } else {
                                    plugin.PlayerLogger(p, plugin.config.safenoSafeonShop, "Error");
                                }
                            } else {
                                plugin.PlayerLogger(p, plugin.config.safenoSafeonShop, "Error");
                                plugin.Logger("Sign == null!", "Debug");
                            }
                        } else if (plugin.config.usedbtomanageXP) {
                            xpShopSignBuyDB(player, line, s);
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
            Player empfaenger = plugin.getmyOfflinePlayer(line, 1);
            if (empfaenger != null) {
                if (plugin.config.getPlayerConfig(empfaenger, player)) {
                    if (Integer.parseInt(sign.getLine(2)) >= Integer.parseInt(line[2])) {
                        if (plugin.ListenerShop.getPrice(s, player, true) > 0) {
                            double price = plugin.ListenerShop.getPrice(s, player, true);
                            if ((plugin.MoneyHandler.getBalance(player) - price) >= 0) {
                                plugin.MoneyHandler.substract(price, player);
                                plugin.UpdateXP(player, (Integer.parseInt(s.getLine(2))), "Sign");
                                plugin.PlayerLogger(player, String.format(plugin.config.Shopsuccessbuy, s.getLine(2), s.getLine(1), price), "");
//                                plugin.UpdateXP(empfaenger, -(Integer.parseInt(s.getLine(2))), "Sign");
                                plugin.MoneyHandler.addmoney(price, empfaenger);
//                                empfaenger.saveData();
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
                        } else {
                            plugin.PlayerLogger(player, plugin.config.Shoperrorcantbuyhere, "Error");
                        }
                    } else {
                        plugin.PlayerLogger(player, plugin.config.safenotenoughXPinShop, "Error");
                    }
                }
            } else {
                plugin.PlayerLogger(player, line[1] + " " + plugin.config.playerwasntonline, "Error");
            }
        } else {
            plugin.PlayerLogger(player, "That is your Shop", "Error");
        }
    }

    public void xpShopSignBuyConDB(Player player, String[] line, Sign s, Sign sign) {
        String playername = player.getName();
        try {
            plugin.Logger("ShopBuyConDB: ", "Debug");
            plugin.Logger("Line 1: " + line[0], "Debug");
            plugin.Logger("Line 2: " + line[1], "Debug");
            plugin.Logger("Line 3: " + line[2], "Debug");
            plugin.Logger("Line 4: " + line[3], "Debug");
            if (!playername.equalsIgnoreCase(line[1])) {
                if (plugin.SQL.isindb(line[1])) {
                    int XPPlayer = plugin.SQL.getXP(playername);
                    double xptemp = player.getTotalExperience();
                    if (xptemp != XPPlayer) {
                        plugin.SQL.UpdateXP(playername, (int) xptemp);
                    }

                    if (Integer.parseInt(sign.getLine(2)) >= Integer.parseInt(line[2])) {
                        if (plugin.ListenerShop.getPrice(s, player, true) > 0) {
                            double price = plugin.ListenerShop.getPrice(s, player, true);
                            if ((plugin.MoneyHandler.getBalance(player) - price) >= 0) {
                                plugin.MoneyHandler.substract(price, player);
                                plugin.MoneyHandler.addmoney(price, line[1]);
                                plugin.UpdateXP(player, Integer.parseInt(line[2]), "Sign");
                                plugin.SQL.UpdateXP(playername, XPPlayer + Integer.parseInt(line[2]));
                                plugin.PlayerLogger(player, String.format(plugin.config.Shopsuccessbuy, s.getLine(2), s.getLine(1), price), "");
                                int Erg = Integer.parseInt(sign.getLine(2)) - Integer.parseInt(line[2]);
                                StringBuilder bui = new StringBuilder();
                                bui.append(Erg);
                                plugin.Logger("Erg: line2 = " + bui.toString(), "Debug");
                                sign.setLine(2, bui.toString());
                                sign.update();
                                plugin.metricshandler.xpShopSignBuy++;
                            } else {
                                plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughmoneyconsumer, "Error");
                            }
                        } else {
                            plugin.PlayerLogger(player, plugin.config.Shoperrorcantbuyhere, "Error");
                        }
                    } else {
                        plugin.PlayerLogger(player, plugin.config.safenotenoughXPinShop, "Error");
                    }
                } else {
                    plugin.PlayerLogger(player, line[1] + " " + plugin.config.playerwasntonline, "Error");
                }
            } else {
                plugin.PlayerLogger(player, "That is your Shop", "Error");
            }
        } catch (Exception e) {
            plugin.PlayerLogger(player, line[1] + " " + plugin.config.playerwasntonline, "Error");
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
            if ((plugin.MoneyHandler.getBalance(player) - price) >= 0) {
                plugin.MoneyHandler.substract(price, player);
                if (plugin.config.usedbtomanageXP) {
                    plugin.SQL.UpdateXP(player.getName(), (int) (Integer.parseInt(line[2]) + player.getTotalExperience()));
                }
                plugin.UpdateXP(player, (Integer.parseInt(s.getLine(2))), "Sign");
                plugin.metricshandler.xpShopSignBuy++;
                plugin.PlayerLogger(player, String.format(plugin.config.Shopsuccessbuy, s.getLine(2), "Admin", price), "");
            } else {
                plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughmoneyconsumer, "Error");
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
            Player empfaenger = plugin.getmyOfflinePlayer(line, 1);
            if (empfaenger != null) {
                if (plugin.config.getPlayerConfig(empfaenger, player)) {
                    if (empfaenger.getTotalExperience() >= Integer.parseInt(line[2])) {
                        if (plugin.ListenerShop.getPrice(s, player, true) > 0) {
                            double price = plugin.ListenerShop.getPrice(s, player, true);
                            if ((plugin.MoneyHandler.getBalance(player) - price) >= 0) {
                                plugin.MoneyHandler.substract(price, player);
                                plugin.MoneyHandler.addmoney(price, empfaenger);
                                plugin.UpdateXP(player, (Integer.parseInt(s.getLine(2))), "Sign");
                                plugin.PlayerLogger(player, String.format(plugin.config.Shopsuccessbuy, s.getLine(2), s.getLine(1), price), "");
                                plugin.UpdateXP(empfaenger, -(Integer.parseInt(s.getLine(2))), "Sign");
                                empfaenger.saveData();
                                plugin.metricshandler.xpShopSignBuy++;
                                plugin.PlayerLogger(empfaenger, String.format(plugin.config.Shopsuccesssellerbuy, s.getLine(2), playername, price), "");
                            } else {
                                plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughmoneyconsumer, "Error");
                            }
                        } else {
                            plugin.PlayerLogger(player, plugin.config.Shoperrorcantbuyhere, "Error");
                        }
                    } else {
                        plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughxpseller, "Error");
                    }
                }
            } else {
                plugin.PlayerLogger(player, line[1] + " " + plugin.config.playerwasntonline, "Error");
            }
        } else {
            plugin.PlayerLogger(player, "That is your Shop", "Error");
        }
    }

    public void xpShopSignBuyDB(Player player, String[] line, Sign s) {
        String playername = player.getName();
        plugin.Logger("ShopBuyDB: ", "Debug");
        plugin.Logger("Line 1: " + line[0], "Debug");
        plugin.Logger("Line 2: " + line[1], "Debug");
        plugin.Logger("Line 3: " + line[2], "Debug");
        plugin.Logger("Line 4: " + line[3], "Debug");
        try {
            if (!playername.equalsIgnoreCase(line[1])) {
                if (plugin.SQL.isindb(line[1])) {
                    int XPPlayer = plugin.SQL.getXP(playername);
                    double xptemp = player.getTotalExperience();
                    if (xptemp != XPPlayer) {
                        plugin.Logger("Updating XP before buy on Sign because some differences!", "");
                        plugin.SQL.UpdateXP(playername, (int) xptemp);
                    }
                    int EmpfXP = plugin.SQL.getXP(line[1]);
                    if (EmpfXP >= Integer.parseInt(line[2])) {
                        if (plugin.ListenerShop.getPrice(s, player, true) > 0) {
                            double price = plugin.ListenerShop.getPrice(s, player, true);
                            if ((plugin.MoneyHandler.getBalance(player) - price) >= 0) {
                                plugin.MoneyHandler.substract(price, player);
                                plugin.MoneyHandler.addmoney(price, line[1]);
                                plugin.UpdateXP(player, Integer.parseInt(line[2]), "Sign");
                                plugin.SQL.UpdateXP(playername, XPPlayer + Integer.parseInt(line[2]));
                                plugin.SQL.UpdateXP(line[1], plugin.SQL.getXP(line[1]) - Integer.parseInt(line[2]));
                                plugin.PlayerLogger(player, String.format(plugin.config.Shopsuccessbuy, s.getLine(2), s.getLine(1), price), "");
                                if (plugin.getServer().getPlayer(line[1]) != null) {
                                    Player Empfaenger = plugin.getServer().getPlayer(line[1]);
                                    Empfaenger.setLevel(0);
                                    Empfaenger.setExp(0);
                                    plugin.UpdateXP(Empfaenger, plugin.SQL.getXP(Empfaenger.getName()), "Sign");
                                    plugin.metricshandler.xpShopSignBuy++;
                                    plugin.PlayerLogger(Empfaenger, String.format(plugin.config.Shopsuccesssellerbuy, s.getLine(2), playername, price), "");
                                }
                            } else {
                                plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughmoneyconsumer, "Error");
                            }
                        } else {
                            plugin.PlayerLogger(player, plugin.config.Shoperrorcantbuyhere, "Error");
                        }
                    } else {
                        plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughxpseller, "Error");
                    }
                } else {
                    plugin.PlayerLogger(player, line[1] + " " + plugin.config.playerwasntonline, "Error");
                }
            } else {
                plugin.PlayerLogger(player, "That is your Shop", "Error");
            }
        } catch (Exception e) {
            plugin.PlayerLogger(player, line[1] + " " + plugin.config.playerwasntonline, "Error");
        }
    }

    public void xpShopSignRechts(PlayerInteractEvent event, String[] line, Player p, Sign s) {
        if (!plugin.Blacklistcode.startsWith("1", 11)) {
            Player player = p;
            String playername = p.getName();
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
                                    if (plugin.config.usedbtomanageXP) {
                                        xpShopSignSellConDB(player, line, s, sign);
                                    } else {
                                        xpShopSignSellCon(player, line, s, sign);
                                    }

                                } else {
                                    plugin.PlayerLogger(p, plugin.config.safenoSafeonShop, "Error");
                                }
                            } else {
                                plugin.PlayerLogger(p, plugin.config.safenoSafeonShop, "Error");
                            }
                        } else if (plugin.config.usedbtomanageXP) {
                            xpShopSignSellDB(player, line, s);
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
            Player empfaenger = plugin.getmyOfflinePlayer(line, 1);
            if (empfaenger != null) {
                if (plugin.config.getPlayerConfig(empfaenger, player)) {
                    if (player.getTotalExperience() >= Integer.parseInt(line[2])) {
                        if (plugin.ListenerShop.getPrice(s, player, false) > 0) {
                            double price = plugin.ListenerShop.getPrice(s, player, false);
                            if ((plugin.MoneyHandler.getBalance(empfaenger) - price) >= 0) {
                                plugin.MoneyHandler.substract(price, empfaenger);
//                                plugin.UpdateXP(empfaenger, (Integer.parseInt(s.getLine(2))), "Sign");
                                plugin.PlayerLogger(empfaenger, String.format(plugin.config.Shopsuccesssellerselled, s.getLine(2), player.getName(), price), "");
                                plugin.MoneyHandler.addmoney(price, player);
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
                        } else {
                            plugin.PlayerLogger(player, plugin.config.Shoperrorcantsellhere, "Error");
                        }
                    } else {
                        plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughxpconsumer, "Error");
                    }
                }
            } else {
                plugin.PlayerLogger(player, line[1] + " " + plugin.config.playerwasntonline, "Error");
            }
        } else {
            plugin.PlayerLogger(player, "That is your Shop", "Error");
        }
    }

    public void xpShopSignSellConDB(Player player, String[] line, Sign s, Sign sign) {
        String playername = player.getName();
        plugin.Logger("ShopSellConDB: ", "Debug");
        plugin.Logger("Line 1: " + line[0], "Debug");
        plugin.Logger("Line 2: " + line[1], "Debug");
        plugin.Logger("Line 3: " + line[2], "Debug");
        plugin.Logger("Line 4: " + line[3], "Debug");
        try {
            if (!playername.equalsIgnoreCase(line[1])) {
                int XPPlayer = plugin.SQL.getXP(playername);
                if (plugin.SQL.getXP(playername) >= Integer.parseInt(line[2])) {
                    if (plugin.ListenerShop.getPrice(s, player, false) > 0) {
                        double price = plugin.ListenerShop.getPrice(s, player, false);
                        if ((plugin.MoneyHandler.getBalance(line[1]) - price) >= 0) {
                            plugin.MoneyHandler.substract(price, line[1]);
                            plugin.MoneyHandler.addmoney(price, player);
                            plugin.UpdateXP(player, -(Integer.parseInt(line[2])), "Sign");
                            plugin.SQL.UpdateXP(playername, XPPlayer - Integer.parseInt(line[2]));
                            int Erg = Integer.parseInt(sign.getLine(2)) + Integer.parseInt(line[2]);
                            StringBuilder bui = new StringBuilder();
                            bui.append(Erg);
                            plugin.Logger("Erg: line2 = " + bui.toString(), "Debug");
                            sign.setLine(2, bui.toString());
                            sign.update();
                            plugin.metricshandler.xpShopSignSell++;
                            plugin.PlayerLogger(player, String.format(plugin.config.Shopsuccesssell, line[2], line[1], price), "");
                            Player empfaenger = plugin.getServer().getPlayer(line[1]);
                            if (empfaenger != null) {

                                plugin.PlayerLogger(empfaenger, String.format(plugin.config.Shopsuccesssellerselled, s.getLine(2), playername, price), "");
                            }
                        } else {
                            plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughmoneyconsumer, "Error");
                        }
                    } else {
                        plugin.PlayerLogger(player, plugin.config.Shoperrorcantsellhere, "Error");
                    }
                } else {
                    plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughxpconsumer, "Error");
                }
            } else {
                plugin.PlayerLogger(player, "That is your Shop", "Error");
            }
        } catch (Exception e) {
            plugin.PlayerLogger(player, line[1] + " " + plugin.config.playerwasntonline, "Error");
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
                plugin.MoneyHandler.addmoney(price, player);
                if (plugin.config.usedbtomanageXP) {
                    plugin.SQL.UpdateXP(player.getName(), (int) (player.getTotalExperience() - Integer.parseInt(line[2])));
                }
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
            Player empfaenger = plugin.getmyOfflinePlayer(line, 1);
            if (empfaenger != null) {
                if (plugin.config.getPlayerConfig(empfaenger, player)) {
                    if (player.getTotalExperience() >= Integer.parseInt(line[2])) {
                        if (plugin.ListenerShop.getPrice(s, player, false) > 0) {
                            double price = plugin.ListenerShop.getPrice(s, player, false);
                            if ((plugin.MoneyHandler.getBalance(empfaenger) - price) >= 0) {
                                plugin.MoneyHandler.substract(price, empfaenger);
                                plugin.UpdateXP(empfaenger, (Integer.parseInt(s.getLine(2))), "Sign");
                                plugin.PlayerLogger(empfaenger, String.format(plugin.config.Shopsuccesssellerselled, s.getLine(2), playername, price), "");
                                plugin.MoneyHandler.addmoney(price, player);
                                plugin.UpdateXP(player, -(Integer.parseInt(s.getLine(2))), "Sign");
                                empfaenger.saveData();
                                plugin.metricshandler.xpShopSignSell++;
                                plugin.PlayerLogger(player, String.format(plugin.config.Shopsuccesssell, s.getLine(2), s.getLine(1), price), "");
                            } else {
                                plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughmoneyconsumer, "Error");
                            }
                        } else {
                            plugin.PlayerLogger(player, plugin.config.Shoperrorcantsellhere, "Error");
                        }
                    } else {
                        plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughxpconsumer, "Error");
                    }
                }
            } else {
                plugin.PlayerLogger(player, line[1] + " " + plugin.config.playerwasntonline, "Error");
            }
        } else {
            plugin.PlayerLogger(player, "That is your Shop", "Error");
        }
    }

    public void xpShopSignSellDB(Player player, String[] line, Sign s) {
        String playername = player.getName();
        plugin.Logger("ShopSellDB: ", "Debug");
        plugin.Logger("Line 1: " + line[0], "Debug");
        plugin.Logger("Line 2: " + line[1], "Debug");
        plugin.Logger("Line 3: " + line[2], "Debug");
        plugin.Logger("Line 4: " + line[3], "Debug");
        try {
            if (!playername.equalsIgnoreCase(line[1])) {
                if (plugin.SQL.isindb(line[1])) {
                    int XPPlayer = plugin.SQL.getXP(playername);
                    if (plugin.SQL.getXP(playername) >= Integer.parseInt(line[2])) {
                        if (plugin.ListenerShop.getPrice(s, player, false) > 0) {
                            double price = plugin.ListenerShop.getPrice(s, player, false);
                            if ((plugin.MoneyHandler.getBalance(line[1]) - price) >= 0) {
                                plugin.MoneyHandler.substract(price, line[1]);
                                plugin.MoneyHandler.addmoney(price, player);
                                plugin.UpdateXP(player, -(Integer.parseInt(line[2])), "Sign");
                                plugin.SQL.UpdateXP(playername, XPPlayer - Integer.parseInt(line[2]));
                                plugin.SQL.UpdateXP(line[1], plugin.SQL.getXP(line[1]) + Integer.parseInt(line[2]));
                                plugin.PlayerLogger(player, String.format(plugin.config.Shopsuccesssell, line[2], line[1], price), "");
                                Player empfaenger = plugin.getServer().getPlayer(line[1]);
                                if (empfaenger != null) {
                                    plugin.UpdateXP(empfaenger, (Integer.parseInt(line[2])), "Sign");
                                    plugin.PlayerLogger(empfaenger, String.format(plugin.config.Shopsuccesssellerselled, s.getLine(2), playername, price), "");
                                }
                                plugin.metricshandler.xpShopSignSell++;
                            } else {
                                plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughmoneyconsumer, "Error");
                            }
                        } else {
                            plugin.PlayerLogger(player, plugin.config.Shoperrorcantsellhere, "Error");
                        }
                    } else {
                        plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughxpconsumer, "Error");
                    }
                } else {
                    plugin.PlayerLogger(player, line[1] + " " + plugin.config.playerwasntonline, "Error");
                }
            } else {
                plugin.PlayerLogger(player, "That is your Shop", "Error");
            }
        } catch (Exception e) {
            plugin.PlayerLogger(player, line[1] + " " + plugin.config.playerwasntonline, "Error");
        }
    }
}
