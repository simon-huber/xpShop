package me.ibhh.xpShop;

import org.bukkit.entity.Player;

public class PermissionsChecker {

    private xpShop plugin;

    public PermissionsChecker(xpShop pl, String von) {
        this.plugin = pl;
    }

    public boolean checkpermissionssilent(Player player, String action) {
        if (plugin.toggle) {
            return false;
        }
        try {
            if (player.isOp()) {
                return true;
            }
            try {
                if (player.hasPermission(action) || player.hasPermission(action.toLowerCase())) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                plugin.report.report(3327, "Couldnt check permission with bPermissions", e.getMessage(), "PermissionsChecker", e);
                return false;
            }
        } catch (Exception e) {
            plugin.Logger("Error on checking permissions!", "Error");
            plugin.report.report(3328, "Error on checking permissions", e.getMessage(), "PermissionsChecker", e);
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkpermissions(Player player, String action) {
        if (plugin.toggle) {
            return false;
        }
        try {
            if (player.isOp()) {
                return true;
            }
            try {
                if (player.hasPermission(action) || player.hasPermission(action.toLowerCase())) {
                    return true;
                } else {
                    plugin.PlayerLogger(player, player.getName() + " " + plugin.getConfig().getString("permissions.error." + plugin.getConfig().getString("language")) + " (" + action + ")", "Error");
                    return false;
                }
            } catch (Exception e) {
                plugin.Logger("Error on checking permissions with bPermissions!", "Error");
                plugin.report.report(3332, "Couldnt check permission with bPermissions", e.getMessage(), "PermissionsChecker", e);
                plugin.PlayerLogger(player, "Error on checking permissions with bPermissions!", "Error");
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            plugin.Logger("Error on checking permissions!", "Error");
            plugin.report.report(3333, "Error on checking permissions", e.getMessage(), "PermissionsChecker", e);
            plugin.PlayerLogger(player, "Error on checking permissions!", "Error");
            e.printStackTrace();
            return false;
        }
    }
}