package me.ibhh.xpShop;

import de.bananaco.bpermissions.api.util.CalculableType;
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PermissionsChecker {

    private xpShop plugin;
    private GroupManager groupManager;
    public int PermPlugin = 0;

    public PermissionsChecker(xpShop pl, String von) {
        this.plugin = pl;
        final String von2 = von;
        final PluginManager pluginManager = plugin.getServer().getPluginManager();
        final Plugin GMplugin = pluginManager.getPlugin("GroupManager");

        plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (GMplugin != null && GMplugin.isEnabled()) {
                    groupManager = (GroupManager) GMplugin;

                }
                plugin.Logger("checking PermissionsPlugin!", "Debug");
                searchpermplugin();
            }
        }, 1);
    }

    public void searchpermplugin() {
        try {
            plugin.getServer().getServicesManager().getRegistration(ru.tehkode.permissions.bukkit.PermissionsEx.class);
            PermPlugin = 2;
            plugin.Logger("Permissions: Hooked into PermissionsEX!", "Debug");
            return;
        } catch (NoClassDefFoundError e) {
        }
        try {
            plugin.getServer().getServicesManager().getRegistration(org.anjocaido.groupmanager.GroupManager.class);
            PermPlugin = 3;
            plugin.Logger("Permissions: Hooked into GroupManager!", "Debug");
            return;
        } catch (NoClassDefFoundError e) {
        }
        try {
            plugin.getServer().getServicesManager().getRegistration(de.bananaco.bpermissions.api.util.CalculableType.class);
            PermPlugin = 3;
            plugin.Logger("Permissions: Hooked into bPermissions!", "Debug");
            return;
        } catch (NoClassDefFoundError e) {
        }
        PermPlugin = 1;
    }

    public boolean checkpermissionssilent(Player player, String action) {
        if (plugin.toggle) {
            return false;
        }
        try {
            if (player.isOp()) {
                return true;
            }
            if (PermPlugin == 1) {
                try {
                    if (player.hasPermission(action)) {
                        return true;
                    }
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } else if (PermPlugin == 2) {
                if(!Bukkit.getPluginManager().isPluginEnabled("PermissionsEx")){
                   return false;
                }
                try {
                    PermissionUser user = PermissionsEx.getUser(player);;
                    if (user.has(action)) {
                        return true;
                    }
                    return false;
                } catch (Exception e) {
                    try {
                        PermissionManager permissions = PermissionsEx.getPermissionManager();
                        if (permissions.has(player, action)) {
                            return true;
                        }
                        return false;
                    } catch (Exception e1) {
                        e.printStackTrace();
                        return false;
                    }
                }
            } else if (PermPlugin == 3) {
                if(!Bukkit.getPluginManager().isPluginEnabled("GroupManager")){
                   return false;
                }
                try {
                    final AnjoPermissionsHandler handler = groupManager.getWorldsHolder().getWorldPermissions(player);
                    {
                        if (handler != null) {
                            if (handler.has(player, action)) {
                                return true;
                            } else {
                                return false;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } else if (PermPlugin == 4) {
                if(!Bukkit.getPluginManager().isPluginEnabled("bPermissions")){
                   return false;
                }
                try {
                    if (de.bananaco.bpermissions.api.ApiLayer.hasPermission(player.getWorld().getName(), CalculableType.USER, player.getName(), action)) {
                        return true;
                    } else if (de.bananaco.bpermissions.api.ApiLayer.hasPermission(player.getWorld().getName(), CalculableType.GROUP, player.getName(), action)) {
                        return true;
                    } else {
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                System.out.println("PermissionsEx plugin are not found.");
                return false;
            }
        } catch (Exception e) {
            plugin.Logger("Error on checking permissions!", "Error");
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public boolean checkpermissions(Player player, String action) {
        if (plugin.toggle) {
            return false;
        }
        try {
            if (player.isOp()) {
                return true;
            }
            if (PermPlugin == 1) {
                try {
                    if (player.hasPermission(action)) {
                        return true;
                    } else {
                        plugin.PlayerLogger(player, player.getName() + " " + plugin.getConfig().getString("permissions.error." + plugin.getConfig().getString("language")) + " (" + action + ")", "Error");
                        return false;
                    }
                } catch (Exception e) {
                    plugin.Logger("Error on checking permissions with BukkitPermissions!", "Error");
                    plugin.PlayerLogger(player, "Error on checking permissions with BukkitPermissions!", "Error");
                    e.printStackTrace();
                    return false;
                }
            } else if (PermPlugin == 2) {
                if (!Bukkit.getPluginManager().isPluginEnabled("PermissionsEx")) {
                    plugin.PlayerLogger(player, "PermissionsEX is not enabled!", "Error");
                    return false;
                }
                try {
                    PermissionUser user = PermissionsEx.getUser(player);
                    if (user.has(action)) {
                        return true;
                    } else {
                        plugin.PlayerLogger(player, player.getName() + " " + plugin.getConfig().getString("permissions.error." + plugin.getConfig().getString("language")) + " (" + action + ")", "Error");
                        return false;
                    }
                } catch (Exception e) {
                    plugin.Logger("Error on checking permissions with PermissionsEX!", "Error");
                    try {
                        PermissionManager permissions = PermissionsEx.getPermissionManager();

                        if (permissions.has(player, action)) {
                            return true;
                        } else {
                            plugin.PlayerLogger(player, player.getName() + " " + plugin.getConfig().getString("permissions.error." + plugin.getConfig().getString("language")) + " (" + action + ")", "Error");
                            return false;
                        }
                    } catch (Exception e1) {
                        e.printStackTrace();
                        return false;
                    }
                }
            } else if (PermPlugin == 3) {
                if (!Bukkit.getPluginManager().isPluginEnabled("GroupManager")) {
                    return false;
                }
                try {
                    final AnjoPermissionsHandler handler = groupManager.getWorldsHolder().getWorldPermissions(player);
                    {
                        if (handler != null) {
                            if (handler.has(player, action)) {
                                return true;
                            } else {
                                plugin.PlayerLogger(player, player.getName() + " " + plugin.getConfig().getString("permissions.error." + plugin.getConfig().getString("language")) + " (" + action + ")", "Error");
                                return false;
                            }
                        }
                    }
                } catch (Exception e) {
                    plugin.Logger("Error on checking permissions with GroupManager!", "Error");
                    plugin.PlayerLogger(player, "Error on checking permissions with GroupManager!", "Error");
                    e.printStackTrace();
                    return false;
                }
            } else if (PermPlugin == 4) {
                if (!Bukkit.getPluginManager().isPluginEnabled("bPermissions")) {
                    return false;
                }
                try {
                    if (de.bananaco.bpermissions.api.ApiLayer.hasPermission(player.getWorld().getName(), CalculableType.USER, player.getName(), action)) {
                        return true;
                    } else if (de.bananaco.bpermissions.api.ApiLayer.hasPermission(player.getWorld().getName(), CalculableType.GROUP, player.getName(), action)) {
                        return true;
                    } else {
                        plugin.PlayerLogger(player, player.getName() + " " + plugin.getConfig().getString("permissions.error." + plugin.getConfig().getString("language")) + " (" + action + ")", "Error");
                        return false;
                    }
                } catch (Exception e) {
                    plugin.Logger("Error on checking permissions with bPermissions!", "Error");
                    plugin.PlayerLogger(player, "Error on checking permissions with bPermissions!", "Error");
                    e.printStackTrace();
                    return false;
                }
            } else {
                plugin.PlayerLogger(player, player.getName() + " " + plugin.getConfig().getString("permissions.error." + plugin.getConfig().getString("language")) + " (" + action + ")", "Error");
                System.out.println("PermissionsEx plugin are not found.");
                return false;
            }
        } catch (Exception e) {
            plugin.Logger("Error on checking permissions!", "Error");
            plugin.PlayerLogger(player, "Error on checking permissions!", "Error");
            e.printStackTrace();
            return false;
        }
        return false;
    }
}