/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ibhh.xpShop;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 *
 * @author Simon
 */
public class InteractHandler {

    private xpShop plugin;
    private SafeHandler safeHandler;
    private SignHandler signHandler;
    private SignConSafeHandler signConSafeHandler;

    /**
     * Konstruktor of InteractHandler
     *
     * @param pl
     */
    public InteractHandler(xpShop pl) {
        plugin = pl;
        safeHandler = new SafeHandler(pl);
        signHandler = new SignHandler(pl);
        signConSafeHandler = new SignConSafeHandler(pl);
    }

    /**
     * Handles playerinteracts
     *
     * @param event
     */
    public void InteracteventHandler(PlayerInteractEvent event) {
        if (!plugin.toggle) {
            Player p = event.getPlayer();
            if (plugin.config.debug) {
                plugin.Logger("A interact Event dected by player: " + p.getName(), "Debug");
            }
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                LeftInteract(event);
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                RightInteract(event);
            }
        }
    }

    /**
     * Manages a rightklick on a block.
     *
     * @param event
     */
    public void RightInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if ((event.hasBlock()) && ((event.getClickedBlock().getState() instanceof Sign)) && (!p.isSneaking())) { // && !(p.isSneaking())
            Sign s = (Sign) event.getClickedBlock().getState();
            String[] line = s.getLines();
            if (line[0].equalsIgnoreCase("[xpShop]")) {
                if (plugin.config.optionalconnectionofSafetoShop) {
                    signConSafeHandler.xpShopSignRechts(event, line, p, s);
                } else {
                    signHandler.RechtsKlick(event, line, p, s);
                }
                MTLocation loc = MTLocation.getMTLocationFromLocation(event.getClickedBlock().getLocation());
                if (!plugin.metricshandler.Shop.containsKey(loc)) {
                    plugin.metricshandler.Shop.put(loc, event.getPlayer().getName());
                    plugin.Logger("Added Shop to list!", "Debug");
                }
            } else if (line[0].equalsIgnoreCase("[xpShopSafe]")) {
                safeHandler.RechtsKlick(event, line, p, s);
                MTLocation loc = MTLocation.getMTLocationFromLocation(event.getClickedBlock().getLocation());
                if (!plugin.metricshandler.Safe.containsKey(loc)) {
                    plugin.metricshandler.Safe.put(loc, event.getPlayer().getName());
                    plugin.Logger("Added Safe to list!", "Debug");
                }
            }
        }
    }

    /**
     * Manages leftklickinteracts
     *
     * @param event
     */
    public void LeftInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (plugin.config.debug) {
            plugin.Logger("A left interact Event dected by player: " + p.getName(), "Debug");
        }
        if ((event.hasBlock()) && ((event.getClickedBlock().getState() instanceof Sign)) && (!p.isSneaking())) { // && !(p.isSneaking())
            Sign s = (Sign) event.getClickedBlock().getState();
            String[] line = s.getLines();
            if (plugin.config.debug) {
                plugin.Logger("Checking first line!", "Debug");
            }
            if (line[0].equalsIgnoreCase("[xpShop]")) {
                if (plugin.config.optionalconnectionofSafetoShop) {
                    signConSafeHandler.xpShopSignLinks(event, line, p, s);
                } else {
                    signHandler.LinksKlick(event, line, p, s);
                }
                MTLocation loc = MTLocation.getMTLocationFromLocation(event.getClickedBlock().getLocation());
                if (!plugin.metricshandler.Shop.containsKey(loc)) {
                    plugin.metricshandler.Shop.put(loc, event.getPlayer().getName());
                }
            } else if (line[0].equalsIgnoreCase("[xpShopSafe]")) {
                safeHandler.LinksKlick(event, line, p, s);
                MTLocation loc = MTLocation.getMTLocationFromLocation(event.getClickedBlock().getLocation());
                if (!plugin.metricshandler.Safe.containsKey(loc)) {
                    plugin.metricshandler.Safe.put(loc, event.getPlayer().getName());
                }
            }
        }
    }
}
