/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ibhh.xpShop;

import java.util.HashMap;
import org.bukkit.entity.Player;

/**
 *
 * @author Simon
 */
public class TeleportManager {

    private HashMap<String, Player> requested = new HashMap<String, Player>();
    private HashMap<String, String> requestedname = new HashMap<String, String>();
    private HashMap<String, Boolean> execname = new HashMap<String, Boolean>();
    private HashMap<String, Player> exec = new HashMap<String, Player>();
    private xpShop plugin;

    public TeleportManager(xpShop pl) {
        plugin = pl;
    }

    public void registerTeleport(final Player destinator, final boolean to, final Player executer) {
        if (exec.containsKey(executer.getName())) {
            plugin.PlayerLogger(executer, plugin.getConfig().getString("teleport.teleportrequest1." + plugin.config.language), "Error");
        } else {
            requested.put(destinator.getName(), executer);
            requestedname.put(destinator.getName(), executer.getName());
            execname.put(executer.getName(), to);
            exec.put(executer.getName(), executer);
            int xpneeded = (int) (plugin.getEntfernung(executer.getLocation(), destinator.getLocation()) * plugin.getConfig().getDouble("teleport.xpperblock"));
            int entfernung = plugin.getEntfernung(destinator.getLocation(), executer.getLocation());
            if (executer.getTotalExperience() >= xpneeded) {
                plugin.Logger("Player: " + executer.getName() + " has enough XP", "Debug");
//                plugin.PlayerLogger(player2, String.format(plugin.getConfig().getString("teleport.teleportrequesttoplayer." + plugin.config.language), player2.getName(), plugin.getConfig().getInt("Cooldownoftp")), "");
//                plugin.PlayerLogger(executer, String.format(plugin.getConfig().getString("teleport.teleportmustbeaccepted." + plugin.config.language), player2.getName(), plugin.getConfig().getInt("Cooldownoftp")), "");
//                plugin.PlayerLogger(executer, String.format(plugin.getConfig().getString("teleport.info." + plugin.config.language), xpneeded, entfernung), "Warning");
                plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

                    @Override
                    public void run() {
                        if (requested.containsKey(destinator.getName())) {
                            plugin.Logger("Player: " + executer.getName() + " telport found", "Debug");
                            requested.remove(destinator.getName());
                            requestedname.remove(destinator.getName());
                            if (exec.containsKey(executer.getName())) {
                                exec.remove(executer.getName());
                                execname.remove(executer.getName());
                            }
                            plugin.PlayerLogger(destinator, String.format(plugin.getConfig().getString("teleport.teleportrequesttimeoutaccept." + plugin.config.language), plugin.getConfig().getInt("Cooldownoftp")), "Warning");
                            plugin.PlayerLogger(executer, String.format(plugin.getConfig().getString("teleport.teleportrequesttimeout." + plugin.config.language), plugin.getConfig().getInt("Cooldownoftp")), "Warning");
                        }
                    }
                }, plugin.getConfig().getInt("Cooldownoftp") * 20);
            } else {
                plugin.PlayerLogger(executer, String.format(plugin.getConfig().getString("teleport.notenoughxp." + plugin.config.language), entfernung, xpneeded), "Error");
            }
        }
    }

    public void Teleportto(Player accepter) {
        Player teleporter = exec.get(requestedname.get(accepter.getName()));
        int entfernung = plugin.getEntfernung(accepter.getLocation(), teleporter.getLocation());
        int xpneeded = (int) (entfernung * plugin.getConfig().getDouble("teleport.xpperblock"));
        if (teleporter.getTotalExperience() >= xpneeded) {
            plugin.UpdateXP(teleporter, -xpneeded, "teleport");
            requested.remove(accepter.getName());
            requestedname.remove(accepter.getName());
            exec.remove(teleporter.getName());
            execname.remove(teleporter.getName());
            teleporter.teleport(accepter);
            teleporter.saveData();
            plugin.PlayerLogger(teleporter, String.format(plugin.getConfig().getString("teleport.teleportrequest." + plugin.config.language), teleporter.getName()), "");
            plugin.PlayerLogger(accepter, String.format(plugin.getConfig().getString("teleport.teleportrequest." + plugin.config.language), teleporter.getName()), "");
        } else {
            plugin.PlayerLogger(teleporter, String.format(plugin.getConfig().getString("teleport.notenoughxp." + plugin.config.language), entfernung, xpneeded), "Error");
        }
    }

    public void Teleporttoexecutor(Player accepter) {
        Player teleporter = exec.get(requestedname.get(accepter.getName()));
        int entfernung = plugin.getEntfernung(accepter.getLocation(), teleporter.getLocation());
        int xpneeded = (int) (entfernung * plugin.getConfig().getDouble("teleport.xpperblock"));
        if (teleporter.getTotalExperience() >= xpneeded) {
            plugin.UpdateXP(teleporter, -xpneeded, "teleport");
            if (plugin.config.usedbtomanageXP) {
                plugin.SQL.UpdateXP(teleporter.getName(), ((int) (teleporter.getTotalExperience() - xpneeded)));
            }
            requested.remove(accepter.getName());
            requestedname.remove(accepter.getName());
            exec.remove(teleporter.getName());
            execname.remove(teleporter.getName());
            accepter.teleport(teleporter);
            teleporter.saveData();
            accepter.saveData();
            plugin.PlayerLogger(teleporter, String.format(plugin.getConfig().getString("teleport.teleportrequest." + plugin.config.language), accepter.getName()), "");
            plugin.PlayerLogger(accepter, String.format(plugin.getConfig().getString("teleport.teleportrequest." + plugin.config.language), accepter.getName()), "");
        } else {
            plugin.PlayerLogger(teleporter, String.format(plugin.getConfig().getString("teleport.notenoughxp." + plugin.config.language), entfernung, xpneeded), "Error");
        }
    }

    public void acceptteleport(Player accepter) {
        if (requested.containsKey(accepter.getName())) {
            plugin.Logger("Player: " + accepter.getName() + " found TP", "Debug");
            Player teleporter = exec.get(requestedname.get(accepter.getName()));
            if (execname.containsKey(teleporter.getName())) {
                plugin.Logger("Player: " + teleporter.getName() + " found TP as executer", "Debug");
                if (execname.get(teleporter.getName())) {
                    plugin.Logger("Player: " + teleporter.getName() + " teleportexecuter", "Debug");
                    Teleporttoexecutor(accepter);
                } else {
                    plugin.Logger("Player: " + teleporter.getName() + " teleportto", "Debug");
                    Teleportto(accepter);
                }
            }
        } else {
            plugin.PlayerLogger(accepter, plugin.getConfig().getString("teleport.noteleport." + plugin.config.language), "Error");
        }
    }

    public void denyteleport(Player accepter) {
        if (requested.containsKey(accepter.getName())) {
            Player teleporter = exec.get(requestedname.get(accepter.getName()));
            if (execname.containsKey(teleporter.getName())) {
                execname.remove(teleporter.getName());
                exec.remove(teleporter.getName());
            }
            requested.remove(accepter.getName());
            requestedname.remove(accepter.getName());
            plugin.PlayerLogger(teleporter, plugin.getConfig().getString("teleport.teleportdenied." + plugin.config.language), "");
            plugin.PlayerLogger(accepter, plugin.getConfig().getString("teleport.teleportdenied." + plugin.config.language), "");
        } else {
            plugin.PlayerLogger(accepter, plugin.getConfig().getString("teleport.noteleport." + plugin.config.language), "Error");
        }
    }

    public void stopteleport(Player accepter) {
        if (exec.containsKey(accepter.getName())) {
            Player teleporter = exec.get(accepter.getName());
            execname.remove(accepter.getName());
            exec.remove(accepter.getName());
            requested.remove(teleporter.getName());
            requestedname.remove(teleporter.getName());
            plugin.PlayerLogger(accepter, plugin.getConfig().getString("teleport.teleportstopped." + plugin.config.language), "");
        } else {
            plugin.PlayerLogger(accepter, plugin.getConfig().getString("teleport.noteleport." + plugin.config.language), "Error");
        }
    }
}
