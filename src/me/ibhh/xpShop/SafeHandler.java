/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ibhh.xpShop;

import java.sql.SQLException;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 *
 * @author Simon
 */
public class SafeHandler {

    private xpShop plugin;

    public SafeHandler(xpShop pl) {
        plugin = pl;
    }

    public void Create(SignChangeEvent event) {
        Player p = event.getPlayer();
        String[] line = event.getLines();
        if (plugin.config.debug) {
            plugin.Logger("First Line [xpShopSafe]", "Debug");
        }
        if (!plugin.Blacklistcode.startsWith("1", 10)) {
            plugin.Logger(plugin.Blacklistcode, "Debug");
            if (plugin.ListenerShop.SafeIsValid(line)) {
                plugin.Logger("Safe is valid", "Debug");
                if (line[1].equalsIgnoreCase(p.getName()) && line[1].length() < 16) {
                    if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.safe.create")) {
                        plugin.PlayerLogger(event.getPlayer(), "Successfully created xpShopSafe!", "");
                        event.setLine(0, "[xpShopSafe]");
                        event.setLine(2, "0");
                    } else {
                        plugin.Logger("Player " + p.getName() + " has no permission: xpShop.safe.create", "Debug");
                        plugin.PlayerLogger(event.getPlayer(), "xpShopSafe creation failed!", "Error");
                        event.setCancelled(true);
                    }
                } else if (line[1].length() >= 16) {
                    plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed! Username too long!", "Error");
                    event.setCancelled(true);
                } else if (!(line[1].equalsIgnoreCase(p.getName()))) {
                    if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.safe.create")) {
                        plugin.Logger("First line != null", "Debug");
                        event.setLine(1, p.getName());
                        event.setLine(2, "0");
                        plugin.PlayerLogger(event.getPlayer(), "Successfully created xpShopSafe!", "");
                    } else {
                        plugin.Logger("Player " + p.getName() + " has no permission: xpShop.create", "Debug");
                        plugin.PlayerLogger(event.getPlayer(), "xpShopSafe creation failed!", "Error");
                        event.setCancelled(true);
                    }
                }
            } else {
                plugin.Logger("Sign is not valid", "Debug");
                plugin.PlayerLogger(event.getPlayer(), "xpShopSafe creation failed!", "Error");
                event.setCancelled(true);
            }
        } else {
            plugin.Logger(plugin.Blacklistcode, "Debug");
            plugin.blacklistLogger(p);
            event.setCancelled(true);
        }
    }

    public void RechtsKlick(PlayerInteractEvent event, String[] line, Player p, Sign s) {
        xpShopSafeSignRechts(event, line, p, s);
    }

    public void LinksKlick(PlayerInteractEvent event, String[] line, Player p, Sign s) {
        xpShopSafeSignLinks(event, line, p, s);
    }

    public void xpShopSafeSignGetDB(String[] line, Player p, Sign s) {
        String playername = p.getName();
        try {
            if (plugin.SQL.isindb(line[1])) {
                int XPPlayer = plugin.SQL.getXP(playername);
                if (Integer.parseInt(line[3]) <= Integer.parseInt(line[2])) {
                    plugin.UpdateXP(p, Integer.parseInt(line[3]), "Safe");
                    plugin.SQL.UpdateXP(playername, XPPlayer + Integer.parseInt(line[3]));
                    int Erg = Integer.parseInt(line[2]) - Integer.parseInt(line[3]);
                    StringBuilder bui = new StringBuilder();
                    bui.append(Erg);
                    plugin.Logger("Erg: line2 = " + bui.toString(), "Debug");
                    s.setLine(2, bui.toString());
                    s.update();
                    plugin.PlayerLogger(p, String.format(plugin.config.safedestore, Integer.parseInt(s.getLine(3))), "");
                } else {
                    plugin.PlayerLogger(p, plugin.config.safenotenoughinsafe, "Error");
                }
            } else {
                plugin.PlayerLogger(p, line[1] + " " + plugin.config.playerwasntonline, "Error");
            }
        } catch (SQLException e) {
            plugin.PlayerLogger(p, line[1] + " " + plugin.config.playerwasntonline, "Error");
        }
    }

    public void xpShopSafeSignGet(String[] line, Player p, Sign s) {
        if (Integer.parseInt(line[3]) <= Integer.parseInt(line[2])) {
            plugin.UpdateXP(p, Integer.parseInt(line[3]), "Safe");
            p.saveData();
            int Erg = Integer.parseInt(line[2]) - Integer.parseInt(line[3]);
            StringBuilder bui = new StringBuilder();
            bui.append(Erg);
            plugin.Logger("Erg: line2 = " + bui.toString(), "Debug");
            s.setLine(2, bui.toString());
            s.update();
            plugin.PlayerLogger(p, String.format(plugin.config.safedestore, Integer.parseInt(s.getLine(3))), "");
        } else {
            plugin.PlayerLogger(p, plugin.config.safenotenoughinsafe, "Error");
        }
    }

    public void xpShopSafeSignLinks(PlayerInteractEvent event, String[] line, Player p, Sign s) {
        plugin.Logger(" first line [xpShopSafe] and leftklick!", "Debug");
        String playername = p.getName();
        Player player = event.getPlayer();
        if (!plugin.Blacklistcode.startsWith("1", 11)) {
            plugin.Logger(" not blacklisted!", "Debug");
            if (plugin.ListenerShop.SafeIsValid(line)) {
                plugin.Logger(" Safe is valid!", "Debug");
                if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.safe.use")) {
                    plugin.Logger("Player: " + p.getName() + " has the permission: xpShop.safe.use", "Debug");
                    if (line[1].equalsIgnoreCase(playername)) {
                        if (plugin.config.usedbtomanageXP) {
                            xpShopSafeSignGetDB(line, p, s);
                        } else {
                            xpShopSafeSignGet(line, p, s);
                        }
                    } else {
                        plugin.PlayerLogger(p, plugin.config.safenotyoursafe, "");
                    }
                }
            }

        }
    }

    public void xpShopSafeSignStoreDB(String[] line, Player p, Sign s) {
        String playername = p.getName();
        try {
            if (plugin.SQL.isindb(line[1])) {
                int XPPlayer = plugin.SQL.getXP(playername);
                if (XPPlayer >= Integer.parseInt(line[3]) && line[2] != null) {
                    plugin.UpdateXP(p, -(Integer.parseInt(line[3])), "Safe");
                    plugin.SQL.UpdateXP(playername, XPPlayer - Integer.parseInt(line[3]));
                    int Erg = Integer.parseInt(line[2]) + Integer.parseInt(line[3]);
                    StringBuilder bui = new StringBuilder();
                    bui.append(Erg);
                    plugin.Logger("Erg: line2 = " + bui.toString(), "Debug");
                    s.setLine(2, bui.toString());
                    s.update();
                    plugin.PlayerLogger(p, String.format(plugin.config.safestore, Integer.parseInt(s.getLine(3))), "");
                } else {
                    plugin.PlayerLogger(p, String.format(plugin.config.safenotenoughxptostore, plugin.getTOTALXP(p)), "Error");
                }
            } else {
                plugin.PlayerLogger(p, line[1] + " " + plugin.config.playerwasntonline, "Error");
            }

        } catch (SQLException e) {
            plugin.PlayerLogger(p, line[1] + " " + plugin.config.playerwasntonline, "Error");
        }
    }

    public void xpShopSafeSignStore(String[] line, Player p, Sign s) {
        if (plugin.getTOTALXP(p) >= Integer.parseInt(line[3]) && line[2] != null) {
            plugin.UpdateXP(p, -(Integer.parseInt(s.getLine(3))), "Safe");
            p.saveData();
            int Erg = Integer.parseInt(line[2]) + Integer.parseInt(line[3]);
            StringBuilder bui = new StringBuilder();
            bui.append(Erg);
            plugin.Logger("Erg: line2 = " + bui.toString(), "Debug");
            s.setLine(2, bui.toString());
            s.update();
            plugin.PlayerLogger(p, String.format(plugin.config.safestore, Integer.parseInt(s.getLine(3))), "");
        } else {
            plugin.PlayerLogger(p, String.format(plugin.config.safenotenoughxptostore, plugin.getTOTALXP(p)), "Error");
        }
    }

    public void xpShopSafeSignRechts(PlayerInteractEvent event, String[] line, Player p, Sign s) {
        if (!plugin.Blacklistcode.startsWith("1", 11)) {
            String playername = p.getName();
            if (plugin.ListenerShop.SafeIsValid(line)) {
                if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.safe.use")) {
                    if (line[1].equalsIgnoreCase(playername)) {
                        if (plugin.config.usedbtomanageXP) {
                            xpShopSafeSignStoreDB(line, p, s);
                        } else {
                            xpShopSafeSignStore(line, p, s);
                        }
                    } else {
                        plugin.PlayerLogger(p, plugin.config.safenotyoursafe, "");
                    }

                }
            }
        }
    }
}
