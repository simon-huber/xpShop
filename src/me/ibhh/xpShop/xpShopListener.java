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
import org.bukkit.event.block.BlockPlaceEvent;
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
        if (!plugin.toggle) {
            if (plugin.config.usedbtomanageXP) {
                String playername;
                Player player = event.getPlayer();
                double XP = plugin.getTOTALXP(player);
                double XPneu = XP;
                playername = player.getName();
                plugin.Logger("Playername (joined): " + playername, "Debug");
                try {
                    if (plugin.SQL.isindb(playername)) {
                        plugin.Logger("Playername is in db: " + playername + "With " + XP + " XP! DB: " + plugin.SQL.getXP(playername), "Debug");
                        XPneu = plugin.SQL.getXP(playername);
                    } else {
                        plugin.SQL.InsertAuction(playername, (int) XP);
                        XPneu = plugin.SQL.getXP(playername);
                        plugin.Logger("Playername insert into db: " + playername + "With " + XP + " XP! DB now: " + XPneu, "Debug");
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
                    plugin.UpdateXP(player, (int) XPneu, "Join");
                    player.saveData();
                }
            }
            if (!plugin.Blacklistcode.startsWith("1")) {
                if (plugin.PermissionsHandler.checkpermissionssilent(event.getPlayer(), "xpShop.admin")) {
                    plugin.Logger("Player: " + event.getPlayer().getName() + " has permission: \"xpShop.admin\"", "Debug");
                    if (plugin.updateaviable) {
                        plugin.PlayerLogger(event.getPlayer(), "New xpShop update aviable: type \"xpShopupdate\" please!", "Warning");
                    }
                } else {
                    plugin.Logger("Player: " + event.getPlayer().getName() + " has no permission: \"xpShop.admin\"", "Debug");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void Verzaubern(PlayerLevelChangeEvent event) {
        if (!plugin.toggle) {
            if (plugin.config.usedbtomanageXP) {
                final Player player = event.getPlayer();
                final String playername = player.getName();
                plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

                    @Override
                    public void run() {
                        plugin.Logger("Saving new XP!", "Debug");
                        double XP;
                        XP = plugin.getTOTALXP(player);
                        plugin.SQL.UpdateXP(playername, (int) XP);
                        try {
                            plugin.Logger("Player updated into db: " + playername + "With " + plugin.getTOTALXP(player) + " XP! DB: " + plugin.SQL.getXP(playername), "Debug");
                        } catch (SQLException ex) {
                            Logger.getLogger(xpShopListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }, 0);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void precommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.toggle) {
            if (event.getMessage().startsWith(("/xpshop").toLowerCase())) {
                if (plugin.config.debugfile) {
                    plugin.Loggerclass.log("Playercommand: " + event.getMessage());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void change(PlayerExpChangeEvent event) {
        if (!plugin.toggle) {
            if (plugin.config.usedbtomanageXP) {

                final Player player = event.getPlayer();
                final String playername = player.getName();
                plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

                    @Override
                    public void run() {
                        plugin.Logger("Saving new XP!", "Debug");
                        double XP;
                        XP = plugin.getTOTALXP(player);
                        plugin.SQL.UpdateXP(playername, (int) XP);
                        try {
                            plugin.Logger("Player updated into db: " + playername + "With " + plugin.getTOTALXP(player) + " XP! DB: " + plugin.SQL.getXP(playername), "Debug");
                        } catch (SQLException ex) {
                            Logger.getLogger(xpShopListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }, 0);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void resp(PlayerDeathEvent event) {
        if (!plugin.toggle) {
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
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void death(PlayerDeathEvent event) {
        if (!plugin.toggle) {
            if (plugin.config.usedbtomanageXP) {
                if (plugin.config.keepxpondeath) {
                    double XP;
                    Player player = (Player) event.getEntity();
                    event.setKeepLevel(true);
                    XP = plugin.getTOTALXP(player);
                    plugin.SQL.UpdateXP(player.getName(), (int) XP);
                } else {
                    Player player = (Player) event.getEntity();
                    double XP = plugin.getTOTALXP(player);
                    plugin.UpdateXP(player, -((int) XP), "death");
                    plugin.SQL.UpdateXP(player.getName(), (int) 0);
                    try {
                        plugin.Logger("Player updated into db: " + player.getName() + " With " + plugin.getTOTALXP(player) + " XP! DB: " + plugin.SQL.getXP(player.getName()), "Debug");
                    } catch (SQLException ex) {
                        Logger.getLogger(xpShopListener.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            } else {
                if (plugin.config.keepxpondeath) {
                    event.setKeepLevel(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void kick(PlayerKickEvent event) {
        if (!plugin.toggle) {
            if (plugin.config.usedbtomanageXP) {
                final Player player = event.getPlayer();
                final String playername = player.getName();
                plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

                    @Override
                    public void run() {
                        plugin.Logger("Saving new XP!", "Debug");
                        double XP;
                        XP = plugin.getTOTALXP(player);
                        plugin.SQL.UpdateXP(playername, (int) XP);
                        try {
                            plugin.Logger("Player updated into db: " + playername + "With " + plugin.getTOTALXP(player) + " XP! DB: " + plugin.SQL.getXP(player.getName()), "Debug");
                        } catch (SQLException ex) {
                            Logger.getLogger(xpShopListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }, 2L);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void quit(PlayerQuitEvent event) {
        if (!plugin.toggle) {
            if (plugin.config.usedbtomanageXP) {
                final Player player = event.getPlayer();
                final String playername = player.getName();
                plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

                    @Override
                    public void run() {
                        plugin.Logger("Saving new XP after quit!", "Debug");
                        double XP;
                        XP = plugin.getTOTALXP(player);
                        try {
                            if (XP != plugin.SQL.getXP(playername)) {
                                plugin.Logger("Updating XP after quit because some differences!", "Debug");
                                plugin.SQL.UpdateXP(playername, (int) XP);
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger(xpShopListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        try {
                            plugin.Logger("Player updated into db: " + playername + "With " + plugin.getTOTALXP(player) + " XP! DB: " + plugin.SQL.getXP(playername), "Debug");
                        } catch (SQLException ex) {
                            Logger.getLogger(xpShopListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }, 2L);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void aendern(SignChangeEvent event) {
        if (plugin.toggle) {
            Player p = event.getPlayer();
            String[] line = event.getLines();
            plugin.Logger("First Line " + line[0], "Debug");
            if (event.getLine(0).equalsIgnoreCase("[xpShop]")) {
                plugin.Logger("First Line [xpShop]", "Debug");
                if (!plugin.Blacklistcode.startsWith("1", 10)) {
                    plugin.Logger(plugin.Blacklistcode, "Debug");
                    try {
                        if (blockIsValid(line, "create", p)) {
                            plugin.Logger("Sign is valid", "Debug");
                            if (!line[1].equalsIgnoreCase("AdminShop") && line[1].length() < 16) {
                                if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.create.own")) {
                                    plugin.Logger("First line != null", "Debug");
                                    event.setLine(1, event.getPlayer().getName());
                                    plugin.PlayerLogger(event.getPlayer(), "Successfully created xpShop!", "");
                                    if (plugin.config.ConnectionofSafetoShop) {
                                        Block eventblock = event.getBlock();
                                        if (eventblock != null) {
                                            Block[] b = DrueberDrunter(eventblock);
                                            Sign sign = getCorrectsafeSign(b);
                                            if (sign != null) {
                                                if (sign.getLine(0).equalsIgnoreCase("[xpShopSafe]")) {
                                                }
                                            } else {
                                                plugin.PlayerLogger(event.getPlayer(), "Please add a Safe under or above to the Shop!", "Warning");
                                                plugin.PlayerLogger(event.getPlayer(), "Else it wont work!", "Warning");
                                            }
                                        }
                                    }

                                } else {
                                    plugin.Logger("Player " + p.getName() + " has no permission: xpShop.create", "Debug");
                                    plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed!", "Error");
                                    event.setCancelled(true);
                                }
                            } else if (line[1].equalsIgnoreCase(p.getName()) && line[1].length() < 16) {
                                if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.create.own")) {
                                    plugin.PlayerLogger(event.getPlayer(), "Successfully created xpShop!", "");
                                    if (plugin.config.ConnectionofSafetoShop) {
                                        Block eventblock = event.getBlock();
                                        if (eventblock != null) {
                                            Block[] b = DrueberDrunter(eventblock);
                                            Sign sign = getCorrectsafeSign(b);
                                            if (sign != null) {
                                                if (sign.getLine(0).equalsIgnoreCase("[xpShopSafe]")) {
                                                }
                                            } else {
                                                plugin.PlayerLogger(event.getPlayer(), "Please add a Safe under or above to the Shop!", "Warning");
                                                plugin.PlayerLogger(event.getPlayer(), "Else it wont work!", "Warning");
                                            }
                                        }
                                    }

                                    event.setLine(0, "[xpShop]");
                                } else {
                                    plugin.Logger("Player " + p.getName() + " has no permission: xpShop.create", "Debug");
                                    plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed!", "Error");
                                    event.setCancelled(true);
                                }
                            } else if (line[1].equalsIgnoreCase("AdminShop")) {
                                if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.create.admin")) {
                                    plugin.Logger("Player " + p.getName() + " has permission: xpShop.create.admin", "Debug");
                                    plugin.PlayerLogger(event.getPlayer(), "Successfully created xpShop!", "");
                                } else {
                                    plugin.Logger("Player " + p.getName() + " has no permission: xpShop.create.admin", "Debug");
                                    plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed!", "Error");
                                    event.setCancelled(true);
                                }
                            } else if (line[1].length() >= 16) {
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
            } else if (line[0].equalsIgnoreCase("[xpShopSafe]")) {
                plugin.Logger("First Line [xpShopSafe]", "Debug");
                if (!plugin.Blacklistcode.startsWith("1", 10)) {
                    plugin.Logger(plugin.Blacklistcode, "Debug");
                    if (SafeIsValid(line)) {
                        plugin.Logger("Safe is valid", "Debug");
                        if (line[1].equalsIgnoreCase(p.getName()) && line[1].length() < 16) {
                            if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.safe.create")) {
                                plugin.PlayerLogger(event.getPlayer(), "Successfully created xpShopSafe!", "");
                                event.setLine(0, "[xpShopSafe]");
                                event.setLine(2, "0");
                            } else {
                                plugin.Logger("Player " + p.getName() + " has no permission: xpShop.safe.create", "Debug");
                                plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed!", "Error");
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
                                plugin.PlayerLogger(event.getPlayer(), "Successfully created xpShop!", "");
                            } else {
                                plugin.Logger("Player " + p.getName() + " has no permission: xpShop.create", "Debug");
                                plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed!", "Error");
                                event.setCancelled(true);
                            }
                        }
                    } else {
                        plugin.Logger("Sign is not valid", "Debug");
                        plugin.PlayerLogger(event.getPlayer(), "xpShop creation failed!", "Error");
                        event.setCancelled(true);
                    }
                } else {
                    plugin.Logger(plugin.Blacklistcode, "Debug");
                    plugin.blacklistLogger(p);
                    event.setCancelled(true);
                }
            }
        }
    }

    public static Block getAttachedFace(org.bukkit.block.Sign sign) {
        return sign.getBlock().getRelative(((org.bukkit.material.Sign) sign.getData()).getAttachedFace());
    }

    private static boolean isCorrectSign(org.bukkit.block.Sign sign, Block block) {
        return (sign != null) && ((sign.getBlock().equals(block)) || (getAttachedFace(sign).equals(block)));
    }

    public static boolean isSign(Block block) {
        return block.getState() instanceof Sign;
    }

    public Sign findSign(Block block) {
        for (BlockFace bf : shopFaces) {
            Block faceBlock = block.getRelative(bf);
            plugin.Logger("Blockface : Y: " + faceBlock.getY() + " X: " + faceBlock.getX() + "Z: " + faceBlock.getZ(), "Debug");
            if (isSign(faceBlock)) {
                Sign sign = (Sign) faceBlock.getState();
                plugin.Logger("Sign found!", "Debug");
                plugin.Logger("Block Sign : Y: " + sign.getY() + " X: " + sign.getX() + "Z: " + sign.getZ(), "Debug");

                Sign s = (Sign) block.getState();
                if (sign.getLine(0).equalsIgnoreCase("[xpShopSafe]") && s.getLine(1).equalsIgnoreCase(sign.getLine(1))) {
                    plugin.Logger("Safe found!", "Debug");
                    plugin.Logger("Block 1 : Y: " + sign.getY() + " X: " + sign.getX() + "Z: " + sign.getZ(), "Debug");
                    return sign;
                }
            }
        }
//        for (BlockFace bf : shopFaces) {
//            Block faceBlock = block.getRelative(bf);
//            if (isSign(faceBlock)) {
//                Sign sign = (Sign) faceBlock.getState();
//                if (sign.getLine(0).equalsIgnoreCase("[xpShopSafe]") && ((faceBlock.equals(block)) || (getAttachedFace(sign).equals(block)))) {
//                    return sign;
//                }
//            }
//        }
        return null;
    }

    private Block[] DrueberDrunter(Block b) {
        plugin.Logger("Sign : Y: " + b.getY() + " X: " + b.getX() + "Z: " + b.getZ(), "Debug");
        Block[] xy = new Block[2];
        if (b.getRelative(BlockFace.UP) != null) {
            xy[0] = b.getRelative(BlockFace.UP);
            plugin.Logger("Block above dedected!", "Debug");
        }
        if (b.getRelative(BlockFace.DOWN) != null) {
            xy[1] = b.getRelative(BlockFace.DOWN);
            plugin.Logger("Block down dedected!", "Debug");
        }
        return xy;
    }

    public Sign findSignxp(Block block, String originalName) {
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

    public Sign getCorrectsafeSign(Block[] sign) {
        if (sign == null) {
            plugin.Logger("block == null!", "Debug");
            return null;
        }
        Sign r = null;
        plugin.Logger("GetCorrectSafe!", "Debug");

        if (sign[0].getState() instanceof Sign || sign[1].getState() instanceof Sign) {
            if (sign[0].getState() instanceof Sign) {
                r = (Sign) sign[0];
                plugin.Logger("sign0 is sign!", "Debug");
            } else {
                r = (Sign) sign[1];
                plugin.Logger("sign1 is sign!", "Debug");
            }
            if (!r.getLine(0).equalsIgnoreCase("[xpShopSafe]")) {
                plugin.Logger("Sign line not xpShopSafe!", "Debug");
                return null;
            }
        } else {
            plugin.Logger("No Safe found!", "Debug");
            plugin.Logger("Block 1 : Y: " + sign[0].getY() + " X: " + sign[0].getX() + "Z: " + sign[0].getZ(), "Debug");
            plugin.Logger("Block 1 : Y: " + sign[1].getY() + " X: " + sign[1].getX() + "Z: " + sign[1].getZ(), "Debug");
        }
        if (sign[0].getState() instanceof Sign && sign[1].getState() instanceof Sign) {
            plugin.Logger("Two sign are dedected!", "Debug");
            Sign r1 = (Sign) sign[0];
            Sign r2 = (Sign) sign[1];
            if (r1.getLine(0).equalsIgnoreCase("[xpShopSafe]") && r2.getLine(0).equalsIgnoreCase("[xpShopSafe]")) {
                return null;
            } else if (r1.getLine(0).equalsIgnoreCase("[xpShopSafe]")) {
                r = (Sign) sign[0];
            } else if (r2.getLine(0).equalsIgnoreCase("[xpShopSafe]")) {
                r = (Sign) sign[1];
            }
        }
        return r;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBreak(BlockBreakEvent event) {
        if (!plugin.toggle) {
            Player p = event.getPlayer();
            if (!(event.getBlock().getState() instanceof Sign)) {
                plugin.Logger("Block dedected", "Debug");
                org.bukkit.block.Sign sign = findSignxp(event.getBlock(), p.getName());
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
                plugin.Logger("Line 0: " + line[0], "Debug");
                plugin.Logger("Sign dedected", "Debug");
                if (line[0].equalsIgnoreCase("[xpShop]")) {
                    if (!plugin.Blacklistcode.startsWith("1", 12)) {
                        if (this.blockIsValid(line, "break", p)) {
                            if (!plugin.PermissionsHandler.checkpermissions(p, "xpShop.create.own") && !plugin.PermissionsHandler.checkpermissions(p, "xpShop.create.admin")) {
                                event.setCancelled(true);
                            } else if (s.getLine(1).equalsIgnoreCase(p.getName()) && plugin.PermissionsHandler.checkpermissions(p, "xpShop.create.own")) {
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
                } else if (line[0].equalsIgnoreCase("[xpShopSafe]")) {
                    if (!plugin.Blacklistcode.startsWith("1", 12)) {
                        if (SafeIsValid(s)) {
                            if (line[1].equalsIgnoreCase(p.getName()) && plugin.PermissionsHandler.checkpermissions(p, "xpShop.safe.create")) {
                                plugin.PlayerLogger(p, "Destroying xpShopSafe!", "");
                                plugin.UpdateXP(p, Integer.parseInt(line[2]), "destroy");
                                if (plugin.config.usedbtomanageXP) {
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
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (!plugin.toggle) {
            Player p = event.getPlayer();
            plugin.Logger("A interact Event dected by player: " + p.getName(), "Debug");
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                LeftInteract(event);
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                RightInteract(event);
            }
        }
    }

    public void xpShopSign(PlayerInteractEvent event, String[] line, Player p, Sign s) {
        if (!plugin.Blacklistcode.startsWith("1", 11)) {
            Player player = p;
            String playername = p.getName();
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
                        if (plugin.config.ConnectionofSafetoShop) {
                            Sign sign = findSign(event.getClickedBlock());

                            if (sign != null) {
                                plugin.Logger("Safe:!", "Debug");
                                plugin.Logger("Block sign : Y: " + sign.getY() + " X: " + sign.getX() + "Z: " + sign.getZ(), "Debug");
                                plugin.Logger("sign != null!", "Debug");
                                if (sign.getLine(0).equalsIgnoreCase("[xpShopSafe]")) {
                                    plugin.Logger("Safe found!", "Debug");
                                    if (plugin.config.usedbtomanageXP) {
                                        try {
                                            if (!playername.equalsIgnoreCase(line[1])) {
                                                int XPPlayer = plugin.SQL.getXP(playername);
                                                if (plugin.SQL.getXP(playername) >= Integer.parseInt(line[2])) {
                                                    if (getPrice(s, p, false) > 0) {
                                                        double price = getPrice(s, p, false);
                                                        if ((plugin.MoneyHandler.getBalance(line[1]) - price) >= 0) {
                                                            plugin.MoneyHandler.substract(price, line[1]);
                                                            plugin.MoneyHandler.addmoney(price, p);
                                                            plugin.UpdateXP(p, -(Integer.parseInt(line[2])), "Sign");
                                                            plugin.SQL.UpdateXP(playername, XPPlayer - Integer.parseInt(line[2]));
                                                            int Erg = Integer.parseInt(sign.getLine(2)) + Integer.parseInt(line[2]);
                                                            StringBuilder bui = new StringBuilder();
                                                            bui.append(Erg);
                                                            plugin.Logger("Erg: line2 = " + bui.toString(), "Debug");
                                                            sign.setLine(2, bui.toString());
                                                            sign.update();
                                                            plugin.PlayerLogger(p, String.format(plugin.config.Shopsuccesssell, line[2], line[1], split[1]), "");
                                                            Player empfaenger = plugin.getServer().getPlayer(line[1]);
                                                            if (empfaenger != null) {

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
                                                plugin.PlayerLogger(player, "That is your Shop", "Error");
                                            }
                                        } catch (Exception e) {
                                            plugin.PlayerLogger(player, line[1] + " " + plugin.config.playerwasntonline, "Error");
                                        }
                                    } else {
                                        if (!playername.equalsIgnoreCase(line[1])) {
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
                                                                int Erg = Integer.parseInt(line[2]) + Integer.parseInt(line[2]);
                                                                StringBuilder bui = new StringBuilder();
                                                                bui.append(Erg);
                                                                plugin.Logger("Erg: line2 = " + bui.toString(), "Debug");
                                                                sign.setLine(2, bui.toString());
                                                                sign.update();
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
                                        } else {
                                            plugin.PlayerLogger(player, "That is your Shop", "Error");
                                        }
                                    }

                                } else {
                                    plugin.PlayerLogger(p, plugin.config.safenoSafeonShop, "Error");
                                }
                            } else {
                                plugin.PlayerLogger(p, plugin.config.safenoSafeonShop, "Error");
                            }
                        } else if (plugin.config.usedbtomanageXP) {
                            try {
                                if (!playername.equalsIgnoreCase(line[1])) {
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
                                } else {
                                    plugin.PlayerLogger(player, "That is your Shop", "Error");
                                }
                            } catch (Exception e) {
                                plugin.PlayerLogger(player, line[1] + " " + plugin.config.playerwasntonline, "Error");
                            }
                        } else {
                            if (!playername.equalsIgnoreCase(line[1])) {
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
                            } else {
                                plugin.PlayerLogger(player, "That is your Shop", "Error");
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

    public void xpShopSafeSign(PlayerInteractEvent event, String[] line, Player p, Sign s) {
        if (!plugin.Blacklistcode.startsWith("1", 11)) {
            String playername = p.getName();
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
                        } else {
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
                    } else {
                        plugin.PlayerLogger(p, plugin.config.safenotyoursafe, "");
                    }

                }
            }
        }
    }

    public void RightInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if ((event.hasBlock()) && ((event.getClickedBlock().getState() instanceof Sign)) && (!p.isSneaking())) { // && !(p.isSneaking())
            Sign s = (Sign) event.getClickedBlock().getState();
            String[] line = s.getLines();
            if (line[0].equalsIgnoreCase("[xpShop]")) {
                xpShopSign(event, line, p, s);
            } else if (line[0].equalsIgnoreCase("[xpShopSafe]")) {
                xpShopSafeSign(event, line, p, s);
            }
        }
    }

    public void LeftInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        plugin.Logger("A left interact Event dected by player: " + p.getName(), "Debug");
        if ((event.hasBlock()) && ((event.getClickedBlock().getState() instanceof Sign)) && (!p.isSneaking())) { // && !(p.isSneaking())
            Sign s = (Sign) event.getClickedBlock().getState();
            String[] line = s.getLines();
            String playername = p.getName();
            Player player = p;
            plugin.Logger("Checking first line!", "Debug");
            if (line[0].equalsIgnoreCase("[xpShop]")) {
                plugin.Logger(" first line [xpShop] and leftklick!", "Debug");
                if (!plugin.Blacklistcode.startsWith("1", 11)) {
                    plugin.Logger(" not blacklisted!", "Debug");
                    if (this.blockIsValid(line, "Interact", p)) {
                        plugin.Logger(" Block is valid!", "Debug");
                        if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.use")) {
                            plugin.Logger("Player: " + p.getName() + " has the permission: xpShop.use", "Debug");
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
                                if (plugin.config.ConnectionofSafetoShop) {
                                    Sign sign = findSign(event.getClickedBlock());
                                    if (sign != null) {
                                        plugin.Logger("sign != null!", "Debug");
                                        if (sign.getLine(0).equalsIgnoreCase("[xpShopSafe]")) {
                                            plugin.Logger("Safe found!", "Debug");
                                            if (plugin.config.usedbtomanageXP) {
                                                try {
                                                    if (!playername.equalsIgnoreCase(line[1])) {
                                                        if (plugin.SQL.isindb(line[1])) {
                                                            int XPPlayer = plugin.SQL.getXP(playername);
                                                            double xptemp = plugin.getTOTALXP(player);
                                                            if (xptemp != XPPlayer) {
                                                                plugin.SQL.UpdateXP(playername, (int) xptemp);
                                                            }

                                                            if (Integer.parseInt(sign.getLine(2)) >= Integer.parseInt(line[2])) {
                                                                if (getPrice(s, p, true) > 0) {
                                                                    double price = getPrice(s, p, true);
                                                                    if ((plugin.MoneyHandler.getBalance(p) - price) >= 0) {
                                                                        plugin.MoneyHandler.substract(price, p);
                                                                        plugin.MoneyHandler.addmoney(price, line[1]);
                                                                        plugin.UpdateXP(p, Integer.parseInt(line[2]), "Sign");
                                                                        plugin.SQL.UpdateXP(playername, XPPlayer + Integer.parseInt(line[2]));
                                                                        plugin.PlayerLogger(p, String.format(plugin.config.Shopsuccessbuy, s.getLine(2), s.getLine(1), split[0]), "");
                                                                        int Erg = Integer.parseInt(sign.getLine(2)) - Integer.parseInt(line[2]);
                                                                        StringBuilder bui = new StringBuilder();
                                                                        bui.append(Erg);
                                                                        plugin.Logger("Erg: line2 = " + bui.toString(), "Debug");
                                                                        sign.setLine(2, bui.toString());
                                                                        sign.update();
                                                                    } else {
                                                                        plugin.PlayerLogger(p, plugin.config.Shoperrornotenoughmoneyconsumer, "Error");
                                                                    }
                                                                } else {
                                                                    plugin.PlayerLogger(p, plugin.config.Shoperrorcantbuyhere, "Error");
                                                                }
                                                            } else {
                                                                plugin.PlayerLogger(p, plugin.config.safenotenoughXPinShop, "Error");
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
                                            } else {
                                                if (!playername.equalsIgnoreCase(line[1])) {
                                                    Player empfaenger = plugin.getmyOfflinePlayer(line, 1);
                                                    if (empfaenger != null) {
                                                        if (plugin.config.getPlayerConfig(empfaenger, p)) {
                                                            if (Integer.parseInt(sign.getLine(2)) >= Integer.parseInt(line[2])) {
                                                                if (getPrice(s, p, true) > 0) {
                                                                    double price = getPrice(s, p, true);
                                                                    if ((plugin.MoneyHandler.getBalance(p) - price) >= 0) {
                                                                        plugin.MoneyHandler.substract(price, p);
                                                                        plugin.UpdateXP(p, (Integer.parseInt(s.getLine(2))), "Sign");
                                                                        plugin.PlayerLogger(p, String.format(plugin.config.Shopsuccessbuy, s.getLine(2), s.getLine(1), split[0]), "");
                                                                        plugin.UpdateXP(empfaenger, -(Integer.parseInt(s.getLine(2))), "Sign");
                                                                        plugin.MoneyHandler.addmoney(price, empfaenger);
                                                                        empfaenger.saveData();
                                                                        int Erg = Integer.parseInt(sign.getLine(2)) - Integer.parseInt(line[2]);
                                                                        StringBuilder bui = new StringBuilder();
                                                                        bui.append(Erg);
                                                                        plugin.Logger("Erg: line2 = " + bui.toString(), "Debug");
                                                                        sign.setLine(2, bui.toString());
                                                                        sign.update();
                                                                        plugin.PlayerLogger(empfaenger, String.format(plugin.config.Shopsuccesssellerbuy, s.getLine(2), p.getName(), split[0]), "");
                                                                    } else {
                                                                        plugin.PlayerLogger(p, plugin.config.Shoperrornotenoughmoneyconsumer, "Error");
                                                                    }
                                                                } else {
                                                                    plugin.PlayerLogger(p, plugin.config.Shoperrorcantbuyhere, "Error");
                                                                }
                                                            } else {
                                                                plugin.PlayerLogger(p, plugin.config.safenotenoughXPinShop, "Error");
                                                            }
                                                        }
                                                    } else {
                                                        plugin.PlayerLogger(p, line[1] + " " + plugin.config.playerwasntonline, "Error");
                                                    }
                                                } else {
                                                    plugin.PlayerLogger(player, "That is your Shop", "Error");
                                                }

                                            }
                                        } else {
                                            plugin.PlayerLogger(p, plugin.config.safenoSafeonShop, "Error");
                                        }
                                    } else {
                                        plugin.PlayerLogger(p, plugin.config.safenoSafeonShop, "Error");
                                        plugin.Logger("Sign == null!", "Debug");
                                    }
                                } else if (plugin.config.usedbtomanageXP) {
                                    try {
                                        if (!playername.equalsIgnoreCase(line[1])) {
                                            if (plugin.SQL.isindb(line[1])) {
                                                int XPPlayer = plugin.SQL.getXP(playername);
                                                double xptemp = plugin.getTOTALXP(player);
                                                if (xptemp != XPPlayer) {
                                                    plugin.Logger("Updating XP before buy on Sign because some differences!", "");
                                                    plugin.SQL.UpdateXP(playername, (int) xptemp);
                                                }
                                                int EmpfXP = plugin.SQL.getXP(line[1]);
                                                if (EmpfXP >= Integer.parseInt(line[2])) {
                                                    if (getPrice(s, p, true) > 0) {
                                                        double price = getPrice(s, p, true);
                                                        if ((plugin.MoneyHandler.getBalance(p) - price) >= 0) {
                                                            plugin.MoneyHandler.substract(price, p);
                                                            plugin.MoneyHandler.addmoney(price, line[1]);
                                                            plugin.UpdateXP(p, Integer.parseInt(line[2]), "Sign");
                                                            plugin.SQL.UpdateXP(playername, XPPlayer + Integer.parseInt(line[2]));
                                                            plugin.SQL.UpdateXP(line[1], plugin.SQL.getXP(line[1]) - Integer.parseInt(line[2]));
                                                            plugin.PlayerLogger(p, String.format(plugin.config.Shopsuccessbuy, s.getLine(2), s.getLine(1), split[0]), "");
                                                            if (plugin.getServer().getPlayer(line[1]) != null) {
                                                                Player Empfaenger = plugin.getServer().getPlayer(line[1]);
                                                                Empfaenger.setLevel(0);
                                                                Empfaenger.setExp(0);
                                                                plugin.UpdateXP(Empfaenger, plugin.SQL.getXP(Empfaenger.getName()), "Sign");
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
                                        } else {
                                            plugin.PlayerLogger(player, "That is your Shop", "Error");
                                        }
                                    } catch (Exception e) {
                                        plugin.PlayerLogger(player, line[1] + " " + plugin.config.playerwasntonline, "Error");
                                    }

                                } else {
                                    if (!playername.equalsIgnoreCase(line[1])) {
                                        Player empfaenger = plugin.getmyOfflinePlayer(line, 1);
                                        if (empfaenger != null) {
                                            if (plugin.config.getPlayerConfig(empfaenger, p)) {
                                                if (plugin.getTOTALXP(empfaenger) >= Integer.parseInt(line[2])) {
                                                    if (getPrice(s, p, true) > 0) {
                                                        double price = getPrice(s, p, true);
                                                        if ((plugin.MoneyHandler.getBalance(p) - price) >= 0) {
                                                            plugin.MoneyHandler.substract(price, p);
                                                            plugin.MoneyHandler.addmoney(price, empfaenger);
                                                            plugin.UpdateXP(p, (Integer.parseInt(s.getLine(2))), "Sign");
                                                            plugin.PlayerLogger(p, String.format(plugin.config.Shopsuccessbuy, s.getLine(2), s.getLine(1), split[0]), "");
                                                            plugin.UpdateXP(empfaenger, -(Integer.parseInt(s.getLine(2))), "Sign");

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
                                    } else {
                                        plugin.PlayerLogger(player, "That is your Shop", "Error");
                                    }
                                }
                            }
                        }
                    }
                } else {
                    plugin.blacklistLogger(p);
                    event.setCancelled(true);
                }
            } else if (line[0].equalsIgnoreCase(
                    "[xpShopSafe]")) {
                plugin.Logger(" first line [xpShopSafe] and leftklick!", "Debug");
                if (!plugin.Blacklistcode.startsWith("1", 11)) {
                    plugin.Logger(" not blacklisted!", "Debug");
                    if (this.SafeIsValid(line)) {
                        plugin.Logger(" Safe is valid!", "Debug");
                        if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.safe.use")) {
                            plugin.Logger("Player: " + p.getName() + " has the permission: xpShop.safe.use", "Debug");
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
                                                plugin.Logger("Erg: line2 = " + bui.toString(), "Debug");
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
                                        plugin.Logger("Erg: line2 = " + bui.toString(), "Debug");
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
        plugin.Logger("Checking if block is valid!", "Debug");
        String[] temp = null;
        try {
            temp = lines[3].split(":");
            plugin.Logger("Line 3 is: " + lines[3], "Debug");
        } catch (Exception e) {
            plugin.Logger("Contains no : ", "Debug");
        }
        try {
            if (Tools.isFloat(temp[0]) && Tools.isFloat(temp[1])) {
                plugin.Logger("Buy and sell amount are ints: " + temp[0] + " und " + temp[1], "Debug");
                if (Float.parseFloat(temp[0]) > 0 || Float.parseFloat(temp[1]) > 0) {
                    plugin.Logger("One of them is greater than 0: " + temp[0] + " und " + temp[1], "Debug");
                    if (!(Float.parseFloat(temp[0]) < 0) && !(Float.parseFloat(temp[1]) < 0)) {
                        plugin.Logger("None of them is smaller than 0: " + temp[0] + " und " + temp[1], "Debug");
                        if (Tools.isInteger(lines[2])) {
                            if (Integer.parseInt(lines[2]) > 0) {
                                plugin.Logger("Line 2 is int", "Debug");
                                a = true;
                                plugin.Logger("block is valid!", "Debug");
                            }
                        }
                    } else {
                        plugin.Logger("One of them is smaller than 0: " + temp[0] + " und " + temp[1], "Debug");
                    }
                } else {
                    plugin.Logger("None of them is greater than 0: " + temp[0] + " und " + temp[1], "Debug");
                }
            } else {
                plugin.Logger("!Tools.isFloat(temp[0]) || !Tools.isFloat(temp[1])", "Debug");
            }
        } catch (Exception ew) {
        }

        return a;
    }

    public boolean blockIsValid(Sign sign) {
        boolean a = false;
        plugin.Logger("Checking if block is valid!", "Debug");
        String[] lines = sign.getLines();
        String[] temp = null;
        try {
            temp = lines[3].split(":");
            plugin.Logger("Line 3 is: " + lines[3], "Debug");
        } catch (Exception e) {
            plugin.Logger("Contains no : ", "Debug");
        }
        try {
            if (Tools.isFloat(temp[0]) && Tools.isFloat(temp[1])) {
                plugin.Logger("Buy and sell amount are ints: " + temp[0] + " und " + temp[1], "Debug");
                if (Float.parseFloat(temp[0]) > 0 || Float.parseFloat(temp[1]) > 0) {
                    plugin.Logger("One of them is greater than 0: " + temp[0] + " und " + temp[1], "Debug");
                    if (!(Float.parseFloat(temp[0]) < 0) && !(Float.parseFloat(temp[1]) < 0)) {
                        plugin.Logger("None of them is smaller than 0: " + temp[0] + " und " + temp[1], "Debug");
                        if (Tools.isInteger(lines[2])) {
                            if (Integer.parseInt(lines[2]) > 0) {
                                plugin.Logger("Line 2 is int", "Debug");
                                a = true;
                                plugin.Logger("block is valid!", "Debug");
                            }
                        }
                    } else {
                        plugin.Logger("One of them is smaller than 0: " + temp[0] + " und " + temp[1], "Debug");
                    }
                } else {
                    plugin.Logger("None of them is greater than 0: " + temp[0] + " und " + temp[1], "Debug");
                }
            } else {
                plugin.Logger("!Tools.isFloat(temp[0]) || !Tools.isFloat(temp[1])", "Debug");
            }
        } catch (Exception ew) {
        }

        return a;
    }

    public boolean SafeIsValid(Sign sign) {
        boolean a = false;
        plugin.Logger("Checking if Safe is valid!", "Debug");
        String[] lines = sign.getLines();
        try {
            if (Tools.isInteger(lines[3])) {
                plugin.Logger("line 3 is int: " + lines[3], "Debug");
                a = true;
            } else {
                plugin.Logger("line 3 isnt a integer", "Debug");
            }
        } catch (Exception ew) {
        }

        return a;
    }

    public boolean SafeIsValid(String[] sign) {
        boolean a = false;
        plugin.Logger("Checking if Safe is valid!", "Debug");
        try {
            if (Tools.isInteger(sign[3])) {
                plugin.Logger("line 3 is int: " + sign[3], "Debug");
                a = true;
            } else {
                plugin.Logger("line 3 isnt a integer", "Debug");
            }
        } catch (Exception ew) {
        }

        return a;
    }
}
