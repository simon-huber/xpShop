package me.ibhh.xpShop;

import java.sql.SQLException;
import java.util.ArrayList;

import me.ibhh.xpShop.Tools.Tools;
import me.ibhh.xpShop.send.sql.XPSend;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;

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
            try {
                final Player player = event.getPlayer();
                plugin.Logger("Player " + event.getPlayer().getTotalExperience() + " XP: " + event.getPlayer().getTotalExperience() + " Level: " + event.getPlayer().getLevel(), "Debug");
//            double y = 1.75 * (event.getPlayer().getLevel() + event.getPlayer().getExp()) * (event.getPlayer().getLevel() + event.getPlayer().getExp()) + 4.9997 * (event.getPlayer().getLevel() + event.getPlayer().getExp()) + 0.1327;
//            event.getPlayer().setTotalExperience((int) y);
                double t = plugin.getLevelXP(player.getLevel()) + (xpShop.nextLevelAt(player.getLevel()) * player.getExp());
                player.setTotalExperience((int) t);
                plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
					
					@Override
					public void run() {
						try {
							ArrayList<XPSend> xpSends = plugin.getSendDatabase().getOpenTransactions(player.getName());
							for(XPSend send : xpSends) {
								plugin.PlayerLogger(player, send.getMessage(), "");
								plugin.UpdateXP(player, send.getSendedXP(), "sendxp");
								player.saveData();
								plugin.getSendDatabase().setStatus(player.getName(), send.getId(), 1);
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}, 20);
                plugin.Logger("After calculating: Player " + event.getPlayer().getTotalExperience() + " XP: " + event.getPlayer().getTotalExperience() + " Level: " + event.getPlayer().getLevel(), "Debug");
                if (!plugin.Blacklistcode.startsWith("1")) {
                    if (plugin.PermissionsHandler.checkpermissionssilent(event.getPlayer(), "xpShop.admin")) {
                        if (xpShop.updateaviable) {
                            plugin.PlayerLogger(event.getPlayer(), "installed xpShop version: " + plugin.Version + ", latest version: " + plugin.newversion, "Warning");
                            plugin.PlayerLogger(event.getPlayer(), "New xpShop update aviable: type \"/xpShop update\" to install!", "Warning");
                            if (!plugin.getConfig().getBoolean("installondownload")) {
                                plugin.PlayerLogger(event.getPlayer(), "Please edit the config.yml if you wish that the plugin updates itself atomatically!", "Warning");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                plugin.report.report(334, "join event failed", e.getMessage(), "xpShopListener", e);
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
    public void aendern(SignChangeEvent event) {
        if (!plugin.toggle) {
            try {
                if (plugin.config.debug) {
                    plugin.Logger("First Line " + event.getLine(0), "Debug");
                }
                if (event.getLine(0).equalsIgnoreCase("[xpShop]")) {
                    createHandler.CreatexpShop(event);
                } else if (event.getLine(0).equalsIgnoreCase("[xpShopSafe]")) {
                    createHandler.CreateSafe(event);
                }
            } catch (Exception e) {
                plugin.report.report(338, "aendern event failed", e.getMessage(), "xpShopListener", e);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onXPChange(PlayerExpChangeEvent event) {
    	final PlayerExpChangeEvent e = event;
    	plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
			
			@Override
			public void run() {
				plugin.Logger("Player " + e.getPlayer().getTotalExperience() + " XP: " + e.getPlayer().getTotalExperience() + " Level: " + e.getPlayer().getLevel(), "Debug");
//	            double y = 1.75 * (event.getPlayer().getLevel() + event.getPlayer().getExp()) * (event.getPlayer().getLevel() + event.getPlayer().getExp()) + 4.9997 * (event.getPlayer().getLevel() + event.getPlayer().getExp()) + 0.1327;
//	            event.getPlayer().setTotalExperience((int) y);
	                double t = plugin.getLevelXP(e.getPlayer().getLevel()) + (xpShop.nextLevelAt(e.getPlayer().getLevel()) * e.getPlayer().getExp());
	                e.getPlayer().setTotalExperience((int) t);
			}
		}, 10);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLevelChange(PlayerLevelChangeEvent event) {
    	final PlayerLevelChangeEvent e = event;
    	plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
			
			@Override
			public void run() {
				plugin.Logger("Player " + e.getPlayer().getTotalExperience() + " XP: " + e.getPlayer().getTotalExperience() + " Level: " + e.getPlayer().getLevel(), "Debug");
//	            double y = 1.75 * (event.getPlayer().getLevel() + event.getPlayer().getExp()) * (event.getPlayer().getLevel() + event.getPlayer().getExp()) + 4.9997 * (event.getPlayer().getLevel() + event.getPlayer().getExp()) + 0.1327;
//	            event.getPlayer().setTotalExperience((int) y);
	                double t = plugin.getLevelXP(e.getPlayer().getLevel()) + (xpShop.nextLevelAt(e.getPlayer().getLevel()) * e.getPlayer().getExp());
	                e.getPlayer().setTotalExperience((int) t);
			}
		}, 10);
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
            try {
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
                                        if (MetricsHandler.Shop.containsKey(loc)) {
                                            MetricsHandler.Shop.remove(loc);
                                            plugin.Logger("Removed Shop from list!", "Debug");
                                        }
                                    } else if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.create.admin")) {
                                        plugin.PlayerLogger(p, "Destroying xpShop (Admin)!", "");
                                        MTLocation loc = MTLocation.getMTLocationFromLocation(sign.getLocation());
                                        if (MetricsHandler.Shop.containsKey(loc)) {
                                            MetricsHandler.Shop.remove(loc);
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
                                        if (MetricsHandler.Safe.containsKey(loc)) {
                                            MetricsHandler.Safe.remove(loc);
                                            plugin.Logger("Removed Safe from list!", "Debug");
                                        }
                                    } else if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.admin")) {
                                        plugin.PlayerLogger(p, "Destroying xpShopSafe (Admin)!", "");
                                        MTLocation loc = MTLocation.getMTLocationFromLocation(sign.getLocation());
                                        if (MetricsHandler.Safe.containsKey(loc)) {
                                            MetricsHandler.Safe.remove(loc);
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
                                    if (MetricsHandler.Shop.containsKey(loc)) {
                                        MetricsHandler.Shop.remove(loc);
                                        plugin.Logger("Removed Shop from list!", "Debug");
                                    }
                                } else if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.create.admin")) {
                                    plugin.PlayerLogger(p, "Destroying xpShop (Admin)!", "");
                                    MTLocation loc = MTLocation.getMTLocationFromLocation(s.getLocation());
                                    if (MetricsHandler.Shop.containsKey(loc)) {
                                        MetricsHandler.Shop.remove(loc);
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
                                    MTLocation loc = MTLocation.getMTLocationFromLocation(s.getLocation());
                                    if (MetricsHandler.Safe.containsKey(loc)) {
                                        MetricsHandler.Safe.remove(loc);
                                        plugin.Logger("Removed Safe from list!", "Debug");
                                    }
                                } else if (plugin.PermissionsHandler.checkpermissions(p, "xpShop.admin")) {
                                    plugin.PlayerLogger(p, "Destroying xpShopSafe (Admin)!", "");
                                    MTLocation loc = MTLocation.getMTLocationFromLocation(s.getLocation());
                                    if (MetricsHandler.Safe.containsKey(loc)) {
                                        MetricsHandler.Safe.remove(loc);
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
            } catch (Exception e) {
                plugin.report.report(338, "break event failed", e.getMessage(), "xpShopListener", e);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        try {
            interactHandler.InteracteventHandler(event);
        } catch (Exception e) {
            plugin.report.report(340, "interact event failed", e.getMessage(), "xpShopListener", e);
        }
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
