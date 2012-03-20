package me.ibhh.xpShop;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class xpShopListener implements Listener {

    private final xpShop plugin;
    private String[] split;
    private static final BlockFace[] shopFaces = {BlockFace.SELF, BlockFace.DOWN, BlockFace.UP, BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH};

    public xpShopListener(xpShop xpShop) {
        this.plugin = xpShop;
        xpShop.getServer().getPluginManager().registerEvents(this, xpShop);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void join(PlayerJoinEvent event) {
        if (plugin.config.usedbtomanageXP) {
            String playername;
            Player player = event.getPlayer();
            double XP = plugin.getTOTALXP(player);
            double XPneu = XP;
            playername = player.getName();
            if (plugin.config.debug) {
                plugin.Logger("Playername (joined): " + playername, "Debug");
            }
            try {
                if (plugin.SQL.isindb(playername)) {
                    if (plugin.config.debug) {
                        plugin.Logger("Playername is in db: " + playername + "With " + XP + " XP! DB: " + plugin.SQL.getXP(playername), "Debug");
                    }
                    XPneu = plugin.SQL.getXP(playername);
                } else {
                    XPneu = plugin.getTOTALXP(player);
                    plugin.SQL.InsertAuction(playername, (int) XPneu);
                    if (plugin.config.debug) {
                        plugin.Logger("Playername insert into db: " + playername + "With " + XP + " XP! DB: " + plugin.SQL.getXP(playername), "Debug");
                    }

                }
            } catch (SQLException we) {
                return;
            }
            if (XP != XPneu) {
                if (XP < XPneu) {
                    plugin.PlayerLogger(player, String.format(plugin.config.addedxp, (int) (XPneu - XP)), "");
                } else if (XPneu < XP) {
                    plugin.PlayerLogger(player, String.format(plugin.config.substractedxp, (int) (XP - XPneu)), "");
                }
                player.setLevel(0);
                player.setExp(0);
                plugin.UpdateXP(player, (int) XP, "Join");
                player.saveData();
            }
        }
        if (!plugin.Blacklistcode.startsWith("1")) {
            if (plugin.PermissionsHandler.checkpermissionssilent(event.getPlayer(), "xpShop.admin")) {
                if (plugin.config.debug) {
                    plugin.Logger("Player: " + event.getPlayer().getName() + " has permission: \"xpShop.admin\"", "Debug");
                }
                if (plugin.updateaviable) {
                    plugin.PlayerLogger(event.getPlayer(), "New xpShop update aviable: type \"xpShopupdate\" please!", "Warning");
                }

            } else {
                if (plugin.config.debug) {
                    plugin.Logger("Player: " + event.getPlayer().getName() + " has no permission: \"xpShop.admin\"", "Debug");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void Verzaubern(PlayerLevelChangeEvent event) {
        if (plugin.config.usedbtomanageXP) {
            final Player player = event.getPlayer();
            final String playername = player.getName();
            plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

                @Override
                public void run() {
                    if (plugin.config.debug) {
                        plugin.Logger("Saving new XP!", "");
                    }
                    double XP;
                    XP = plugin.getTOTALXP(player);
                    plugin.SQL.UpdateXP(playername, (int) XP);
                    if (plugin.config.debug) {
                        try {
                            plugin.Logger("Player updated into db: " + playername + "With " + plugin.getTOTALXP(player) + " XP! DB: " + plugin.SQL.getXP(playername), "Debug");
                        } catch (SQLException ex) {
                            Logger.getLogger(xpShopListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void change(PlayerExpChangeEvent event) {
        if (plugin.config.usedbtomanageXP) {
            final Player player = event.getPlayer();
            final String playername = player.getName();
            plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

                @Override
                public void run() {
                    if (plugin.config.debug) {
                        plugin.Logger("Saving new XP!", "");
                    }
                    double XP;
                    XP = plugin.getTOTALXP(player);
                    plugin.SQL.UpdateXP(playername, (int) XP);
                    if (plugin.config.debug) {
                        try {
                            plugin.Logger("Player updated into db: " + playername + "With " + plugin.getTOTALXP(player) + " XP! DB: " + plugin.SQL.getXP(playername), "Debug");
                        } catch (SQLException ex) {
                            Logger.getLogger(xpShopListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void resp(PlayerDeathEvent event) {
        if (plugin.config.keepxpondeath) {
            Player player = (Player) event.getEntity();
            if (player != null) {
                player.setExp(0);
                player.setLevel(0);
                try {
                    plugin.UpdateXP(player, plugin.SQL.getXP(player.getName()), "respawn");
                } catch (SQLException ex) {
                    Logger.getLogger(xpShopListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void death(PlayerDeathEvent event) {
        if (plugin.config.usedbtomanageXP) {
            if (plugin.config.keepxpondeath) {
                double XP;
                Player player = (Player) event.getEntity();
                event.setKeepLevel(true);
                XP = plugin.getTOTALXP(player);
                plugin.SQL.UpdateXP(player.getName(), (int) XP);
            } else {
                double XP;
                Player player = (Player) event.getEntity();
                plugin.SQL.UpdateXP(player.getName(), (int) 0);
                if (plugin.config.debug) {
                    try {
                        plugin.Logger("Player updated into db: " + player.getName() + " With " + plugin.getTOTALXP(player) + " XP! DB: " + plugin.SQL.getXP(player.getName()), "Debug");
                    } catch (SQLException ex) {
                        Logger.getLogger(xpShopListener.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } else {
            if (plugin.config.keepxpondeath) {
                event.setKeepLevel(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void kick(PlayerKickEvent event) {
        if (plugin.config.usedbtomanageXP) {
            final Player player = event.getPlayer();
            final String playername = player.getName();
            plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

                @Override
                public void run() {
                    if (plugin.config.debug) {
                        plugin.Logger("Saving new XP!", "");
                    }
                    double XP;
                    XP = plugin.getTOTALXP(player);
                    plugin.SQL.UpdateXP(playername, (int) XP);
                    if (plugin.config.debug) {
                        try {
                            plugin.Logger("Player updated into db: " + playername + "With " + plugin.getTOTALXP(player) + " XP! DB: " + plugin.SQL.getXP(player.getName()), "Debug");
                        } catch (SQLException ex) {
                            Logger.getLogger(xpShopListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }, 2L);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void quit(PlayerQuitEvent event) {
        if (plugin.config.usedbtomanageXP) {
            final Player player = event.getPlayer();
            final String playername = player.getName();
            plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

                @Override
                public void run() {
                    if (plugin.config.debug) {
                        plugin.Logger("Saving new XP!", "");
                    }
                    double XP;
                    XP = plugin.getTOTALXP(player);
                    try {
                        if (XP != plugin.SQL.getXP(playername)) {
                            plugin.SQL.UpdateXP(playername, (int) XP);
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(xpShopListener.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (plugin.config.debug) {
                        try {
                            plugin.Logger("Player updated into db: " + playername + "With " + plugin.getTOTALXP(player) + " XP! DB: " + plugin.SQL.getXP(playername), "Debug");
                        } catch (SQLException ex) {
                            Logger.getLogger(xpShopListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }, 2L);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void aendern(SignChangeEvent event) {
        Player p = event.getPlayer();
        String[] line = event.getLines();
        if (plugin.config.debug) {
            plugin.Logger("First Line " + line[0], "Debug");
        }
        if (event.getLine(0).equalsIgnoreCase("[xpShop]")) {
            if (plugin.config.debug) {
                plugin.Logger("First Line [xpShop]", "Debug");
            }
            if (!plugin.Blacklistcode.startsWith("1", 10)) {
                if (plugin.config.debug) {
                    plugin.Logger(plugin.Blacklistcode, "Debug");
                }
                try {
                    if (blockIsValid(line, "create", p)) {
                        if (plugin.config.debug) {
                            plugin.Logger("Sign is valid", "Debug");
                        }
                        if (!line[1].equalsIgnoreCase("AdminShop") && line[1].length() < 16) {
                            if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.create.own")) {
                                if (plugin.config.debug) {
                                    plugin.Logger("First line != null", "Debug");
                                }
                                event.setLine(1, event.getPlayer().getName());
                                plugin.PlayerLogger(event.getPlayer(), "Successfully created xpShop!", "");
                            } else {
                                if (plugin.config.debug) {
                                    plugin.Logger("Player " + p.getName() + " has no permission: xpShop.create", "Debug");
                                }
                                plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed!", "Error");
                                event.setCancelled(true);
                            }
                        } else if (line[1].equalsIgnoreCase(p.getName()) && line[1].length() < 16) {
                            if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.create.own")) {
                                plugin.PlayerLogger(event.getPlayer(), "Successfully created xpShop!", "");
                                event.setLine(0, "[xpShop]");
                            } else {
                                if (plugin.config.debug) {
                                    plugin.Logger("Player " + p.getName() + " has no permission: xpShop.create", "Debug");
                                }
                                plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed!", "Error");
                                event.setCancelled(true);
                            }
                        } else if (line[1].equalsIgnoreCase("AdminShop")) {
                            if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.create.admin")) {
                                if (plugin.config.debug) {
                                    plugin.Logger("Player " + p.getName() + " has permission: xpShop.create.admin", "Debug");
                                }
                                plugin.PlayerLogger(event.getPlayer(), "Successfully created xpShop!", "");
                            } else {
                                if (plugin.config.debug) {
                                    plugin.Logger("Player " + p.getName() + " has no permission: xpShop.create.admin", "Debug");
                                }
                                plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed!", "Error");
                                event.setCancelled(true);
                            }
                        } else if (line[1].length() >= 16) {
                            plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed! Username too long!", "Error");
                            event.setCancelled(true);
                        }
                    } else {
                        if (plugin.config.debug) {
                            plugin.Logger("Sign is not valid", "Debug");
                        }
                        plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed!", "Error");
                        event.setCancelled(true);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    event.setCancelled(true);
                    plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed!", "Error");
                }
            } else {
                if (plugin.config.debug) {
                    plugin.Logger(plugin.Blacklistcode, "Debug");
                }
                plugin.blacklistLogger(p);
                event.setCancelled(true);
            }
        } else if (line[0].equalsIgnoreCase("[xpShopSafe]")) {
            if (plugin.config.debug) {
                plugin.Logger("First Line [xpShopSafe]", "Debug");
            }
            if (!plugin.Blacklistcode.startsWith("1", 10)) {
                if (plugin.config.debug) {
                    plugin.Logger(plugin.Blacklistcode, "Debug");
                }
                if (SafeIsValid(line)) {
                    if (plugin.config.debug) {
                        plugin.Logger("Safe is valid", "Debug");
                    }
                    if (line[1].equalsIgnoreCase(p.getName()) && line[1].length() < 16) {
                        if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.safe.create")) {
                            plugin.PlayerLogger(event.getPlayer(), "Successfully created xpShopSafe!", "");
                            event.setLine(0, "[xpShopSafe]");
                            event.setLine(2, "0");
                        } else {
                            if (plugin.config.debug) {
                                plugin.Logger("Player " + p.getName() + " has no permission: xpShop.safe.create", "Debug");
                            }
                            plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed!", "Error");
                            event.setCancelled(true);
                        }
                    } else if (line[1].length() >= 16) {
                        plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed! Username too long!", "Error");
                        event.setCancelled(true);
                    } else if (!(line[1].equalsIgnoreCase(p.getName()))) {
                        if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.safe.create")) {
                            if (plugin.config.debug) {
                                plugin.Logger("First line != null", "Debug");
                            }
                            event.setLine(1, p.getName());
                            event.setLine(2, "0");
                            plugin.PlayerLogger(event.getPlayer(), "Successfully created xpShop!", "");
                        } else {
                            if (plugin.config.debug) {
                                plugin.Logger("Player " + p.getName() + " has no permission: xpShop.create", "Debug");
                            }
                            plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed!", "Error");
                            event.setCancelled(true);
                        }
                    }
                } else {
                    if (plugin.config.debug) {
                        plugin.Logger("Sign is not valid", "Debug");
                    }
                    plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed!", "Error");
                    event.setCancelled(true);
                }
            } else {
                if (plugin.config.debug) {
                    plugin.Logger(plugin.Blacklistcode, "Debug");
                }
                plugin.blacklistLogger(p);
                event.setCancelled(true);
            }
        }
    }

//    @EventHandler
//    public void onPlace(BlockPlaceEvent event) {
//        if ((event.getBlock() instanceof Sign)) {
//            Sign sign = (Sign) event.getBlock().getState();
//            String[] line = sign.getLines();
//            Player p = event.getPlayer();
//            if (sign.getLine(0).equalsIgnoreCase("[xpShop]")) {
//                try {
//                    if (plugin.Permission.checkpermissions(p, "xpShop.create")) {
//                        if (blockIsValid(line, "create", p)) {
//                            if (!sign.getLine(1).isEmpty()) {
//                                if (plugin.Permission.checkpermissions(p, "xpShop.create.admin")) {
//                                    plugin.PlayerLogger(event.getPlayer(), "Successfully created xpShop!", "");
//                                } else {
//                                    sign.setLine(1, event.getPlayer().getName());
//                                }
//                            } else {
//                                plugin.PlayerLogger(event.getPlayer(), "Successfully created xpShop!", "");
//                                sign.setLine(0, "[xpShop]");
//                            }
//                        } else {
//                            plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed!", "Error");
//                            event.setCancelled(true);
//                        }
//                    } else {
//                        event.setCancelled(true);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    event.setCancelled(true);
//                    plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed!", "Error");
//                }
//            }
//        }
//    }
    public static Block getAttachedFace(org.bukkit.block.Sign sign) {
        return sign.getBlock().getRelative(((org.bukkit.material.Sign) sign.getData()).getAttachedFace());
    }

    private static boolean isCorrectSign(org.bukkit.block.Sign sign, Block block) {
        return (sign != null) && ((sign.getBlock().equals(block)) || (getAttachedFace(sign).equals(block)));
    }

    public static boolean isSign(Block block) {
        return block.getState() instanceof Sign;
    }

    public Sign findSign(Block block, String originalName) {
        for (BlockFace bf : shopFaces) {
            Block faceBlock = block.getRelative(bf);
            if (isSign(faceBlock)) {
                Sign sign = (Sign) faceBlock.getState();
                if ((blockIsValid(sign)) && ((faceBlock.equals(block)) || (getAttachedFace(sign).equals(block)))) {
                    return sign;
                }
            }
        }
        return null;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        if (!(event.getBlock().getState() instanceof Sign)) {
            if (plugin.config.debug) {
                plugin.Logger("Block dedected", "Debug");
            }
            org.bukkit.block.Sign sign = findSign(event.getBlock(), p.getName());
            if (isCorrectSign(sign, event.getBlock())) {
                if (sign.getLine(0).equalsIgnoreCase("[xpShop]")) {
                    if (!plugin.Blacklistcode.startsWith("1", 12)) {
                        String[] line = sign.getLines();
                        if (blockIsValid(sign)) {
                            if (line[1].equalsIgnoreCase(p.getName()) && plugin.PermissionsHandler.checkpermissions(p, "xpShop.create.own")) {
                                plugin.PlayerLogger(p, "Destroying xpShop!", "");
                            } else if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.create.admin")) {
                                plugin.PlayerLogger(p, "Destroying xpShop (Admin)!", "");
                            } else {
                                event.setCancelled(true);
                            }
                        }
                    } else {
                        plugin.blacklistLogger(p);
                        event.setCancelled(true);
                    }
                } else if (sign.getLine(0).equalsIgnoreCase("[xpShopSafe]")) {
                    if (!plugin.Blacklistcode.startsWith("1", 12)) {
                        String[] line = sign.getLines();
                        if (SafeIsValid(sign)) {
                            if (line[1].equalsIgnoreCase(p.getName()) && plugin.PermissionsHandler.checkpermissions(p, "xpShop.safe.create")) {
                                plugin.PlayerLogger(p, "Destroying xpShopSafe!", "");
                            } else if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.admin")) {
                                plugin.PlayerLogger(p, "Destroying xpShopSafe (Admin)!", "");
                            } else {
                                event.setCancelled(true);
                            }
                        }
                    } else {
                        plugin.blacklistLogger(p);
                        event.setCancelled(true);
                    }
                }
            }
        } else {
            Sign s = (Sign) event.getBlock().getState();
            String[] line = s.getLines();
            if (plugin.config.debug) {
                plugin.Logger("Line 0: " + line[0], "Debug");
                plugin.Logger("Sign dedected", "Debug");
            }
            if (line[0].equalsIgnoreCase("[xpShop]")) {
                if (!plugin.Blacklistcode.startsWith("1", 12)) {
                    if (this.blockIsValid(line, "break", p)) {
                        if (!plugin.PermissionsHandler.checkpermissions(p, "xpShop.create.own") && !plugin.PermissionsHandler.checkpermissions(p, "xpShop.create.admin")) {
                            event.setCancelled(true);
                        } else if (s.getLine(1).equalsIgnoreCase(p.getName()) && plugin.PermissionsHandler.checkpermissions(p, "xpShop.create.own")) {
                            plugin.PlayerLogger(p, "Destroying xpShop!", "");
                            plugin.UpdateXP(p, Integer.parseInt(line[2]), "destroy");
                            if(plugin.config.usedbtomanageXP){
                                try {
                                    plugin.SQL.UpdateXP(p.getName(), plugin.SQL.getXP(p.getName()) + Integer.parseInt(line[2]));
                                } catch (SQLException ex) {
                                    Logger.getLogger(xpShopListener.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        } else if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.create.admin")) {
                            plugin.PlayerLogger(p, "Destroying xpShop (Admin)!", "");
                        } else {
                            event.setCancelled(true);
                        }
                    }
                } else {
                    plugin.blacklistLogger(p);
                    event.setCancelled(true);
                }
            } else if (line[0].equalsIgnoreCase("[xpShopSafe]")) {
                if (!plugin.Blacklistcode.startsWith("1", 12)) {
                    if (SafeIsValid(s)) {
                        if (line[1].equalsIgnoreCase(p.getName()) && plugin.PermissionsHandler.checkpermissions(p, "xpShop.safe.create")) {
                            plugin.PlayerLogger(p, "Destroying xpShopSafe!", "");
                            plugin.UpdateXP(p, Integer.parseInt(line[2]), "destroy");
                            if(plugin.config.usedbtomanageXP){
                                try {
                                    plugin.SQL.UpdateXP(p.getName(), plugin.SQL.getXP(p.getName()) + Integer.parseInt(line[2]));
                                } catch (SQLException ex) {
                                    Logger.getLogger(xpShopListener.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        } else if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.admin")) {
                            plugin.PlayerLogger(p, "Destroying xpShopSafe (Admin)!", "");
                        } else {
                            event.setCancelled(true);
                        }
                    }
                } else {
                    plugin.blacklistLogger(p);
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (plugin.config.debug) {
            plugin.Logger("A interact Event dected by player: " + p.getName(), "Debug");
        }
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (plugin.config.debug) {
                plugin.Logger("A left interact Event dected by player: " + p.getName(), "Debug");
            }
            if ((event.hasBlock()) && ((event.getClickedBlock().getState() instanceof Sign)) && (!p.isSneaking())) { // && !(p.isSneaking())
                Sign s = (Sign) event.getClickedBlock().getState();
                String[] line = s.getLines();
                String playername = p.getName();
                Player player = p;
                if (plugin.config.debug) {
                    plugin.Logger("Checking first line!", "Debug");
                }
                if (line[0].equalsIgnoreCase("[xpShop]")) {
                    if (plugin.config.debug) {
                        plugin.Logger(" first line [xpShop] and leftklick!", "Debug");
                    }
                    if (!plugin.Blacklistcode.startsWith("1", 11)) {
                        if (plugin.config.debug) {
                            plugin.Logger(" not blacklisted!", "Debug");
                        }
                        if (this.blockIsValid(line, "Interact", p)) {
                            if (plugin.config.debug) {
                                plugin.Logger(" Block is valid!", "Debug");
                            }
                            if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.use")) {
                                if (plugin.config.debug) {
                                    plugin.Logger("Player: " + p.getName() + " has the permission: xpShop.use", "Debug");
                                }
                                if (line[1].equalsIgnoreCase("AdminShop")) {
                                    double price = getPrice(s, p, true);
                                    if (price > 0) {
                                        if ((plugin.MoneyHandler.getBalance(p) - price) >= 0) {
                                            plugin.MoneyHandler.substract(price, p);
                                            if (plugin.config.usedbtomanageXP) {
                                                plugin.SQL.UpdateXP(p.getName(), (int) (Integer.parseInt(line[2]) + plugin.getTOTALXP(player)));
                                            }
                                            plugin.UpdateXP(p, (Integer.parseInt(s.getLine(2))), "Sign");
                                            plugin.PlayerLogger(p, String.format(plugin.config.Shopsuccessbuy, s.getLine(2), "Admin", split[0]), "");
                                        } else {
                                            plugin.PlayerLogger(p, plugin.config.Shoperrornotenoughmoneyconsumer, "Error");
                                        }
                                    } else {
                                        plugin.PlayerLogger(p, plugin.config.Shoperrorcantbuyhere, "Error");
                                    }
                                } else {
                                    if (plugin.config.usedbtomanageXP) {
                                        try {
                                            if (plugin.SQL.isindb(line[1])) {
                                                int XPPlayer = plugin.SQL.getXP(playername);
                                                if (plugin.SQL.getXP(line[1]) >= Integer.parseInt(line[2])) {
                                                    if (getPrice(s, p, true) > 0) {
                                                        double price = getPrice(s, p, true);
                                                        if ((plugin.MoneyHandler.getBalance(p) - price) >= 0) {
                                                            plugin.MoneyHandler.substract(price, p);
                                                            plugin.UpdateXP(p, Integer.parseInt(line[2]), "Sign");
                                                            plugin.SQL.UpdateXP(playername, XPPlayer + Integer.parseInt(line[2]));
                                                            plugin.PlayerLogger(p, String.format(plugin.config.Shopsuccessbuy, s.getLine(2), s.getLine(1), split[0]), "");
                                                            plugin.SQL.UpdateXP(line[1], plugin.SQL.getXP(line[1]) - Integer.parseInt(line[2]));
                                                            plugin.MoneyHandler.addmoney(price, line[1]);
                                                            if (plugin.getServer().getPlayer(line[1]) != null) {
                                                                Player Empfaenger = plugin.getServer().getPlayer(line[1]);
                                                                plugin.UpdateXP(Empfaenger, -(Integer.parseInt(line[2])), "Sign");
                                                                plugin.PlayerLogger(Empfaenger, String.format(plugin.config.Shopsuccesssellerbuy, s.getLine(2), p.getName(), split[0]), "");
                                                            }
                                                        } else {
                                                            plugin.PlayerLogger(p, plugin.config.Shoperrornotenoughmoneyconsumer, "Error");
                                                        }
                                                    } else {
                                                        plugin.PlayerLogger(p, plugin.config.Shoperrorcantbuyhere, "Error");
                                                    }
                                                } else {
                                                    plugin.PlayerLogger(p, plugin.config.Shoperrornotenoughxpseller, "Error");
                                                }
                                            } else {
                                                plugin.PlayerLogger(player, line[1] + " " + plugin.config.playerwasntonline, "Error");
                                            }
                                        } catch (Exception e) {
                                            plugin.PlayerLogger(player, line[1] + " " + plugin.config.playerwasntonline, "Error");
                                        }

                                    } else {
                                        Player empfaenger = plugin.getmyOfflinePlayer(line, 1);
                                        if (empfaenger != null) {
                                            if (plugin.config.getPlayerConfig(empfaenger, p)) {
                                                if (plugin.getTOTALXP(empfaenger) >= Integer.parseInt(line[2])) {
                                                    if (getPrice(s, p, true) > 0) {
                                                        double price = getPrice(s, p, true);
                                                        if ((plugin.MoneyHandler.getBalance(p) - price) >= 0) {
                                                            plugin.MoneyHandler.substract(price, p);
                                                            plugin.UpdateXP(p, (Integer.parseInt(s.getLine(2))), "Sign");
                                                            plugin.PlayerLogger(p, String.format(plugin.config.Shopsuccessbuy, s.getLine(2), s.getLine(1), split[0]), "");
                                                            plugin.UpdateXP(empfaenger, -(Integer.parseInt(s.getLine(2))), "Sign");
                                                            plugin.MoneyHandler.addmoney(price, empfaenger);
                                                            empfaenger.saveData();
                                                            plugin.PlayerLogger(empfaenger, String.format(plugin.config.Shopsuccesssellerbuy, s.getLine(2), p.getName(), split[0]), "");
                                                        } else {
                                                            plugin.PlayerLogger(p, plugin.config.Shoperrornotenoughmoneyconsumer, "Error");
                                                        }
                                                    } else {
                                                        plugin.PlayerLogger(p, plugin.config.Shoperrorcantbuyhere, "Error");
                                                    }
                                                } else {
                                                    plugin.PlayerLogger(p, plugin.config.Shoperrornotenoughxpseller, "Error");
                                                }
                                            }
                                        } else {
                                            plugin.PlayerLogger(p, line[1] + " " + plugin.config.playerwasntonline, "Error");
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        plugin.blacklistLogger(p);
                        event.setCancelled(true);
                    }
                } else if (line[0].equalsIgnoreCase("[xpShopSafe]")) {
                    if (plugin.config.debug) {
                        plugin.Logger(" first line [xpShopSafe] and leftklick!", "Debug");
                    }
                    if (!plugin.Blacklistcode.startsWith("1", 11)) {
                        if (plugin.config.debug) {
                            plugin.Logger(" not blacklisted!", "Debug");
                        }
                        if (this.SafeIsValid(line)) {
                            if (plugin.config.debug) {
                                plugin.Logger(" Safe is valid!", "Debug");
                            }
                            if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.safe.use")) {
                                if (plugin.config.debug) {
                                    plugin.Logger("Player: " + p.getName() + " has the permission: xpShop.safe.use", "Debug");
                                }
                                if (line[1].equalsIgnoreCase(playername)) {
                                    if (plugin.config.usedbtomanageXP) {
                                        try {
                                            if (plugin.SQL.isindb(line[1])) {
                                                int XPPlayer = plugin.SQL.getXP(playername);
                                                if (Integer.parseInt(line[3]) <= Integer.parseInt(line[2])) {
                                                    plugin.UpdateXP(p, Integer.parseInt(line[3]), "Safe");
                                                    plugin.SQL.UpdateXP(playername, XPPlayer + Integer.parseInt(line[3]));
                                                    int Erg = Integer.parseInt(line[2]) - Integer.parseInt(line[3]);
                                                    StringBuilder bui = new StringBuilder();
                                                    bui.append(Erg);
                                                    if (plugin.config.debug) {
                                                        plugin.Logger("Erg: line2 = " + bui.toString(), "Debug");
                                                    }
                                                    s.setLine(2, bui.toString());
                                                    s.update();
                                                    plugin.PlayerLogger(p, String.format(plugin.config.safedestore, Integer.parseInt(s.getLine(3))), "");
                                                } else {
                                                    plugin.PlayerLogger(p, plugin.config.safenotenoughinsafe, "Error");
                                                }
                                            } else {
                                                plugin.PlayerLogger(player, line[1] + " " + plugin.config.playerwasntonline, "Error");
                                            }
                                        } catch (SQLException e) {
                                            plugin.PlayerLogger(player, line[1] + " " + plugin.config.playerwasntonline, "Error");
                                        }

                                    } else {
                                        if (Integer.parseInt(line[3]) <= Integer.parseInt(line[2])) {
                                            plugin.UpdateXP(p, Integer.parseInt(line[3]), "Safe");
                                            p.saveData();
                                            int Erg = Integer.parseInt(line[2]) - Integer.parseInt(line[3]);
                                            StringBuilder bui = new StringBuilder();
                                            bui.append(Erg);
                                            if (plugin.config.debug) {
                                                plugin.Logger("Erg: line2 = " + bui.toString(), "Debug");
                                            }
                                            s.setLine(2, bui.toString());
                                            s.update();
                                            plugin.PlayerLogger(p, String.format(plugin.config.safedestore, Integer.parseInt(s.getLine(3))), "");
                                        } else {
                                            plugin.PlayerLogger(p, plugin.config.safenotenoughinsafe, "Error");
                                        }
                                    }
                                } else {
                                    plugin.PlayerLogger(p, plugin.config.safenotyoursafe, "");
                                }
                            }
                        }
                    }
                }
            }
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if ((event.hasBlock()) && ((event.getClickedBlock().getState() instanceof Sign)) && (!p.isSneaking())) { // && !(p.isSneaking())
                Sign s = (Sign) event.getClickedBlock().getState();
                String[] line = s.getLines();
                String playername = p.getName();
                Player player = p;
                if (line[0].equalsIgnoreCase("[xpShop]")) {
                    if (!plugin.Blacklistcode.startsWith("1", 11)) {
                        if (this.blockIsValid(line, "Interact", p)) {
                            if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.use")) {
                                if (line[1].equalsIgnoreCase("AdminShop")) {
                                    if (plugin.getTOTALXP(p) >= Integer.parseInt(line[2])) {
                                        if (getPrice(s, p, false) > 0) {
                                            double price = getPrice(s, p, false);
                                            plugin.MoneyHandler.addmoney(price, p);
                                            if (plugin.config.usedbtomanageXP) {
                                                plugin.SQL.UpdateXP(p.getName(), (int) (plugin.getTOTALXP(player) - Integer.parseInt(line[2])));
                                            }
                                            plugin.UpdateXP(p, -(Integer.parseInt(s.getLine(2))), "Sign");
                                            plugin.PlayerLogger(p, String.format(plugin.config.Shopsuccesssell, s.getLine(2), "Admin", split[1]), "");
                                        } else {
                                            plugin.PlayerLogger(p, plugin.config.Shoperrorcantsellhere, "Error");
                                        }
                                    } else {
                                        plugin.PlayerLogger(p, plugin.config.Shoperrornotenoughxpconsumer, "Error");
                                    }
                                } else {
                                    if (plugin.config.usedbtomanageXP) {
                                        try {
                                            if (plugin.SQL.isindb(line[1])) {
                                                int XPPlayer = plugin.SQL.getXP(playername);
                                                if (plugin.SQL.getXP(playername) >= Integer.parseInt(line[2])) {
                                                    if (getPrice(s, p, false) > 0) {
                                                        double price = getPrice(s, p, false);
                                                        if ((plugin.MoneyHandler.getBalance(line[1]) - price) >= 0) {
                                                            plugin.MoneyHandler.substract(price, line[1]);
                                                            plugin.MoneyHandler.addmoney(price, p);
                                                            plugin.UpdateXP(p, -(Integer.parseInt(line[2])), "Sign");
                                                            plugin.SQL.UpdateXP(playername, XPPlayer - Integer.parseInt(line[2]));
                                                            plugin.SQL.UpdateXP(line[1], plugin.SQL.getXP(line[1]) + Integer.parseInt(line[2]));
                                                            plugin.PlayerLogger(p, String.format(plugin.config.Shopsuccesssell, line[2], line[1], split[1]), "");
                                                            Player empfaenger = plugin.getServer().getPlayer(line[1]);
                                                            if (empfaenger != null) {
                                                                plugin.UpdateXP(empfaenger, (Integer.parseInt(line[2])), "Sign");
                                                                plugin.PlayerLogger(empfaenger, String.format(plugin.config.Shopsuccesssellerselled, s.getLine(2), playername, split[1]), "");
                                                            }
                                                        } else {
                                                            plugin.PlayerLogger(p, plugin.config.Shoperrornotenoughmoneyconsumer, "Error");
                                                        }
                                                    } else {
                                                        plugin.PlayerLogger(p, plugin.config.Shoperrorcantsellhere, "Error");
                                                    }
                                                } else {
                                                    plugin.PlayerLogger(p, plugin.config.Shoperrornotenoughxpconsumer, "Error");
                                                }
                                            } else {
                                                plugin.PlayerLogger(player, line[1] + " " + plugin.config.playerwasntonline, "Error");
                                            }

                                        } catch (Exception e) {
                                            plugin.PlayerLogger(player, line[1] + " " + plugin.config.playerwasntonline, "Error");
                                        }
                                    } else {

                                        Player empfaenger = plugin.getmyOfflinePlayer(line, 1);
                                        if (empfaenger != null) {
                                            if (plugin.config.getPlayerConfig(empfaenger, p)) {
                                                if (plugin.getTOTALXP(p) >= Integer.parseInt(line[2])) {
                                                    if (getPrice(s, p, false) > 0) {
                                                        double price = getPrice(s, p, false);
                                                        if ((plugin.MoneyHandler.getBalance(empfaenger) - price) >= 0) {
                                                            plugin.MoneyHandler.substract(price, empfaenger);
                                                            plugin.UpdateXP(empfaenger, (Integer.parseInt(s.getLine(2))), "Sign");
                                                            plugin.PlayerLogger(empfaenger, String.format(plugin.config.Shopsuccesssellerselled, s.getLine(2), p.getName(), split[1]), "");
                                                            plugin.MoneyHandler.addmoney(price, p);
                                                            plugin.UpdateXP(p, -(Integer.parseInt(s.getLine(2))), "Sign");
                                                            empfaenger.saveData();
                                                            plugin.PlayerLogger(p, String.format(plugin.config.Shopsuccesssell, s.getLine(2), s.getLine(1), split[1]), "");
                                                        } else {
                                                            plugin.PlayerLogger(p, plugin.config.Shoperrornotenoughmoneyconsumer, "Error");
                                                        }
                                                    } else {
                                                        plugin.PlayerLogger(p, plugin.config.Shoperrorcantsellhere, "Error");
                                                    }
                                                } else {
                                                    plugin.PlayerLogger(p, plugin.config.Shoperrornotenoughxpconsumer, "Error");
                                                }
                                            }
                                        } else {
                                            plugin.PlayerLogger(p, line[1] + " " + plugin.config.playerwasntonline, "Error");
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        plugin.blacklistLogger(p);
                        event.setCancelled(true);
                    }
                } else if (line[0].equalsIgnoreCase("[xpShopSafe]")) {
                    if (!plugin.Blacklistcode.startsWith("1", 11)) {
                        if (this.SafeIsValid(line)) {
                            if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.safe.use")) {
                                if (line[1].equalsIgnoreCase(playername)) {
                                    if (plugin.config.usedbtomanageXP) {
                                        try {
                                            if (plugin.SQL.isindb(line[1])) {
                                                int XPPlayer = plugin.SQL.getXP(playername);
                                                if (XPPlayer >= Integer.parseInt(line[3]) && line[2] != null) {
                                                    plugin.UpdateXP(p, -(Integer.parseInt(line[3])), "Safe");
                                                    plugin.SQL.UpdateXP(playername, XPPlayer - Integer.parseInt(line[3]));
                                                    int Erg = Integer.parseInt(line[2]) + Integer.parseInt(line[3]);
                                                    StringBuilder bui = new StringBuilder();
                                                    bui.append(Erg);
                                                    if (plugin.config.debug) {
                                                        plugin.Logger("Erg: line2 = " + bui.toString(), "Debug");
                                                    }
                                                    s.setLine(2, bui.toString());
                                                    s.update();
                                                    plugin.PlayerLogger(p, String.format(plugin.config.safestore, Integer.parseInt(s.getLine(3))), "");
                                                } else {
                                                    plugin.PlayerLogger(p, String.format(plugin.config.safenotenoughxptostore, plugin.getTOTALXP(player)), "Error");
                                                }
                                            } else {
                                                plugin.PlayerLogger(player, line[1] + " " + plugin.config.playerwasntonline, "Error");
                                            }

                                        } catch (SQLException e) {
                                            plugin.PlayerLogger(player, line[1] + " " + plugin.config.playerwasntonline, "Error");
                                        }
                                    } else {
                                        if (plugin.getTOTALXP(p) >= Integer.parseInt(line[3]) && line[2] != null) {
                                            plugin.UpdateXP(p, -(Integer.parseInt(s.getLine(3))), "Safe");
                                            p.saveData();
                                            int Erg = Integer.parseInt(line[2]) + Integer.parseInt(line[3]);
                                            StringBuilder bui = new StringBuilder();
                                            bui.append(Erg);
                                            if (plugin.config.debug) {
                                                plugin.Logger("Erg: line2 = " + bui.toString(), "Debug");
                                            }
                                            s.setLine(2, bui.toString());
                                            s.update();
                                            plugin.PlayerLogger(p, String.format(plugin.config.safestore, Integer.parseInt(s.getLine(3))), "");
                                        } else {
                                            plugin.PlayerLogger(p, String.format(plugin.config.safenotenoughxptostore, plugin.getTOTALXP(player)), "Error");
                                        }
                                    }
                                } else {
                                    plugin.PlayerLogger(p, plugin.config.safenotyoursafe, "");
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    private double getPrice(Sign s, Player p, boolean buy) {
        split = s.getLine(3).split(":");
        double doubeline1 = 0;
        try {
            if (buy) {
                doubeline1 = Double.parseDouble(split[0]);
            } else {
                doubeline1 = Double.parseDouble(split[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doubeline1;
    }

    public boolean blockIsValid(String[] lines, String von, Player p) {
        boolean a = false;
        if (plugin.config.debug) {
            plugin.Logger("Checking if block is valid!", "Debug");
        }
        String[] temp = null;
        try {
            temp = lines[3].split(":");
            if (plugin.config.debug) {
                plugin.Logger("Line 3 is: " + lines[3], "Debug");
            }
        } catch (Exception e) {
            if (plugin.config.debug) {
                plugin.Logger("Contains no : ", "Debug");
            }
        }
        try {
            if (Tools.isFloat(temp[0]) && Tools.isFloat(temp[1])) {
                if (plugin.config.debug) {
                    plugin.Logger("Buy and sell amount are ints: " + temp[0] + " und " + temp[1], "Debug");
                }
                if (Float.parseFloat(temp[0]) > 0 || Float.parseFloat(temp[1]) > 0) {
                    if (plugin.config.debug) {
                        plugin.Logger("One of them is greater than 0: " + temp[0] + " und " + temp[1], "Debug");
                    }
                    if (!(Float.parseFloat(temp[0]) < 0) && !(Float.parseFloat(temp[1]) < 0)) {
                        if (plugin.config.debug) {
                            plugin.Logger("None of them is smaller than 0: " + temp[0] + " und " + temp[1], "Debug");
                        }
                        if (Tools.isInteger(lines[2])) {
                            if (Integer.parseInt(lines[2]) > 0) {
                                if (plugin.config.debug) {
                                    plugin.Logger("Line 2 is int", "Debug");
                                }
                                a = true;
                                if (plugin.config.debug) {
                                    plugin.Logger("block is valid!", "Debug");
                                }
                            }
                        }
                    } else {
                        if (plugin.config.debug) {
                            plugin.Logger("One of them is smaller than 0: " + temp[0] + " und " + temp[1], "Debug");
                        }
                    }
                } else {
                    if (plugin.config.debug) {
                        plugin.Logger("None of them is greater than 0: " + temp[0] + " und " + temp[1], "Debug");
                    }
                }
            } else {
                if (plugin.config.debug) {
                    plugin.Logger("!Tools.isFloat(temp[0]) || !Tools.isFloat(temp[1])", "Debug");
                }
            }
        } catch (Exception ew) {
        }

        return a;
    }

    public boolean blockIsValid(Sign sign) {
        boolean a = false;
        if (plugin.config.debug) {
            plugin.Logger("Checking if block is valid!", "Debug");
        }
        String[] lines = sign.getLines();
        String[] temp = null;
        try {
            temp = lines[3].split(":");
            if (plugin.config.debug) {
                plugin.Logger("Line 3 is: " + lines[3], "Debug");
            }
        } catch (Exception e) {
            if (plugin.config.debug) {
                plugin.Logger("Contains no : ", "Debug");
            }
        }
        try {
            if (Tools.isFloat(temp[0]) && Tools.isFloat(temp[1])) {
                if (plugin.config.debug) {
                    plugin.Logger("Buy and sell amount are ints: " + temp[0] + " und " + temp[1], "Debug");
                }
                if (Float.parseFloat(temp[0]) > 0 || Float.parseFloat(temp[1]) > 0) {
                    if (plugin.config.debug) {
                        plugin.Logger("One of them is greater than 0: " + temp[0] + " und " + temp[1], "Debug");
                    }
                    if (!(Float.parseFloat(temp[0]) < 0) && !(Float.parseFloat(temp[1]) < 0)) {
                        if (plugin.config.debug) {
                            plugin.Logger("None of them is smaller than 0: " + temp[0] + " und " + temp[1], "Debug");
                        }
                        if (Tools.isInteger(lines[2])) {
                            if (Integer.parseInt(lines[2]) > 0) {
                                if (plugin.config.debug) {
                                    plugin.Logger("Line 2 is int", "Debug");
                                }
                                a = true;
                                if (plugin.config.debug) {
                                    plugin.Logger("block is valid!", "Debug");
                                }
                            }
                        }
                    } else {
                        if (plugin.config.debug) {
                            plugin.Logger("One of them is smaller than 0: " + temp[0] + " und " + temp[1], "Debug");
                        }
                    }
                } else {
                    if (plugin.config.debug) {
                        plugin.Logger("None of them is greater than 0: " + temp[0] + " und " + temp[1], "Debug");
                    }
                }
            } else {
                if (plugin.config.debug) {
                    plugin.Logger("!Tools.isFloat(temp[0]) || !Tools.isFloat(temp[1])", "Debug");
                }
            }
        } catch (Exception ew) {
        }

        return a;
    }

    public boolean SafeIsValid(Sign sign) {
        boolean a = false;
        if (plugin.config.debug) {
            plugin.Logger("Checking if Safe is valid!", "Debug");
        }
        String[] lines = sign.getLines();
        try {
            if (Tools.isInteger(lines[3])) {
                if (plugin.config.debug) {
                    plugin.Logger("line 3 is int: " + lines[3], "Debug");
                }
                a = true;
            } else {
                if (plugin.config.debug) {
                    plugin.Logger("line 3 isnt a integer", "Debug");
                }
            }
        } catch (Exception ew) {
        }

        return a;
    }

    public boolean SafeIsValid(String[] sign) {
        boolean a = false;
        if (plugin.config.debug) {
            plugin.Logger("Checking if Safe is valid!", "Debug");
        }
        try {
            if (Tools.isInteger(sign[3])) {
                if (plugin.config.debug) {
                    plugin.Logger("line 3 is int: " + sign[3], "Debug");
                }
                a = true;
            } else {
                if (plugin.config.debug) {
                    plugin.Logger("line 3 isnt a integer", "Debug");
                }
            }
        } catch (Exception ew) {
        }

        return a;
    }
}