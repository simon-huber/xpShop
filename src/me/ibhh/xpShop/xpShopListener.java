package me.ibhh.xpShop;

import com.griefcraft.scripting.ModuleLoader;
import java.io.File;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class xpShopListener implements Listener {

    private final xpShop plugin;
    private InteractHandler interactHandler;
    private CreateHandler createHandler;
    private String[] split;
    private static final BlockFace[] shopFaces = {BlockFace.SELF, BlockFace.DOWN, BlockFace.UP, BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH};

    public xpShopListener(xpShop xpShop) {
        this.plugin = xpShop;
        interactHandler = new InteractHandler(plugin);
        createHandler = new CreateHandler(plugin);
        xpShop.getServer().getPluginManager().registerEvents(this, xpShop);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void join(PlayerJoinEvent event) {
        if (!plugin.toggle) {
            Player player = event.getPlayer();
            plugin.Logger("Player " + event.getPlayer().getTotalExperience() + " XP: " + event.getPlayer().getTotalExperience() + " Level: " + event.getPlayer().getLevel(), "Debug");
//            double y = 1.75 * (event.getPlayer().getLevel() + event.getPlayer().getExp()) * (event.getPlayer().getLevel() + event.getPlayer().getExp()) + 4.9997 * (event.getPlayer().getLevel() + event.getPlayer().getExp()) + 0.1327;
//            event.getPlayer().setTotalExperience((int) y);
            double t = plugin.getLevelXP(player.getLevel()) + (plugin.nextLevelAt(player.getLevel()) * player.getExp());
            player.setTotalExperience((int) t);
            plugin.Logger("After calculating: Player " + event.getPlayer().getTotalExperience() + " XP: " + event.getPlayer().getTotalExperience() + " Level: " + event.getPlayer().getLevel(), "Debug");
            if (plugin.config.usedbtomanageXP) {
                String playername;
                double XP = player.getTotalExperience();
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
                    if (plugin.updateaviable) {
                        plugin.PlayerLogger(event.getPlayer(), "installed xpShop version: " + plugin.Version + ", latest version: " + plugin.newversion, "Warning");
                        plugin.PlayerLogger(event.getPlayer(), "New xpShop update aviable: type \"/xpShop update\" to install!", "Warning");
                        if (!plugin.getConfig().getBoolean("installondownload")) {
                            plugin.PlayerLogger(event.getPlayer(), "Please edit the config.yml if you wish that the plugin updates itself atomatically!", "Warning");
                        }
                    }
                    File file = new File("plugins" + File.separator + "xpShop" + File.separator + "debug.txt");
                    if (file.exists()) {
                        if (file.length() > 100000000) {
                            plugin.PlayerLogger(event.getPlayer(), "debug.txt is " + file.length() + "Byte big!", "Warning");
                            plugin.PlayerLogger(event.getPlayer(), "Type /xpShop deletedebug to delete the debug.txt!", "Warning");
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void Verzaubern(PlayerLevelChangeEvent event) {
        if (!plugin.toggle) {
            plugin.Logger("Players Level changed: " + event.getPlayer().getName() + " XP: " + event.getPlayer().getTotalExperience() + " Level: " + event.getPlayer().getLevel(), "Debug");
            final Player player = event.getPlayer();
            double t = plugin.getLevelXP(player.getLevel()) + (plugin.nextLevelAt(player.getLevel()) * player.getExp());
            plugin.Logger("Players Level changed: new XP: " + t, "Debug");
            player.setTotalExperience((int) t);
            if (plugin.config.usedbtomanageXP) {

                final String playername = player.getName();
                plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

                    @Override
                    public void run() {
                        plugin.Logger("Saving new XP!", "Debug");
                        double XP;
                        XP = player.getTotalExperience();
                        plugin.SQL.UpdateXP(playername, (int) XP);
                        try {
                            plugin.Logger("Player updated into db: " + playername + "With " + player.getTotalExperience() + " XP! DB: " + plugin.SQL.getXP(playername), "Debug");
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
            if (event.getMessage().toLowerCase().startsWith(("/xpshop".toLowerCase()))) {
                if (plugin.config.debugfile) {
                    plugin.Loggerclass.log("Player: " + event.getPlayer().getName() + " command: " + event.getMessage());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void change(PlayerExpChangeEvent event) {
        if (!plugin.toggle) {
            if (plugin.config.debug) {
                plugin.Logger("Players XP changed: " + event.getPlayer().getName() + " XP: " + event.getPlayer().getTotalExperience() + " Level: " + event.getPlayer().getLevel(), "Debug");
            }
            if (plugin.config.usedbtomanageXP) {

                final Player player = event.getPlayer();
                final String playername = player.getName();
                plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

                    @Override
                    public void run() {
                        plugin.Logger("Saving new XP!", "Debug");
                        double XP;
                        XP = player.getTotalExperience();
                        plugin.SQL.UpdateXP(playername, (int) XP);
                        try {
                            plugin.Logger("Player updated into db: " + playername + "With " + player.getTotalExperience() + " XP! DB: " + plugin.SQL.getXP(playername), "Debug");
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
            Player player = (Player) event.getEntity();
            plugin.Logger("Player: " + player.getName() + " respawned!", "Debug");
            if (plugin.config.keepxpondeath) {
                plugin.Logger("Keeping XP!", "Debug");
                if (plugin.config.usedbtomanageXP) {
                    plugin.Logger("db used!", "Debug");
                    if (player != null) {
                        player.setExp(0);
                        player.setLevel(0);
                        try {
                            plugin.UpdateXP(player, plugin.SQL.getXP(player.getName()), "respawn");
                            plugin.Logger("Successfully saved XP in db!", "Debug");
                        } catch (SQLException ex) {
                            Logger.getLogger(xpShopListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } else {
                    plugin.Logger("Not using db!", "Debug");
                    event.setKeepLevel(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void death(PlayerDeathEvent event) {
        if (!plugin.toggle) {
            Player player = (Player) event.getEntity();
            plugin.Logger("Player: " + player.getName() + " died!", "Debug");
            if (plugin.config.usedbtomanageXP) {
                plugin.Logger("using db!", "Debug");
                if (plugin.config.keepxpondeath) {
                    plugin.Logger("Keeping Levels!", "Debug");
                    double XP;
                    event.setKeepLevel(true);
                    XP = player.getTotalExperience();
                    plugin.SQL.UpdateXP(player.getName(), (int) XP);
                } else {
                    plugin.Logger("Not keeping XP!", "Debug");
                    plugin.Logger("saving new XP in db!", "Debug");
                    double XP = player.getTotalExperience();
                    plugin.UpdateXP(player, -((int) XP), "death");
                    plugin.SQL.UpdateXP(player.getName(), (int) 0);
                    try {
                        plugin.Logger("Player updated into db: " + player.getName() + " With " + player.getTotalExperience() + " XP! DB: " + plugin.SQL.getXP(player.getName()), "Debug");
                    } catch (SQLException ex) {
                        Logger.getLogger(xpShopListener.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                plugin.Logger("Not using db!", "Debug");
                if (plugin.config.keepxpondeath) {
                    plugin.Logger("keeping XP!", "Debug");
                    event.setKeepLevel(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void kick(PlayerKickEvent event) {
        if (!plugin.toggle) {
            if (plugin.config.usedbtomanageXP) {
                plugin.Logger("Player " + event.getPlayer().getName() + " kicked!", "Debug");
                plugin.Logger("Using DB!", "Debug");
                final Player player = event.getPlayer();
                final String playername = player.getName();
                plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

                    @Override
                    public void run() {
                        plugin.Logger("Saving new XP!", "Debug");
                        double XP;
                        XP = player.getTotalExperience();
                        plugin.SQL.UpdateXP(playername, (int) XP);
                        try {
                            plugin.Logger("Player updated into db: " + playername + "With " + player.getTotalExperience() + " XP! DB: " + plugin.SQL.getXP(player.getName()), "Debug");
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
                        XP = player.getTotalExperience();
                        try {
                            int temp = plugin.SQL.getXP(playername);
                            if (XP != temp) {
                                plugin.Logger("Updating XP after quit because some differences!", "Debug");
                                plugin.Logger("Difference: Player: " + XP + " Player: " + temp, "Debug");
                                plugin.SQL.UpdateXP(playername, (int) XP);
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger(xpShopListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        try {
                            plugin.Logger("Player updated into db: " + playername + "With " + player.getTotalExperience() + " XP! DB: " + plugin.SQL.getXP(playername), "Debug");
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
        if (!plugin.toggle) {
            if (plugin.config.debug) {
                plugin.Logger("First Line " + event.getLine(0), "Debug");
            }
            if (event.getLine(0).equalsIgnoreCase("[xpShop]")) {
                createHandler.CreatexpShop(event);
            } else if (event.getLine(0).equalsIgnoreCase("[xpShopSafe]")) {
                createHandler.CreateSafe(event);
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

    public Block[] DrueberDrunter(Block b) {
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
                if (plugin.config.debug) {
                    plugin.Logger("Block dedected", "Debug");
                }
                org.bukkit.block.Sign sign = findSignxp(event.getBlock(), p.getName());
                if (isCorrectSign(sign, event.getBlock())) {
                    if (sign.getLine(0).equalsIgnoreCase("[xpShop]")) {
                        if (!plugin.Blacklistcode.startsWith("1", 12)) {
                            String[] line = sign.getLines();
                            if (blockIsValid(sign)) {
                                if (line[1].equalsIgnoreCase(p.getName()) && plugin.PermissionsHandler.checkpermissions(p, "xpShop.create.own")) {
                                    plugin.PlayerLogger(p, "Destroying xpShop!", "");
                                    MTLocation loc = MTLocation.getMTLocationFromLocation(sign.getLocation());
                                    if (plugin.metricshandler.Shop.containsKey(loc)) {
                                        plugin.metricshandler.Shop.remove(loc);
                                        plugin.Logger("Removed Shop from list!", "Debug");
                                    }
                                } else if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.create.admin")) {
                                    plugin.PlayerLogger(p, "Destroying xpShop (Admin)!", "");
                                    MTLocation loc = MTLocation.getMTLocationFromLocation(sign.getLocation());
                                    if (plugin.metricshandler.Shop.containsKey(loc)) {
                                        plugin.metricshandler.Shop.remove(loc);
                                        plugin.Logger("Removed Shop from list!", "Debug");
                                    }
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
                                    MTLocation loc = MTLocation.getMTLocationFromLocation(sign.getLocation());
                                    if (plugin.metricshandler.Safe.containsKey(loc)) {
                                        plugin.metricshandler.Safe.remove(loc);
                                        plugin.Logger("Removed Safe from list!", "Debug");
                                    }
                                } else if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.admin")) {
                                    plugin.PlayerLogger(p, "Destroying xpShopSafe (Admin)!", "");
                                    MTLocation loc = MTLocation.getMTLocationFromLocation(sign.getLocation());
                                    if (plugin.metricshandler.Safe.containsKey(loc)) {
                                        plugin.metricshandler.Safe.remove(loc);
                                        plugin.Logger("Removed Safe from list!", "Debug");
                                    }
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
                                MTLocation loc = MTLocation.getMTLocationFromLocation(s.getLocation());
                                if (plugin.metricshandler.Shop.containsKey(loc)) {
                                    plugin.metricshandler.Shop.remove(loc);
                                    plugin.Logger("Removed Shop from list!", "Debug");
                                }
                            } else if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.create.admin")) {
                                plugin.PlayerLogger(p, "Destroying xpShop (Admin)!", "");
                                MTLocation loc = MTLocation.getMTLocationFromLocation(s.getLocation());
                                if (plugin.metricshandler.Shop.containsKey(loc)) {
                                    plugin.metricshandler.Shop.remove(loc);
                                    plugin.Logger("Removed Shop from list!", "Debug");
                                }
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
                                MTLocation loc = MTLocation.getMTLocationFromLocation(s.getLocation());
                                if (plugin.metricshandler.Safe.containsKey(loc)) {
                                    plugin.metricshandler.Safe.remove(loc);
                                    plugin.Logger("Removed Safe from list!", "Debug");
                                }
                            } else if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.admin")) {
                                plugin.PlayerLogger(p, "Destroying xpShopSafe (Admin)!", "");
                                MTLocation loc = MTLocation.getMTLocationFromLocation(s.getLocation());
                                if (plugin.metricshandler.Safe.containsKey(loc)) {
                                    plugin.metricshandler.Safe.remove(loc);
                                    plugin.Logger("Removed Safe from list!", "Debug");
                                }
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
        interactHandler.InteracteventHandler(event);
    }

    public double getPrice(Sign s, Player p, boolean buy) {
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
